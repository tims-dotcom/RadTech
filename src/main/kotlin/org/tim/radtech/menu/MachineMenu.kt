package org.tim.radtech.menu

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.SlotItemHandler
import org.tim.radtech.block.MachineBlockEntity
import org.tim.radtech.registry.ModMenus

class MachineMenu(id: Int, inv: Inventory, val be: MachineBlockEntity, val data: ContainerData) :
    AbstractContainerMenu(ModMenus.MACHINE.get(), id) {

    constructor(id: Int, inv: Inventory, buf: FriendlyByteBuf) :
            this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()) as MachineBlockEntity, SimpleContainerData(4))

    val progress: Int get() = data.get(0)
    val maxProgress: Int get() = data.get(1).coerceAtLeast(1)
    val energy: Long get() = (data.get(3).toLong() shl 15) or data.get(2).toLong()

    init {
        addSlot(SlotItemHandler(be.items, 0, 56, 35))
        addSlot(object : SlotItemHandler(be.items, 1, 116, 35) {
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
        val moved = if (index < 2) moveItemStackTo(stack, 2, slots.size, true)
        else moveItemStackTo(stack, 0, 1, false)
        if (!moved) return ItemStack.EMPTY
        if (stack.isEmpty) slot.setByPlayer(ItemStack.EMPTY) else slot.setChanged()
        return copy
    }

    override fun stillValid(player: Player) =
        stillValid(ContainerLevelAccess.create(be.level!!, be.blockPos), player, be.blockState.block)
}