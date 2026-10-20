package org.tim.radtech.menu

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.SlotItemHandler
import org.tim.radtech.block.ThermalGeneratorBlockEntity
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.registry.ModMenus

class ThermalGeneratorMenu(id: Int, inv: Inventory, val be: ThermalGeneratorBlockEntity, val data: ContainerData) :
    AbstractContainerMenu(ModMenus.THERMAL_GENERATOR.get(), id) {

    constructor(id: Int, inv: Inventory, buf: FriendlyByteBuf) :
            this(
                id,
                inv,
                inv.player.level().getBlockEntity(buf.readBlockPos()) as ThermalGeneratorBlockEntity,
                SimpleContainerData(ThermalGeneratorBlockEntity.DATA_COUNT)
            )

    val isBurning: Boolean get() = data.get(ThermalGeneratorBlockEntity.BURN_TIME) > 0

    val flameHeight: Int
        get() {
            val duration = data.get(ThermalGeneratorBlockEntity.BURN_DURATION).coerceAtLeast(1)
            return (data.get(ThermalGeneratorBlockEntity.BURN_TIME) * FLAME_SIZE + duration - 1) / duration
        }

    val stored: Long get() = PowerSync.stored(data, ThermalGeneratorBlockEntity.POWER_OFFSET)
    val capacity: Long get() = PowerSync.capacity(data, ThermalGeneratorBlockEntity.POWER_OFFSET)

    init {
        addSlot(SlotItemHandler(be.items, 0, 80, 53))
        for (r in 0..2) for (c in 0..8) addSlot(Slot(inv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18))
        for (c in 0..8) addSlot(Slot(inv, c, 8 + c * 18, 142))
        addDataSlots(data)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val slot = slots[index]
        if (!slot.hasItem()) return ItemStack.EMPTY
        val stack = slot.item
        val copy = stack.copy()
        val moved = if (index == 0) moveItemStackTo(stack, 1, slots.size, true)
        else moveItemStackTo(stack, 0, 1, false)
        if (!moved) return ItemStack.EMPTY
        if (stack.isEmpty) slot.setByPlayer(ItemStack.EMPTY) else slot.setChanged()
        return copy
    }

    override fun stillValid(player: Player) =
        stillValid(ContainerLevelAccess.create(be.level!!, be.blockPos), player, be.blockState.block)

    companion object {
        const val FLAME_SIZE = 14
    }
}