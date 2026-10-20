package org.tim.radtech.energy

import net.minecraft.network.chat.Component
import java.util.Locale

object PowerFormat {
    private const val UNITS_PER_WATT_HOUR = 72_000.0

    fun energy(units: Long): String = scaled(units / UNITS_PER_WATT_HOUR, "watthour")

    fun power(watts: Long): String = scaled(watts.toDouble(), "watt")

    fun voltage(volts: Int): String = scaled(volts.toDouble(), "volt")

    private fun scaled(value: Double, unit: String): String {
        val (divisor, prefix) = when {
            value >= 1_000_000 -> 1_000_000.0 to "mega"
            value >= 1_000 -> 1_000.0 to "kilo"
            else -> 1.0 to ""
        }
        return "${number(value / divisor)} ${Component.translatable("unit.radtech.$prefix$unit").string}"
    }

    private fun number(value: Double): String = when {
        value >= 100 -> "%.0f"
        value >= 10 -> "%.1f"
        else -> "%.2f"
    }.format(Locale.ROOT, value)
}