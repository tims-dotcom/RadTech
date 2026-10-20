package org.tim.radtech.energy

import com.mojang.serialization.Codec
import net.minecraft.util.StringRepresentable

enum class CableType(val id: String, val maxAmps: Int) : StringRepresentable {
    TIN("tin", 16),
    COPPER("copper", 30),
    ALUMINUM("aluminum", 66),
    IRON("iron", 120);

    val tier: VoltageTier get() = VoltageTier.INDUSTRIAL
    val maxPerTick: Long get() = tier.volts.toLong() * maxAmps

    override fun getSerializedName(): String = id

    companion object {
        val CODEC: Codec<CableType> = StringRepresentable.fromEnum(CableType::values)

        // Пари: голий тип -> ізольований
        val BARE_IDS = values().map { "bare_${it.id}" }
    }
}
