package org.tim.radtech.energy

interface IPowerStorage {
    val tier: VoltageTier
    val stored: Long
    val capacity: Long
    val maxInputAmps: Int
    val maxOutputAmps: Int

    val maxInputPerTick: Long get() = tier.volts.toLong() * maxInputAmps
    val maxOutputPerTick: Long get() = tier.volts.toLong() * maxOutputAmps

    fun receive(amount: Long, simulate: Boolean): Long
    fun extract(amount: Long, simulate: Boolean): Long
}