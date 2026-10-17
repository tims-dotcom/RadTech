package org.tim.radtech.menu

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.SlotItemHandler
import org.tim.radtech.block.FurnaceBlockEntity
import org.tim.radtech.registry.ModMenus

class FurnaceMenu(id: Int, inv: Inventory, val be: FurnaceBlockEntity, val data: ContainerData) :
    AbstractContainerMenu(ModMenus.FURNACE.get(), id) {

    constructor(id: Int, inv: Inventory, buf: FriendlyByteBuf) :
            this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()) as FurnaceBlockEntity, SimpleContainerData(4))

    val isLit: Boolean get() = data.get(0) > 0
    val flameHeight: Int get() = data.get(0) * 13 / data.get(1).coerceAtLeast(1) + 1
    val arrowWidth: Int get() = data.get(2) * 24 / data.get(3).coerceAtLeast(1)

    init {
        addSlot(SlotItemHandler(be.items, 0, 56, 17))
        addSlot(SlotItemHandler(be.items, 1, 56, 53))
        addSlot(object : SlotItemHandler(be.items, 2, 116, 35) {
            override fun mayPlace(stack: ItemStack) = false
        })
        for (r in 0..2) for (c in 0..8) addSlot(Slot(inv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18))
        for (c in 0..8) addSlot(Slot(inv, c, 8 + c * 18, 142))
        addDataSlots(data)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val slot = slots[index]
        if (!slot.hasItem()) return ItemStack.EMPTY
        val stack = slot.item
        val copy = stack.copy()
        val moved = if (index < 3) moveItemStackTo(stack, 3, slots.size, true)
        else moveItemStackTo(stack, 1, 2, false) || moveItemStackTo(stack, 0, 1, false)
        if (!moved) return ItemStack.EMPTY
        if (stack.isEmpty) slot.setByPlayer(ItemStack.EMPTY) else slot.setChanged()
        return copy
    }

    override fun stillValid(player: Player) =
        stillValid(ContainerLevelAccess.create(be.level!!, be.blockPos), player, be.blockState.block)
}