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
        val limit = tier.volts.toLong() * maxInputAmps
        val accepted = minOf(amount, limit, capacity - energy).coerceAtLeast(0)
        if (!simulate) energy += accepted
        return accepted
    }

    override fun extract(amount: Long, simulate: Boolean): Long {
        val limit = tier.volts.toLong() * maxOutputAmps
        val taken = minOf(amount, limit, energy).coerceAtLeast(0)
        if (!simulate) energy -= taken
        return taken
    }

    fun consume(amount: Long): Boolean {
        if (energy < amount) return false
        energy -= amount
        return true
    }

    fun save(tag: CompoundTag) = tag.putLong("power", energy)

    fun load(tag: CompoundTag) {
        energy = tag.getLong("power").coerceIn(0, capacity)
    }
}