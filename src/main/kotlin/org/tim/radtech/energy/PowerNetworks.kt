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

    fun registerConnectable(level: Level, pos: BlockPos) = networks(level).registerConnectable(pos)

    fun unregisterConnectable(level: Level, pos: BlockPos) = networks(level).unregisterConnectable(pos)

    fun markDirty(level: Level) = networks(level).markDirty()

    fun getNetwork(level: Level, pos: BlockPos): PowerNetwork? = networks(level).getNetwork(pos)

    fun onLevelTick(event: LevelTickEvent.Post) {
        val level = event.level as? ServerLevel ?: return
        levels[level.dimension()]?.tick(level)
    }

    fun onServerTick(event: ServerTickEvent.Post) = PowerClock.advance()

    fun onServerStopped(event: ServerStoppedEvent) = levels.clear()

    private fun networks(level: Level) = levels.getOrPut(level.dimension()) { LevelNetworks() }

    private class LevelNetworks {
        private val cables = HashMap<BlockPos, CableType>()
        private val connectables = HashSet<BlockPos>()
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

        fun registerConnectable(pos: BlockPos) {
            connectables.add(pos.immutable())
            dirty = true
        }

        fun unregisterConnectable(pos: BlockPos) {
            connectables.remove(pos)
            dirty = true
        }

        fun markDirty() {
            dirty = true
        }

        fun getNetwork(pos: BlockPos): PowerNetwork? =
            networks.firstOrNull { it.cables.contains(pos) }

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
            val allStarts = HashSet<BlockPos>().apply {
                addAll(cables.keys)
                addAll(connectables)
            }

            for (start in allStarts) {
                if (!visited.add(start)) continue
                val queue = ArrayDeque<BlockPos>()
                queue.add(start)
                val endpoints = LinkedHashMap<BlockPos, PowerNetwork.Endpoint>()
                val networkCables = ArrayList<BlockPos>()
                var netTier = VoltageTier.INDUSTRIAL

                while (queue.isNotEmpty()) {
                    val pos = queue.removeFirst()

                    // Обхід через блокові кабелі
                    if (cables.containsKey(pos)) {
                        networkCables.add(pos)
                        netTier = cables.getValue(pos).tier
                        for (dir in Direction.entries) {
                            val next = pos.relative(dir)
                            if (cables.containsKey(next)) {
                                if (visited.add(next)) queue.add(next)
                            } else if (connectables.contains(next)) {
                                if (visited.add(next)) queue.add(next)
                            } else if (level.isLoaded(next) &&
                                level.getCapability(PowerCapabilities.POWER, next, dir.opposite) != null
                            ) {
                                endpoints.putIfAbsent(next, PowerNetwork.Endpoint(next, dir.opposite))
                            }
                        }
                    }

                    // Обхід через підвісні дроти та конектори (IWireConnectable)
                    if (connectables.contains(pos)) {
                        networkCables.add(pos)
                        val be = level.getBlockEntity(pos)
                        if (be is IWireConnectable) {
                            for (conn in be.connections) {
                                val target = conn.targetPos
                                if (level.isLoaded(target) && visited.add(target)) {
                                    queue.add(target)
                                }
                            }
                            val powerPos = be.getConnectedPowerPos()
                            if (powerPos != null && level.isLoaded(powerPos)) {
                                if (powerPos == pos) {
                                    // Сам блок має споживання (наприклад, лампа)
                                    endpoints.putIfAbsent(pos, PowerNetwork.Endpoint(pos, Direction.UP))
                                } else {
                                    for (d in Direction.entries) {
                                        val cap = level.getCapability(PowerCapabilities.POWER, powerPos, d)
                                        if (cap != null) {
                                            endpoints.putIfAbsent(powerPos, PowerNetwork.Endpoint(powerPos, d))
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (endpoints.isNotEmpty() || networkCables.isNotEmpty()) {
                    result += PowerNetwork(netTier, endpoints.values.toList(), networkCables)
                }
            }
            return result
        }
    }
}
