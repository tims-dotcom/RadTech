package org.tim.radtech.energy

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import org.tim.radtech.block.BareCableBlock
import org.tim.radtech.block.CableBlock

class PowerNetwork(
    val tier: VoltageTier,
    val endpoints: List<Endpoint>,
    val cables: List<BlockPos> = emptyList()
) {
    data class Endpoint(val pos: BlockPos, val side: Direction)

    var lastTransferred: Long = 0L
        private set

    fun tick(level: ServerLevel) {
        if (endpoints.size < 2) return

        val storages = endpoints.mapNotNull { ep ->
            if (level.isLoaded(ep.pos)) {
                level.getCapability(PowerCapabilities.POWER, ep.pos, ep.side)
            } else null
        }.filter { it.tier == tier }

        val sources = storages.filter { it.maxOutputAmps > 0 && it.stored > 0 }
        val sinks = storages.filter { it.maxInputAmps > 0 && it.stored < it.capacity }

        if (sources.isEmpty() || sinks.isEmpty()) return

        var totalTransferred = 0L

        for (source in sources) {
            for (sink in sinks) {
                if (source === sink) continue

                val maxCanExtract = source.extract(Long.MAX_VALUE, true)
                if (maxCanExtract <= 0) break

                val canReceive = sink.receive(maxCanExtract, true)
                if (canReceive <= 0) continue

                val extracted = source.extract(canReceive, false)
                val accepted = sink.receive(extracted, false)
                totalTransferred += accepted

                if (accepted < extracted) {
                    source.receive(extracted - accepted, false)
                }
            }
        }

        lastTransferred = totalTransferred
        val amps = totalTransferred.toDouble() / tier.volts
        if (amps > 16.0) {
            burnOut(level, amps)
        }
    }

    private fun burnOut(level: ServerLevel, amps: Double) {
        for (pos in cables) {
            if (level.isLoaded(pos)) {
                val block = level.getBlockState(pos).block
                val cableType = (block as? CableBlock)?.type ?: (block as? BareCableBlock)?.type ?: continue
                if (amps > cableType.maxAmps) {
                    level.sendParticles(
                        ParticleTypes.FLAME,
                        pos.x + 0.5, pos.y + 0.5, pos.z + 0.5,
                        15, 0.2, 0.2, 0.2, 0.04
                    )
                    level.sendParticles(
                        ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        pos.x + 0.5, pos.y + 0.5, pos.z + 0.5,
                        10, 0.2, 0.2, 0.2, 0.03
                    )
                    level.playSound(
                        null, pos,
                        SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS,
                        1.0f, 1.0f
                    )
                    level.destroyBlock(pos, false)
                }
            }
        }
    }
}
