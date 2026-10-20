package org.tim.radtech.energy

enum class VoltageTier(val volts: Int) {
    INDUSTRIAL(380),
    DISTRIBUTION(10_000),
    TRANSMISSION(110_000)
}
