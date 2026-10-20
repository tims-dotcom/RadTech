package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.tim.radtech.energy.IPowerStorage
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerTransfer
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.menu.CircuitBreakerMenu
import org.tim.radtech.registry.ModBlockEntities

class CircuitBreakerBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.CIRCUIT_BREAKER.get(), pos, state), MenuProvider {

    var isTripped: Boolean = false
    var tripLimitAmps: Int = 32
    var lastAmps: Double = 0.0

    class BreakerBuffer(val be: CircuitBreakerBlockEntity) :
        PowerBuffer(VoltageTier.INDUSTRIAL, 100_000L, 120, 120) {
        var flowAccumulator: Long = 0L

        override val maxInputAmps: Int get() = if (be.isTripped) 0 else be.tripLimitAmps
        override val maxOutputAmps: Int get() = if (be.isTripped) 0 else be.tripLimitAmps

        override fun receive(amount: Long, simulate: Boolean): Long {
            if (be.isTripped) return 0
            val accepted = super.receive(amount, simulate)
            if (!simulate) flowAccumulator += accepted
            return accepted
        }

        override fun extract(amount: Long, simulate: Boolean): Long {
            if (be.isTripped) return 0
            val taken = super.extract(amount, simulate)
            if (!simulate) flowAccumulator += taken
            return taken
        }
    }

    val power = BreakerBuffer(this)

    val data = object : ContainerData {
        override fun get(index: Int): Int = when (index) {
            0 -> if (isTripped) 1 else 0
            1 -> tripLimitAmps
            2 -> (lastAmps * 10).toInt()
            else -> 0
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                0 -> isTripped = (value == 1)
                1 -> tripLimitAmps = value
                2 -> lastAmps = value / 10.0
            }
        }

        override fun getCount(): Int = 3
    }

    override fun getDisplayName(): Component = Component.translatable("block.radtech.circuit_breaker")

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        CircuitBreakerMenu(id, inv, this, data)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        power.save(tag)
        tag.putBoolean("isTripped", isTripped)
        tag.putInt("tripLimitAmps", tripLimitAmps)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        power.load(tag)
        isTripped = tag.getBoolean("isTripped")
        tripLimitAmps = tag.getInt("tripLimitAmps").coerceIn(16, 120)
    }

    companion object {
        fun tick(level: Level, pos: BlockPos, state: BlockState, be: CircuitBreakerBlockEntity) {
            if (level.isClientSide) return

            if (!be.isTripped) {
                PowerTransfer.push(level, pos, be.power)
            }

            val amps = be.power.flowAccumulator.toDouble() / VoltageTier.INDUSTRIAL.volts
            be.lastAmps = amps
            be.power.flowAccumulator = 0L

            // Якщо струм перевищує встановлений ліміт автомата — ВИБИВАЄ!
            if (!be.isTripped && amps > be.tripLimitAmps) {
                be.isTripped = true
                level.playSound(
                    null, pos,
                    SoundEvents.DISPENSER_FAIL,
                    SoundSource.BLOCKS,
                    1.2f, 0.5f
                )
                level.playSound(
                    null, pos,
                    SoundEvents.LEVER_CLICK,
                    SoundSource.BLOCKS,
                    1.2f, 0.6f
                )
                be.setChanged()
            }
        }
    }
}
