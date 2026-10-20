package org.tim.radtech.energy

object PowerClock {
    var ticks: Long = 0L
        private set

    fun advance() {
        ticks++
    }
}
