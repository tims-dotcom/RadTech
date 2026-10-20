package org.tim.radtech.energy

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

object PowerTransfer {
    fun push(level: Level, pos: BlockPos, source: IPowerStorage): Long {
        var budget = source.maxOutputPerTick
        var moved = 0L
        for (side in Direction.entries) {
            if (budget <= 0) break
            val target = level.getCapability(PowerCapabilities.POWER, pos.relative(side), side.opposite) ?: continue
            if (target.tier != source.tier) continue
            val accepted = target.receive(source.extract(budget, true), true)
            if (accepted <= 0) continue
            val sent = source.extract(accepted, false)
            target.receive(sent, false)
            budget -= sent
            moved += sent
        }
        return moved
    }
}