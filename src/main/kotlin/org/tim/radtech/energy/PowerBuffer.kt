package org.tim.radtech.energy

import net.minecraft.nbt.CompoundTag

class PowerBuffer(
    override val tier: VoltageTier,
    override val capacity: Long,
    override val maxInputAmps: Int,
    override val maxOutputAmps: Int
) : IPowerStorage {

    private var energy = 0L

    override val stored: Long get() = energy

    override fun receive(amount: Long, simulate: Boolean): Long {
        val accepted = minOf(amount, maxInputPerTick, capacity - energy).coerceAtLeast(0)
        if (!simulate) energy += accepted
        return accepted
    }

    override fun extract(amount: Long, simulate: Boolean): Long {
        val taken = minOf(amount, maxOutputPerTick, energy).coerceAtLeast(0)
        if (!simulate) energy -= taken
        return taken
    }

    fun fill(amount: Long): Long {
        val added = minOf(amount, capacity - energy).coerceAtLeast(0)
        energy += added
        return added
    }

    fun consume(amount: Long): Boolean {
        if (energy < amount) return false
        energy -= amount
        return true
    }

    fun save(tag: CompoundTag) = tag.putLong(TAG, energy)

    fun load(tag: CompoundTag) {
        energy = tag.getLong(TAG).coerceIn(0, capacity)
    }

    private companion object {
        const val TAG = "Energy"
    }
}