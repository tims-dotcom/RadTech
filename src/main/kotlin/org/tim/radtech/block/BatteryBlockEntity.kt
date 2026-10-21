package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.neoforged.neoforge.items.ItemStackHandler
import org.tim.radtech.item.IElectricItem
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.energy.PowerTransfer
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.menu.BatteryMenu
import org.tim.radtech.registry.ModBlockEntities

class BatteryBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.BATTERY.get(), pos, state), MenuProvider {

    class TrackedBuffer(
        tier: VoltageTier,
        capacity: Long,
        maxInputAmps: Int,
        maxOutputAmps: Int
    ) : PowerBuffer(tier, capacity, maxInputAmps, maxOutputAmps) {
        var flowAccumulator: Long = 0L

        override fun receive(amount: Long, simulate: Boolean): Long {
            val accepted = super.receive(amount, simulate)
            if (!simulate) flowAccumulator += accepted
            return accepted
        }

        override fun extract(amount: Long, simulate: Boolean): Long {
            val taken = super.extract(amount, simulate)
            if (!simulate) flowAccumulator += taken
            return taken
        }
    }

    val power = TrackedBuffer(TIER, CAPACITY, MAX_INPUT_AMPS, MAX_OUTPUT_AMPS)

    val items = object : ItemStackHandler(2) {
        override fun onContentsChanged(slot: Int) = setChanged()
    }

    var heatTicks = 0
    var criticalTicks = 0
    var lastAmps: Double = 0.0

    val data = object : ContainerData {
        override fun get(index: Int): Int = when (index) {
            0, 1, 2, 3 -> PowerSync.read(power, index)
            4 -> (lastAmps * 10).toInt()
            5 -> heatTicks
            6 -> if (blockState.getValue(BatteryBlock.OVERHEATED)) 1 else 0
            else -> 0
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                4 -> lastAmps = value / 10.0
                5 -> heatTicks = value
            }
        }

        override fun getCount(): Int = BatteryMenu.DATA_COUNT
    }

    override fun getDisplayName(): Component = Component.translatable("block.radtech.battery")

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        BatteryMenu(id, inv, this, data)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        power.save(tag)
        tag.put("items", items.serializeNBT(registries))
        tag.putInt("heatTicks", heatTicks)
        tag.putInt("criticalTicks", criticalTicks)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        power.load(tag)
        if (tag.contains("items")) items.deserializeNBT(registries, tag.getCompound("items"))
        heatTicks = tag.getInt("heatTicks")
        criticalTicks = tag.getInt("criticalTicks")
    }

    companion object {
        val TIER = VoltageTier.INDUSTRIAL
        const val CAPACITY = 4_000_000L
        const val MAX_INPUT_AMPS = 256
        const val MAX_OUTPUT_AMPS = 256

        // Номінальний струм акумулятора (вище нього йде нагрів)
        const val SAFE_AMPS = 32.0

        // Пороги для перегріву
        const val OVERHEAT_THRESHOLD = 80
        const val COOL_THRESHOLD = 30
        const val EXPLOSION_THRESHOLD = 160 // 8 секунд перегріву

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: BatteryBlockEntity) {
            if (level.isClientSide) return

            // Спершу роздаємо енергію сусідам напряму (якщо вони поруч)
            PowerTransfer.push(level, pos, be.power)

            // Заряджання/розряджання предметів у слотах
            val chargeStack = be.items.getStackInSlot(0)
            if (!chargeStack.isEmpty && chargeStack.item is IElectricItem) {
                val electric = chargeStack.item as IElectricItem
                val space = electric.capacity - electric.getStored(chargeStack)
                if (space > 0 && be.power.stored > 0) {
                    val toSend = minOf(space, 4_000L, be.power.stored)
                    val extracted = be.power.extract(toSend, false)
                    if (extracted > 0) {
                        electric.receive(chargeStack, extracted, false)
                    }
                }
            }
            val dischargeStack = be.items.getStackInSlot(1)
            if (!dischargeStack.isEmpty && dischargeStack.item is IElectricItem) {
                val electric = dischargeStack.item as IElectricItem
                val stored = electric.getStored(dischargeStack)
                val space = be.power.capacity - be.power.stored
                if (stored > 0 && space > 0) {
                    val toTake = minOf(stored, 4_000L, space)
                    val taken = electric.extract(dischargeStack, toTake, false)
                    if (taken > 0) {
                        be.power.receive(taken, false)
                    }
                }
            }

            val sLevel = level as? ServerLevel ?: return

            // Рахуємо струм, який пройшов крізь акумулятор (з мережі або від сусідів)
            val ampsFlow = be.power.flowAccumulator.toDouble() / TIER.volts
            be.lastAmps = ampsFlow
            be.power.flowAccumulator = 0L // скидаємо на наступний такт

            // Якщо струм вищий за безпечний (32 А), акумулятор нагрівається пропорційно навантаженню
            if (ampsFlow > SAFE_AMPS) {
                val excess = (ampsFlow - SAFE_AMPS).coerceAtLeast(1.0)
                be.heatTicks += (excess * 1.5).toInt().coerceAtLeast(2)
            } else if (ampsFlow < 16.0 && be.heatTicks > 0) {
                be.heatTicks--
            }

            val wasOverheated = state.getValue(BatteryBlock.OVERHEATED)
            val isOverheated = if (wasOverheated) {
                be.heatTicks >= COOL_THRESHOLD
            } else {
                be.heatTicks >= OVERHEAT_THRESHOLD
            }

            if (isOverheated != wasOverheated) {
                level.setBlock(pos, state.setValue(BatteryBlock.OVERHEATED, isOverheated), 3)
            }

            if (isOverheated) {
                be.criticalTicks++

                // Звук кипіння електроліту кожні 20 тіків
                if (be.criticalTicks % 20 == 0) {
                    level.playSound(
                        null, pos,
                        SoundEvents.LAVA_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.8f, 1.4f
                    )
                }

                // Дим
                sLevel.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.x + 0.5, pos.y + 1.05, pos.z + 0.5,
                    3, 0.15, 0.05, 0.15, 0.03
                )

                // Вибух 0.4 від TNT (сила 1.6f)
                if (be.criticalTicks >= EXPLOSION_THRESHOLD) {
                    level.destroyBlock(pos, false)
                    level.explode(
                        null,
                        pos.x + 0.5, pos.y + 0.5, pos.z + 0.5,
                        1.6f,
                        Level.ExplosionInteraction.BLOCK
                    )
                    return
                }
            } else {
                be.criticalTicks = 0
            }

            be.setChanged()
        }
    }
}
