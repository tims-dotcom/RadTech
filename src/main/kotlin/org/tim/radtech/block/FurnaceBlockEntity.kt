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
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler
import org.tim.radtech.menu.FurnaceMenu
import org.tim.radtech.registry.ModBlockEntities

class FurnaceBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.FURNACE.get(), pos, state), MenuProvider {

    val items = object : ItemStackHandler(3) {
        override fun onContentsChanged(slot: Int) = setChanged()
        override fun isItemValid(slot: Int, stack: ItemStack) =
            if (slot == 1) stack.getBurnTime(RecipeType.SMELTING) > 0 else true
    }

    var burnTime = 0
    var burnDuration = 0
    var progress = 0
    var maxProgress = 200

    val data = object : ContainerData {
        override fun get(index: Int) = when (index) {
            0 -> burnTime
            1 -> burnDuration
            2 -> progress
            else -> maxProgress
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                0 -> burnTime = value
                1 -> burnDuration = value
                2 -> progress = value
                else -> maxProgress = value
            }
        }

        override fun getCount() = 4
    }

    override fun getDisplayName(): Component = Component.translatable("block.radtech.furnace")

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        FurnaceMenu(id, inv, this, data)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.put("inv", items.serializeNBT(registries))
        tag.putInt("burnTime", burnTime)
        tag.putInt("burnDuration", burnDuration)
        tag.putInt("progress", progress)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        items.deserializeNBT(registries, tag.getCompound("inv"))
        burnTime = tag.getInt("burnTime")
        burnDuration = tag.getInt("burnDuration")
        progress = tag.getInt("progress")
    }

    companion object {
        fun tick(level: Level, pos: BlockPos, state: BlockState, be: FurnaceBlockEntity) {
            if (level.isClientSide) return
            var changed = false
            if (be.burnTime > 0) {
                be.burnTime--
                changed = true
            }
            val input = be.items.getStackInSlot(0)
            val result = if (input.isEmpty) ItemStack.EMPTY else
                level.recipeManager.getRecipeFor(RecipeType.SMELTING, SingleRecipeInput(input), level)
                    .map { it.value().getResultItem(level.registryAccess()) }
                    .orElse(ItemStack.EMPTY)
            val out = be.items.getStackInSlot(2)
            val canSmelt = !result.isEmpty && (out.isEmpty ||
                    (ItemStack.isSameItemSameComponents(out, result) && out.count + result.count <= out.maxStackSize))
            if (canSmelt && be.burnTime == 0) {
                val fuelTime = be.items.getStackInSlot(1).getBurnTime(RecipeType.SMELTING)
                if (fuelTime > 0) {
                    be.burnTime = fuelTime
                    be.burnDuration = fuelTime
                    be.items.extractItem(1, 1, false)
                    changed = true
                }
            }
            if (canSmelt && be.burnTime > 0) {
                if (++be.progress >= be.maxProgress) {
                    be.progress = 0
                    be.items.extractItem(0, 1, false)
                    be.items.insertItem(2, result.copy(), false)
                }
                changed = true
            } else if (be.progress > 0) {
                be.progress = (be.progress - 2).coerceAtLeast(0)
                changed = true
            }
            if (changed) be.setChanged()
        }
    }
}