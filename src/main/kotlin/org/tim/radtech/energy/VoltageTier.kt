package org.tim.radtech.energy

enum class VoltageTier(val volts: Int) {
    INDUSTRIAL(400),
    DISTRIBUTION(10_000),
    TRANSMISSION(110_000)
}