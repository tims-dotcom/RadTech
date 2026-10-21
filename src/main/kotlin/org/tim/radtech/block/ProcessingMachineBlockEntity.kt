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
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.menu.ProcessingMenu

class MachineSpec(val tier: VoltageTier, val inputAmps: Int, val capacity: Long, val titleKey: String)

class MachineRecipe(val result: ItemStack, val time: Int)

abstract class ProcessingMachineBlockEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
    spec: MachineSpec
) : BlockEntity(type, pos, state), MenuProvider {

    private val titleKey = spec.titleKey

    val items = object : ItemStackHandler(2) {
        override fun onContentsChanged(slot: Int) = setChanged()
    }

    val power = PowerBuffer(spec.tier, spec.capacity, spec.inputAmps, 0)

    var progress = 0
    var maxProgress = 100

    val data = object : ContainerData {
        override fun get(index: Int) = when (index) {
            PROGRESS -> progress
            MAX_PROGRESS -> maxProgress
            else -> PowerSync.read(power, index - POWER_OFFSET)
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                PROGRESS -> progress = value
                MAX_PROGRESS -> maxProgress = value
            }
        }

        override fun getCount() = DATA_COUNT
    }

    protected abstract val menuType: MenuType<*>

    abstract fun findRecipe(input: ItemStack, level: Level): MachineRecipe?

    override fun getDisplayName(): Component = Component.translatable(titleKey)

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        ProcessingMenu(menuType, id, inv, this, data)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.put("inv", items.serializeNBT(registries))
        tag.putInt("progress", progress)
        power.save(tag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        items.deserializeNBT(registries, tag.getCompound("inv"))
        progress = tag.getInt("progress")
        power.load(tag)
    }

    companion object {
        const val PROGRESS = 0
        const val MAX_PROGRESS = 1
        const val POWER_OFFSET = 2
        const val DATA_COUNT = POWER_OFFSET + PowerSync.SLOTS

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: ProcessingMachineBlockEntity) {
            if (level.isClientSide) return
            val input = be.items.getStackInSlot(0)
            if (input.isEmpty) {
                if (be.progress != 0) {
                    be.progress = 0
                    be.setChanged()
                }
                return
            }
            val recipe = be.findRecipe(input, level)
            if (recipe == null || recipe.result.isEmpty) {
                if (be.progress != 0) {
                    be.progress = 0
                    be.setChanged()
                }
                return
            }
            be.maxProgress = recipe.time
            val out = be.items.getStackInSlot(1)
            val fits = out.isEmpty ||
                    (ItemStack.isSameItemSameComponents(out, recipe.result) &&
                            out.count + recipe.result.count <= out.maxStackSize)
            if (!fits || !be.power.consume(be.power.maxInputPerTick)) return
            if (++be.progress >= recipe.time) {
                be.progress = 0
                be.items.extractItem(0, 1, false)
                be.items.insertItem(1, recipe.result.copy(), false)
            }
        }
    }
}
