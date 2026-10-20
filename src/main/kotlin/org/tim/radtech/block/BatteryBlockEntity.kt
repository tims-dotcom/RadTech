package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerTransfer
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.registry.ModBlockEntities

class BatteryBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.BATTERY.get(), pos, state) {

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

    var heatTicks = 0
    var criticalTicks = 0
    var lastAmps: Double = 0.0

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        power.save(tag)
        tag.putInt("heatTicks", heatTicks)
        tag.putInt("criticalTicks", criticalTicks)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        power.load(tag)
        heatTicks = tag.getInt("heatTicks")
        criticalTicks = tag.getInt("criticalTicks")
    }

    companion object {
        val TIER = VoltageTier.INDUSTRIAL
        const val CAPACITY = 2_000_000L
        const val MAX_INPUT_AMPS = 128
        const val MAX_OUTPUT_AMPS = 128

        // Номінальний струм акумулятора (вище нього йде нагрів)
        const val SAFE_AMPS = 16.0

        // Пороги для перегріву
        const val OVERHEAT_THRESHOLD = 80
        const val COOL_THRESHOLD = 30
        const val EXPLOSION_THRESHOLD = 160 // 8 секунд перегріву

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: BatteryBlockEntity) {
            if (level.isClientSide) return

            // Спершу роздаємо енергію сусідам напряму (якщо вони поруч)
            PowerTransfer.push(level, pos, be.power)

            val sLevel = level as? ServerLevel ?: return

            // Рахуємо струм, який пройшов крізь акумулятор (з мережі або від сусідів)
            val ampsFlow = be.power.flowAccumulator.toDouble() / TIER.volts
            be.lastAmps = ampsFlow
            be.power.flowAccumulator = 0L // скидаємо на наступний такт

            // Якщо струм вищий за безпечний (16 А), акумулятор нагрівається пропорційно навантаженню
            if (ampsFlow > SAFE_AMPS) {
                val excess = (ampsFlow - SAFE_AMPS).coerceAtLeast(1.0)
                be.heatTicks += (excess * 1.5).toInt().coerceAtLeast(2)
            } else if (ampsFlow < 8.0 && be.heatTicks > 0) {
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
