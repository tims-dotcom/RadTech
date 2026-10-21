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
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.items.ItemStackHandler
import net.neoforged.neoforge.items.SlotItemHandler
import org.tim.radtech.block.BatteryBlockEntity
import org.tim.radtech.block.LithiumBatteryBlockEntity
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.item.IElectricItem
import org.tim.radtech.registry.ModMenus

class BatteryMenu(id: Int, inv: Inventory, val be: BlockEntity, val data: ContainerData) :
    AbstractContainerMenu(ModMenus.BATTERY.get(), id) {

    constructor(id: Int, inv: Inventory, buf: FriendlyByteBuf) :
            this(
                id,
                inv,
                inv.player.level().getBlockEntity(buf.readBlockPos())!!,
                SimpleContainerData(DATA_COUNT)
            )

    val stored: Long get() = PowerSync.stored(data, 0)
    val capacity: Long get() = PowerSync.capacity(data, 0)
    val amps: Double get() = data.get(4) / 10.0
    val heatTicks: Int get() = data.get(5)
    val isOverheated: Boolean get() = data.get(6) == 1

    val items: ItemStackHandler = when (be) {
        is BatteryBlockEntity -> be.items
        is LithiumBatteryBlockEntity -> be.items
        else -> ItemStackHandler(2)
    }

    init {
        // Слоти акумулятора (0: Зарядка ⚡+, 1: Розрядка ⚡−)
        addSlot(SlotItemHandler(items, 0, 146, 25))
        addSlot(SlotItemHandler(items, 1, 146, 47))

        // Інвентар гравця
        for (r in 0..2) {
            for (c in 0..8) {
                addSlot(Slot(inv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18))
            }
        }
        for (c in 0..8) {
            addSlot(Slot(inv, c, 8 + c * 18, 142))
        }
        addDataSlots(data)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var result = ItemStack.EMPTY
        val slot = slots[index]
        if (slot.hasItem()) {
            val stack = slot.item
            result = stack.copy()
            if (index < 2) {
                // Зі слотів акумулятора в інвентар гравця
                if (!moveItemStackTo(stack, 2, slots.size, true)) return ItemStack.EMPTY
            } else {
                // З інвентаря в слоти акумулятора
                if (stack.item is IElectricItem) {
                    if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY
                } else {
                    return ItemStack.EMPTY
                }
            }
            if (stack.isEmpty) slot.set(ItemStack.EMPTY) else slot.setChanged()
        }
        return result
    }

    override fun stillValid(player: Player): Boolean =
        stillValid(ContainerLevelAccess.create(be.level!!, be.blockPos), player, be.blockState.block)

    companion object {
        const val DATA_COUNT = 7
    }
}
