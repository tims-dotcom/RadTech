package org.tim.radtech.energy

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

object PowerTransfer {
    private val DIRECTIONS = Direction.entries.toTypedArray()

    fun push(level: Level, pos: BlockPos, source: IPowerStorage): Long {
        if (source.stored <= 0L || source.maxOutputPerTick <= 0) return 0L
        var budget = minOf(source.maxOutputPerTick, source.stored)
        var moved = 0L
        for (side in DIRECTIONS) {
            if (budget <= 0L) break
            val target = level.getCapability(PowerCapabilities.POWER, pos.relative(side), side.opposite) ?: continue
            if (target.tier != source.tier) continue
            val accepted = target.receive(source.extract(budget, true), true)
            if (accepted <= 0L) continue
            val sent = source.extract(accepted, false)
            target.receive(sent, false)
            budget -= sent
            moved += sent
        }
        return moved
    }
}
