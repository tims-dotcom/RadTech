package org.tim.radtech.energy

interface IPowerStorage {
    val tier: VoltageTier
    val stored: Long
    val capacity: Long
    val maxInputAmps: Int
    val maxOutputAmps: Int
    fun receive(amount: Long, simulate: Boolean): Long
    fun extract(amount: Long, simulate: Boolean): Long
}