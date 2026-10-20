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
import org.tim.radtech.block.TransformerBlockEntity
import org.tim.radtech.registry.ModMenus

class TransformerMenu(
    id: Int,
    inv: Inventory,
    val be: TransformerBlockEntity,
    val data: ContainerData
) : AbstractContainerMenu(ModMenus.TRANSFORMER.get(), id) {

    constructor(id: Int, inv: Inventory, buf: FriendlyByteBuf) :
            this(
                id,
                inv,
                inv.player.level().getBlockEntity(buf.readBlockPos()) as TransformerBlockEntity,
                SimpleContainerData(3)
            )

    val mode: Int get() = data.get(0)
    val lowKWh: Int get() = data.get(1)
    val highKWh: Int get() = data.get(2)

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
        if (id == 0) {
            val newMode = if (mode == 0) 1 else 0
            data.set(0, newMode)
            return true
        }
        return false
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY

    override fun stillValid(player: Player): Boolean =
        stillValid(ContainerLevelAccess.create(be.level!!, be.blockPos), player, be.blockState.block)
}
