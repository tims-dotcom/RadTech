package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
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
import org.tim.radtech.energy.IPowerStorage
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerCapabilities
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.menu.TransformerMenu
import org.tim.radtech.registry.ModBlockEntities

class TransformerBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.TRANSFORMER.get(), pos, state), MenuProvider {

    // 0: STEP_UP (380V -> 10kV), 1: STEP_DOWN (10kV -> 380V)
    var mode: Int = 0

    val lowBuffer = PowerBuffer(VoltageTier.INDUSTRIAL, 500_000L, 64, 64)
    val highBuffer = PowerBuffer(VoltageTier.DISTRIBUTION, 500_000L, 16, 16)

    fun getCapability(side: Direction?): IPowerStorage {
        val facing = blockState.getValue(TransformerBlock.FACING)
        // Задня грань — вхід, передня — вихід (або навпаки залежно від режиму)
        return if (mode == 0) {
            // Step-Up: вхід 380V (Low), вихід 10kV (High)
            if (side == facing) highBuffer else lowBuffer
        } else {
            // Step-Down: вхід 10kV (High), вихід 380V (Low)
            if (side == facing) lowBuffer else highBuffer
        }
    }

    val data = object : ContainerData {
        override fun get(index: Int): Int = when (index) {
            0 -> mode
            1 -> (lowBuffer.stored / 1000).toInt()
            2 -> (highBuffer.stored / 1000).toInt()
            else -> 0
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                0 -> mode = value
            }
        }

        override fun getCount(): Int = 3
    }

    override fun getDisplayName(): Component = Component.translatable("block.radtech.transformer")

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        TransformerMenu(id, inv, this, data)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        lowBuffer.save(tag)
        highBuffer.save(tag)
        tag.putInt("mode", mode)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        lowBuffer.load(tag)
        highBuffer.load(tag)
        mode = tag.getInt("mode").coerceIn(0, 1)
    }

    companion object {
        const val MAX_CONVERT_WATTS = 50_000L // 50 кВт

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: TransformerBlockEntity) {
            if (level.isClientSide) return

            if (be.mode == 0) {
                // Step-Up: беремо з LowBuffer -> переводимо в HighBuffer
                val canTake = minOf(MAX_CONVERT_WATTS, be.lowBuffer.stored, be.highBuffer.capacity - be.highBuffer.stored)
                if (canTake > 0) {
                    be.lowBuffer.consume(canTake)
                    be.highBuffer.fill(canTake)
                }
            } else {
                // Step-Down: беремо з HighBuffer -> переводимо в LowBuffer
                val canTake = minOf(MAX_CONVERT_WATTS, be.highBuffer.stored, be.lowBuffer.capacity - be.lowBuffer.stored)
                if (canTake > 0) {
                    be.highBuffer.consume(canTake)
                    be.lowBuffer.fill(canTake)
                }
            }

            // Роздача виходу
            val facing = state.getValue(TransformerBlock.FACING)
            val outSide = facing
            val targetPos = pos.relative(outSide)
            val outStorage = if (be.mode == 0) be.highBuffer else be.lowBuffer
            val target = level.getCapability(PowerCapabilities.POWER, targetPos, outSide.opposite)
            if (target != null && target.tier == outStorage.tier) {
                val toSend = outStorage.extract(outStorage.maxOutputPerTick, true)
                val accepted = target.receive(toSend, false)
                outStorage.extract(accepted, false)
            }

            be.setChanged()
        }
    }
}
