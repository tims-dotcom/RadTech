package org.tim.radtech.energy

import net.minecraft.world.inventory.ContainerData

object PowerSync {
    const val SLOTS = 4
    private const val SHIFT = 15
    private const val MASK = (1L shl SHIFT) - 1

    fun read(storage: IPowerStorage, index: Int): Int = when (index) {
        0 -> (storage.stored and MASK).toInt()
        1 -> (storage.stored shr SHIFT).toInt()
        2 -> (storage.capacity and MASK).toInt()
        else -> (storage.capacity shr SHIFT).toInt()
    }

    fun stored(data: ContainerData, offset: Int): Long = join(data.get(offset), data.get(offset + 1))

    fun capacity(data: ContainerData, offset: Int): Long = join(data.get(offset + 2), data.get(offset + 3))

    private fun join(low: Int, high: Int): Long = (high.toLong() shl SHIFT) or low.toLong()
}