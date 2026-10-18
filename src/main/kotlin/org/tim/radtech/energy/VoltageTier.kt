package org.tim.radtech.energy

enum class VoltageTier(val volts: Int) {
    LOW(12),
    MEDIUM(120),
    HIGH(480)
}