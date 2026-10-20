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
import org.tim.radtech.block.CircuitBreakerBlockEntity
import org.tim.radtech.registry.ModMenus

class CircuitBreakerMenu(
    id: Int,
    inv: Inventory,
    val be: CircuitBreakerBlockEntity,
    val data: ContainerData
) : AbstractContainerMenu(ModMenus.CIRCUIT_BREAKER.get(), id) {

    constructor(id: Int, inv: Inventory, buf: FriendlyByteBuf) :
            this(
                id,
                inv,
                inv.player.level().getBlockEntity(buf.readBlockPos()) as CircuitBreakerBlockEntity,
                SimpleContainerData(3)
            )

    val isTripped: Boolean get() = data.get(0) == 1
    val tripLimitAmps: Int get() = data.get(1)
    val currentAmps: Double get() = data.get(2) / 10.0

    init {
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

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        // id 0: toggle Reset (ввімкнути)
        // id 1: 16A, id 2: 32A, id 3: 66A, id 4: 120A
        when (id) {
            0 -> data.set(0, 0) // reset trip
            1 -> data.set(1, 16)
            2 -> data.set(1, 32)
            3 -> data.set(1, 66)
            4 -> data.set(1, 120)
        }
        return true
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY

    override fun stillValid(player: Player): Boolean =
        stillValid(ContainerLevelAccess.create(be.level!!, be.blockPos), player, be.blockState.block)
}
