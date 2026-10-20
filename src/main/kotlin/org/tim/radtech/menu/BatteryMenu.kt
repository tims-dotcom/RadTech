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
import org.tim.radtech.block.BatteryBlockEntity
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.registry.ModMenus

class BatteryMenu(id: Int, inv: Inventory, val be: BatteryBlockEntity, val data: ContainerData) :
    AbstractContainerMenu(ModMenus.BATTERY.get(), id) {

    constructor(id: Int, inv: Inventory, buf: FriendlyByteBuf) :
            this(
                id,
                inv,
                inv.player.level().getBlockEntity(buf.readBlockPos()) as BatteryBlockEntity,
                SimpleContainerData(DATA_COUNT)
            )

    val stored: Long get() = PowerSync.stored(data, 0)
    val capacity: Long get() = PowerSync.capacity(data, 0)
    val amps: Double get() = data.get(4) / 10.0
    val heatTicks: Int get() = data.get(5)
    val isOverheated: Boolean get() = data.get(6) == 1

    init {
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

    override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY

    override fun stillValid(player: Player): Boolean =
        stillValid(ContainerLevelAccess.create(be.level!!, be.blockPos), player, be.blockState.block)

    companion object {
        const val DATA_COUNT = 7
    }
}
