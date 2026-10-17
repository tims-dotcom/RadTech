package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler
import org.tim.radtech.menu.MachineMenu
import org.tim.radtech.registry.ModBlockEntities

class MachineBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.MACHINE.get(), pos, state), MenuProvider {

    val items = object : ItemStackHandler(2) {
        override fun onContentsChanged(slot: Int) = setChanged()
    }

    var progress = 0
    var maxProgress = 100

    val data = object : ContainerData {
        override fun get(index: Int) = if (index == 0) progress else maxProgress
        override fun set(index: Int, value: Int) {
            if (index == 0) progress = value else maxProgress = value
        }
        override fun getCount() = 2
    }

    fun canWork() = true

    override fun getDisplayName(): Component = Component.translatable("block.radtech.crusher")

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        MachineMenu(id, inv, this, data)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.put("inv", items.serializeNBT(registries))
        tag.putInt("progress", progress)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        items.deserializeNBT(registries, tag.getCompound("inv"))
        progress = tag.getInt("progress")
    }

    companion object {
        private val RECIPES = mapOf(
            "radtech:raw_tin" to "radtech:tin_dust",
            "radtech:raw_aluminum" to "radtech:aluminum_dust",
            "radtech:raw_lithium" to "radtech:lithium_dust",
            "radtech:raw_neodymium" to "radtech:neodymium_dust",
            "radtech:raw_titanium" to "radtech:titanium_dust",
            "radtech:raw_tungsten" to "radtech:tungsten_dust"
        )

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: MachineBlockEntity) {
            if (level.isClientSide) return
            val input = be.items.getStackInSlot(0)
            val result = RECIPES[BuiltInRegistries.ITEM.getKey(input.item).toString()]
                ?.let { BuiltInRegistries.ITEM.get(ResourceLocation.parse(it)) }
                ?.let { ItemStack(it, 2) }
            if (input.isEmpty || result == null || result.isEmpty || !be.canWork()) {
                if (be.progress != 0) {
                    be.progress = 0
                    be.setChanged()
                }
                return
            }
            val out = be.items.getStackInSlot(1)
            val fits = out.isEmpty ||
                    (ItemStack.isSameItemSameComponents(out, result) && out.count + result.count <= out.maxStackSize)
            if (!fits) return
            if (++be.progress >= be.maxProgress) {
                be.progress = 0
                be.items.extractItem(0, 1, false)
                be.items.insertItem(1, result.copy(), false)
            }
            be.setChanged()
        }
    }
}