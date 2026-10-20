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
import org.tim.radtech.block.LargeThermalGeneratorBlockEntity
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.registry.ModMenus

class LargeThermalGeneratorMenu(
    id: Int,
    inv: Inventory,
    val be: LargeThermalGeneratorBlockEntity,
    val data: ContainerData
) : AbstractContainerMenu(ModMenus.LARGE_THERMAL_GEN.get(), id) {

    constructor(id: Int, inv: Inventory, buf: FriendlyByteBuf) :
            this(
                id,
                inv,
                inv.player.level().getBlockEntity(buf.readBlockPos()) as LargeThermalGeneratorBlockEntity,
                SimpleContainerData(LargeThermalGeneratorBlockEntity.DATA_COUNT)
            )

    val burnTime: Int get() = data.get(LargeThermalGeneratorBlockEntity.BURN_TIME)
    val maxBurnTime: Int get() = data.get(LargeThermalGeneratorBlockEntity.MAX_BURN_TIME)
    val isFormed: Boolean get() = data.get(LargeThermalGeneratorBlockEntity.IS_FORMED) == 1
    val stored: Long get() = PowerSync.stored(data, LargeThermalGeneratorBlockEntity.POWER_OFFSET)
    val capacity: Long get() = PowerSync.capacity(data, LargeThermalGeneratorBlockEntity.POWER_OFFSET)

    init {
        // 4 слоти для палива (2x2 сітка)
        addSlot(SlotItemHandler(be.items, 0, 96, 26))
        addSlot(SlotItemHandler(be.items, 1, 114, 26))
        addSlot(SlotItemHandler(be.items, 2, 96, 44))
        addSlot(SlotItemHandler(be.items, 3, 114, 44))

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
            if (index < 4) {
                if (!moveItemStackTo(stack, 4, slots.size, true)) return ItemStack.EMPTY
            } else {
                if (!moveItemStackTo(stack, 0, 4, false)) return ItemStack.EMPTY
            }
            if (stack.isEmpty) slot.set(ItemStack.EMPTY) else slot.setChanged()
        }
        return result
    }

    override fun stillValid(player: Player): Boolean =
        stillValid(ContainerLevelAccess.create(be.level!!, be.blockPos), player, be.blockState.block)
}
