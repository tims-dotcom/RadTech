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
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.energy.PowerTransfer
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.menu.ThermalGeneratorMenu
import org.tim.radtech.registry.ModBlockEntities

class ThermalGeneratorBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.THERMAL_GENERATOR.get(), pos, state), MenuProvider {

    val items = object : ItemStackHandler(1) {
        override fun onContentsChanged(slot: Int) = setChanged()
        override fun isItemValid(slot: Int, stack: ItemStack) = stack.getBurnTime(RecipeType.SMELTING) > 0
    }

    val power = PowerBuffer(TIER, CAPACITY, 0, OUTPUT_AMPS)

    var burnTime = 0
    var burnDuration = 0

    val data = object : ContainerData {
        override fun get(index: Int) = when (index) {
            BURN_TIME -> burnTime
            BURN_DURATION -> burnDuration
            else -> PowerSync.read(power, index - POWER_OFFSET)
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                BURN_TIME -> burnTime = value
                BURN_DURATION -> burnDuration = value
            }
        }

        override fun getCount() = DATA_COUNT
    }

    override fun getDisplayName(): Component = Component.translatable("block.radtech.thermal_generator")

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        ThermalGeneratorMenu(id, inv, this, data)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.put("inv", items.serializeNBT(registries))
        tag.putInt("burnTime", burnTime)
        tag.putInt("burnDuration", burnDuration)
        power.save(tag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        items.deserializeNBT(registries, tag.getCompound("inv"))
        burnTime = tag.getInt("burnTime")
        burnDuration = tag.getInt("burnDuration")
        power.load(tag)
    }

    private fun ignite(): Boolean {
        val fuel = items.getStackInSlot(0)
        val time = fuel.getBurnTime(RecipeType.SMELTING)
        if (time <= 0) return false
        val remainder = fuel.craftingRemainingItem
        items.extractItem(0, 1, false)
        if (!remainder.isEmpty && items.getStackInSlot(0).isEmpty) items.setStackInSlot(0, remainder)
        burnTime = time
        burnDuration = time
        return true
    }

    companion object {
        val TIER = VoltageTier.INDUSTRIAL
        const val OUTPUT_AMPS = 12
        const val CAPACITY = 800_000L
        val GENERATION = TIER.volts.toLong() * OUTPUT_AMPS

        const val BURN_TIME = 0
        const val BURN_DURATION = 1
        const val POWER_OFFSET = 2
        const val DATA_COUNT = POWER_OFFSET + PowerSync.SLOTS

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: ThermalGeneratorBlockEntity) {
            if (level.isClientSide) return
            var changed = false
            val hasRoom = be.power.capacity - be.power.stored >= GENERATION
            if (be.burnTime == 0 && hasRoom) changed = be.ignite()
            if (be.burnTime > 0 && hasRoom) {
                be.burnTime--
                be.power.fill(GENERATION)
            }
            if (be.power.stored > 0) {
                PowerTransfer.push(level, pos, be.power)
            }
            if (changed || level.gameTime % 60L == 0L) be.setChanged()
        }
    }
}
