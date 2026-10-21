package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.item.IElectricItem
import org.tim.radtech.menu.BatteryMenu
import org.tim.radtech.registry.ModBlockEntities

class LithiumBatteryBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.LITHIUM_BATTERY.get(), pos, state), MenuProvider {

    val items = object : ItemStackHandler(2) {
        override fun onContentsChanged(slot: Int) = setChanged()
    }

    // Літій-іонний акумулятор: 16 МВт·год, вхід/вихід до 512 А, безпечний струм 64 А
    val power = BatteryBlockEntity.TrackedBuffer(VoltageTier.INDUSTRIAL, CAPACITY, MAX_AMPS, MAX_AMPS)

    var heatTicks = 0
    var criticalOverheatTicks = 0
    var lastAmps: Double = 0.0

    val data = object : ContainerData {
        override fun get(index: Int): Int = when (index) {
            0, 1, 2, 3 -> PowerSync.read(power, index)
            4 -> (lastAmps * 10).toInt()
            5 -> heatTicks
            6 -> if (blockState.getValue(LithiumBatteryBlock.OVERHEATED)) 1 else 0
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

    override fun getDisplayName(): Component = Component.translatable("block.radtech.lithium_battery")

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        BatteryMenu(id, inv, this, data)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.put("inv", items.serializeNBT(registries))
        tag.putInt("heatTicks", heatTicks)
        tag.putInt("criticalOverheatTicks", criticalOverheatTicks)
        power.save(tag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        items.deserializeNBT(registries, tag.getCompound("inv"))
        heatTicks = tag.getInt("heatTicks")
        criticalOverheatTicks = tag.getInt("criticalOverheatTicks")
        power.load(tag)
    }

    companion object {
        const val CAPACITY = 16_000_000L
        const val MAX_AMPS = 512
        const val SAFE_AMPS = 64.0
        const val OVERHEAT_THRESHOLD = 120
        const val EXPLOSION_THRESHOLD = 240

        const val HEAT = 0
        const val IS_OVERHEATED = 1
        const val POWER_OFFSET = 2
        const val DATA_COUNT = POWER_OFFSET + PowerSync.SLOTS

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: LithiumBatteryBlockEntity) {
            if (level.isClientSide) return

            org.tim.radtech.energy.PowerTransfer.push(level, pos, be.power)

            // Заряджання/розряджання предметів у слотах
            var itemTransferred = false
            val chargeStack = be.items.getStackInSlot(0)
            if (!chargeStack.isEmpty && chargeStack.item is IElectricItem) {
                val electric = chargeStack.item as IElectricItem
                val space = electric.capacity - electric.getStored(chargeStack)
                if (space > 0 && be.power.stored > 0) {
                    val toSend = minOf(space, 8_000L, be.power.stored)
                    val extracted = be.power.extract(toSend, false)
                    if (extracted > 0) {
                        electric.receive(chargeStack, extracted, false)
                        itemTransferred = true
                    }
                }
            }
            val dischargeStack = be.items.getStackInSlot(1)
            if (!dischargeStack.isEmpty && dischargeStack.item is IElectricItem) {
                val electric = dischargeStack.item as IElectricItem
                val stored = electric.getStored(dischargeStack)
                val space = be.power.capacity - be.power.stored
                if (stored > 0 && space > 0) {
                    val toTake = minOf(stored, 8_000L, space)
                    val taken = electric.extract(dischargeStack, toTake, false)
                    if (taken > 0) {
                        be.power.receive(taken, false)
                        itemTransferred = true
                    }
                }
            }

            val currentAmps = be.power.flowAccumulator.toDouble() / VoltageTier.INDUSTRIAL.volts
            be.lastAmps = currentAmps
            be.power.flowAccumulator = 0L

            if (currentAmps > SAFE_AMPS) {
                val excess = (currentAmps - SAFE_AMPS).coerceAtLeast(1.0)
                be.heatTicks = (be.heatTicks + (excess * 1.5).toInt().coerceAtLeast(2)).coerceAtMost(EXPLOSION_THRESHOLD)
            } else if (currentAmps < 32.0 && be.heatTicks > 0) {
                be.heatTicks--
            }

            val wasOverheated = state.getValue(LithiumBatteryBlock.OVERHEATED)
            val isOverheated = be.heatTicks >= OVERHEAT_THRESHOLD
            if (wasOverheated != isOverheated) {
                level.setBlock(pos, state.setValue(LithiumBatteryBlock.OVERHEATED, isOverheated), 3)
            }

            if (isOverheated) {
                be.criticalOverheatTicks++
                if (be.criticalOverheatTicks >= EXPLOSION_THRESHOLD) {
                    level.destroyBlock(pos, false)
                    level.explode(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 2.5f, Level.ExplosionInteraction.BLOCK)
                    return
                }
            } else {
                be.criticalOverheatTicks = (be.criticalOverheatTicks - 1).coerceAtLeast(0)
            }

            if (itemTransferred || wasOverheated != isOverheated || level.gameTime % 80L == 0L) {
                be.setChanged()
            }
        }
    }
}
