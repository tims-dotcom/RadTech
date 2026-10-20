package org.tim.radtech.energy

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

object PowerNetworks {
    private val levels = HashMap<ResourceKey<Level>, LevelNetworks>()

    fun register(level: Level, pos: BlockPos, type: CableType) = networks(level).register(pos, type)

    fun unregister(level: Level, pos: BlockPos) = networks(level).unregister(pos)

    fun markDirty(level: Level) = networks(level).markDirty()

    fun onLevelTick(event: LevelTickEvent.Post) {
        val level = event.level as? ServerLevel ?: return
        levels[level.dimension()]?.tick(level)
    }

    fun onServerTick(event: ServerTickEvent.Post) = PowerClock.advance()

    fun onServerStopped(event: ServerStoppedEvent) = levels.clear()

    private fun networks(level: Level) = levels.getOrPut(level.dimension()) { LevelNetworks() }

    private class LevelNetworks {
        private val cables = HashMap<BlockPos, CableType>()
        private var networks: List<PowerNetwork> = emptyList()
        private var dirty = false

        fun register(pos: BlockPos, type: CableType) {
            cables[pos.immutable()] = type
            dirty = true
        }

        fun unregister(pos: BlockPos) {
            cables.remove(pos)
            dirty = true
        }

        fun markDirty() {
            dirty = true
        }

        fun tick(level: ServerLevel) {
            if (dirty) {
                networks = rebuild(level)
                dirty = false
            }
            networks.forEach { it.tick(level) }
        }

        private fun rebuild(level: Level): List<PowerNetwork> {
            val visited = HashSet<BlockPos>()
            val result = ArrayList<PowerNetwork>()
            for (start in cables.keys) {
                if (!visited.add(start)) continue
                val queue = ArrayDeque<BlockPos>()
                queue.add(start)
                val endpoints = LinkedHashMap<BlockPos, PowerNetwork.Endpoint>()
                val networkCables = ArrayList<BlockPos>()
                while (queue.isNotEmpty()) {
                    val pos = queue.removeFirst()
                    networkCables.add(pos)
                    for (dir in Direction.entries) {
                        val next = pos.relative(dir)
                        if (cables.containsKey(next)) {
                            if (visited.add(next)) queue.add(next)
                        } else if (level.isLoaded(next) &&
                            level.getCapability(PowerCapabilities.POWER, next, dir.opposite) != null
                        ) {
                            endpoints.putIfAbsent(next, PowerNetwork.Endpoint(next, dir.opposite))
                        }
                    }
                }
                result += PowerNetwork(cables.getValue(start).tier, endpoints.values.toList(), networkCables)
            }
            return result
        }
    }
}
