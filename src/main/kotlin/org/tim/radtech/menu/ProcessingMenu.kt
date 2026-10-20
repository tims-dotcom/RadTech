package org.tim.radtech.menu

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.SlotItemHandler
import org.tim.radtech.block.ProcessingMachineBlockEntity
import org.tim.radtech.energy.PowerSync

class ProcessingMenu(
    type: MenuType<*>,
    id: Int,
    inv: Inventory,
    val be: ProcessingMachineBlockEntity,
    val data: ContainerData
) : AbstractContainerMenu(type, id) {

    constructor(type: MenuType<*>, id: Int, inv: Inventory, buf: FriendlyByteBuf) :
            this(
                type,
                id,
                inv,
                inv.player.level().getBlockEntity(buf.readBlockPos()) as ProcessingMachineBlockEntity,
                SimpleContainerData(ProcessingMachineBlockEntity.DATA_COUNT)
            )

    val progress: Int get() = data.get(ProcessingMachineBlockEntity.PROGRESS)
    val maxProgress: Int get() = data.get(ProcessingMachineBlockEntity.MAX_PROGRESS).coerceAtLeast(1)
    val stored: Long get() = PowerSync.stored(data, ProcessingMachineBlockEntity.POWER_OFFSET)
    val capacity: Long get() = PowerSync.capacity(data, ProcessingMachineBlockEntity.POWER_OFFSET)

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
