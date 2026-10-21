package org.tim.radtech.menu

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.SlotItemHandler
import org.tim.radtech.block.QuarryBlockEntity
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.registry.ModMenus

class QuarryMenu(
    id: Int,
    inv: Inventory,
    val be: QuarryBlockEntity,
    val data: ContainerData
) : AbstractContainerMenu(ModMenus.QUARRY.get(), id) {

    constructor(id: Int, inv: Inventory, buf: FriendlyByteBuf) :
            this(
                id,
                inv,
                inv.player.level().getBlockEntity(buf.readBlockPos()) as QuarryBlockEntity,
                SimpleContainerData(8)
            )

    val stored: Long get() = PowerSync.stored(data, 0)
    val capacity: Long get() = PowerSync.capacity(data, 0)
    val currentY: Int get() = data.get(4) - 100
    val blocksMined: Int get() = data.get(5)
    val isFormed: Boolean get() = data.get(6) == 1
    val isMining: Boolean get() = data.get(7) == 1

    init {
        // 18 слотів буфера видобутку (2x9)
        for (r in 0..1) {
            for (c in 0..8) {
                addSlot(SlotItemHandler(be.items, c + r * 9, 8 + c * 18, 54 + r * 18))
            }
        }

        // Інвентар гравця
        for (r in 0..2) {
            for (c in 0..8) {
                addSlot(Slot(inv, c + r * 9 + 9, 8 + c * 18, 98 + r * 18))
            }
        }
        for (c in 0..8) {
            addSlot(Slot(inv, c, 8 + c * 18, 156))
        }

        addDataSlots(data)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var result = ItemStack.EMPTY
        val slot = slots[index]
        if (slot.hasItem()) {
            val stack = slot.item
            result = stack.copy()
            if (index < 18) {
                if (!moveItemStackTo(stack, 18, slots.size, true)) return ItemStack.EMPTY
            } else {
                if (!moveItemStackTo(stack, 0, 18, false)) return ItemStack.EMPTY
            }
            if (stack.isEmpty) slot.set(ItemStack.EMPTY) else slot.setChanged()
        }
        return result
    }

    override fun stillValid(player: Player): Boolean =
        stillValid(ContainerLevelAccess.create(be.level!!, be.blockPos), player, be.blockState.block)
}
