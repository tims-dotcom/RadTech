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
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler
import org.tim.radtech.Radtech
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.menu.LargeThermalGeneratorMenu
import org.tim.radtech.registry.ModBlockEntities

class LargeThermalGeneratorBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.LARGE_THERMAL_GEN.get(), pos, state), MenuProvider {

    val items = object : ItemStackHandler(4) {
        override fun onContentsChanged(slot: Int) = setChanged()
    }

    // Промислова ТЕС: 380 В, 48 Ампер (18 240 Вт!), буфер 2 000 000 Вт·год
    val power = PowerBuffer(VoltageTier.INDUSTRIAL, CAPACITY, 0, OUTPUT_AMPS)

    var burnTime = 0
    var maxBurnTime = 0
    var isFormed = false

    val data = object : ContainerData {
        override fun get(index: Int): Int = when (index) {
            BURN_TIME -> burnTime
            MAX_BURN_TIME -> maxBurnTime
            IS_FORMED -> if (isFormed) 1 else 0
            else -> PowerSync.read(power, index - POWER_OFFSET)
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                BURN_TIME -> burnTime = value
                MAX_BURN_TIME -> maxBurnTime = value
            }
        }

        override fun getCount(): Int = DATA_COUNT
    }

    fun checkStructure(level: Level, pos: BlockPos, state: BlockState): Boolean {
        val facing = state.getValue(LargeThermalGeneratorBlock.FACING)
        val center = pos.relative(facing.opposite)

        for (dx in -1..1) {
            for (dy in -1..1) {
                for (dz in -1..1) {
                    val p = center.offset(dx, dy, dz)
                    if (p == pos) continue // Контролер
                    if (dx == 0 && dy == 0 && dz == 0) continue // Порожній внутрішній сердечник
                    val b = level.getBlockState(p).block
                    if (b != Radtech.BOILER_CASING_BLOCK.get()) {
                        isFormed = false
                        return false
                    }
                }
            }
        }
        isFormed = true
        return true
    }

    override fun getDisplayName(): Component = Component.translatable("block.radtech.large_thermal_generator")

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        LargeThermalGeneratorMenu(id, inv, this, data)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.put("inv", items.serializeNBT(registries))
        tag.putInt("burnTime", burnTime)
        tag.putInt("maxBurnTime", maxBurnTime)
        tag.putBoolean("formed", isFormed)
        power.save(tag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        items.deserializeNBT(registries, tag.getCompound("inv"))
        burnTime = tag.getInt("burnTime")
        maxBurnTime = tag.getInt("maxBurnTime")
        isFormed = tag.getBoolean("formed")
        power.load(tag)
    }

    companion object {
        const val CAPACITY = 2_000_000L
        const val OUTPUT_AMPS = 48
        const val GENERATION_PER_TICK = 380L * OUTPUT_AMPS // 18 240 Вт

        const val BURN_TIME = 0
        const val MAX_BURN_TIME = 1
        const val IS_FORMED = 2
        const val POWER_OFFSET = 3
        const val DATA_COUNT = POWER_OFFSET + PowerSync.SLOTS

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: LargeThermalGeneratorBlockEntity) {
            if (level.isClientSide) return

            // Періодично раз на 40 тіків перевіряємо цілісність куба
            if (level.gameTime % 40 == 0L) {
                val formed = be.checkStructure(level, pos, state)
                if (state.getValue(LargeThermalGeneratorBlock.FORMED) != formed) {
                    level.setBlock(pos, state.setValue(LargeThermalGeneratorBlock.FORMED, formed), 3)
                }
            }

            if (!be.isFormed) return

            if (be.burnTime > 0) {
                be.burnTime--
                be.power.fill(GENERATION_PER_TICK)
            } else if (be.power.stored < be.power.capacity) {
                // Шукаємо паливо у 4 слотах
                for (slot in 0 until be.items.slots) {
                    val stack = be.items.getStackInSlot(slot)
                    if (!stack.isEmpty) {
                        val fuelValue = stack.getBurnTime(RecipeType.SMELTING)
                        if (fuelValue > 0) {
                            be.maxBurnTime = fuelValue * 3 / 2 // 150% ефективність згоряння!
                            be.burnTime = be.maxBurnTime
                            stack.shrink(1)
                            be.setChanged()
                            break
                        }
                    }
                }
            }

            val isLit = be.burnTime > 0
            if (state.getValue(LargeThermalGeneratorBlock.LIT) != isLit) {
                level.setBlock(pos, state.setValue(LargeThermalGeneratorBlock.LIT, isLit), 3)
            }

            if (level.gameTime % 60L == 0L) {
                be.setChanged()
            }
        }
    }
}
