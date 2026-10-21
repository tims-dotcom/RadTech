package org.tim.radtech.item

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import kotlin.math.min

interface IElectricItem {
    val capacity: Long
    val maxTransfer: Long get() = 10_000L

    fun getStored(stack: ItemStack): Long {
        val data = stack.get(DataComponents.CUSTOM_DATA) ?: return 0L
        return data.copyTag().getLong("Energy")
    }

    fun setStored(stack: ItemStack, amount: Long) {
        val clamped = amount.coerceIn(0L, capacity)
        CustomData.update(DataComponents.CUSTOM_DATA, stack) { tag ->
            tag.putLong("Energy", clamped)
        }
    }

    fun receive(stack: ItemStack, amount: Long, simulate: Boolean): Long {
        val current = getStored(stack)
        val space = capacity - current
        val toAdd = min(space, min(amount, maxTransfer))
        if (!simulate && toAdd > 0) {
            setStored(stack, current + toAdd)
        }
        return toAdd
    }

    fun extract(stack: ItemStack, amount: Long, simulate: Boolean): Long {
        val current = getStored(stack)
        val toTake = min(current, min(amount, maxTransfer))
        if (!simulate && toTake > 0) {
            setStored(stack, current - toTake)
        }
        return toTake
    }
}
