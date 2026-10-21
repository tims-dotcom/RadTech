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
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.energy.PowerTransfer
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.menu.SolarPanelMenu
import org.tim.radtech.registry.ModBlockEntities

class SolarPanelBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.SOLAR_PANEL.get(), pos, state), MenuProvider {

    val power = PowerBuffer(VoltageTier.INDUSTRIAL, 200_000L, 0, 4)
    var currentGenWatts: Int = 0
    var sunPercent: Int = 0

    val data = object : ContainerData {
        override fun get(index: Int): Int = when (index) {
            0, 1, 2, 3 -> PowerSync.read(power, index)
            4 -> currentGenWatts
            5 -> sunPercent
            else -> 0
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                4 -> currentGenWatts = value
                5 -> sunPercent = value
            }
        }

        override fun getCount(): Int = 6
    }

    override fun getDisplayName(): Component = Component.translatable("block.radtech.solar_panel")

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        SolarPanelMenu(id, inv, this, data)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        power.save(tag)
        tag.putInt("genWatts", currentGenWatts)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        power.load(tag)
        currentGenWatts = tag.getInt("genWatts")
    }

    companion object {
        const val MAX_GEN_AMPS = 4
        val VOLTS = VoltageTier.INDUSTRIAL.volts

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: SolarPanelBlockEntity) {
            if (level.isClientSide) return

            // Перевіряємо небо раз на 20 тіків (1 секунду), уникаючи raycast-перевірок щотакту
            if (level.gameTime % 20L == 0L || (be.currentGenWatts == 0 && be.sunPercent == 0)) {
                val canSeeSky = level.canSeeSky(pos.above())
                val isDay = level.isDay
                val isRaining = level.isRaining

                val efficiency = when {
                    !canSeeSky || !isDay -> 0.0
                    isRaining -> 0.25
                    else -> 1.0
                }

                be.sunPercent = (efficiency * 100).toInt()
                be.currentGenWatts = (efficiency * VOLTS * MAX_GEN_AMPS).toInt()
            }

            if (be.currentGenWatts > 0) {
                be.power.fill(be.currentGenWatts.toLong())
            }

            if (be.power.stored > 0) {
                PowerTransfer.push(level, pos, be.power)
            }

            if (level.gameTime % 80L == 0L) {
                be.setChanged()
            }
        }
    }
}
