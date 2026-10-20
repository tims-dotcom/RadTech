package org.tim.radtech.energy

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils

data class WireConnection(
    val targetPos: BlockPos,
    val type: CableType,
    val isBare: Boolean,
    var sag: Float = 0.4f // 0.0f = ідеально рівна струна, 0.4f = помірний натяг, 0.8f = глибоке провисання
) {
    fun serializeNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putInt("tx", targetPos.x)
        tag.putInt("ty", targetPos.y)
        tag.putInt("tz", targetPos.z)
        tag.putString("type", type.id)
        tag.putBoolean("bare", isBare)
        tag.putFloat("sag", sag)
        return tag
    }

    companion object {
        fun deserializeNBT(tag: CompoundTag): WireConnection? {
            if (!tag.contains("tx") || !tag.contains("ty") || !tag.contains("tz")) return null
            val targetPos = BlockPos(tag.getInt("tx"), tag.getInt("ty"), tag.getInt("tz"))
            val typeStr = tag.getString("type")
            val type = CableType.entries.firstOrNull { it.id == typeStr } ?: CableType.COPPER
            val bare = tag.getBoolean("bare")
            val sag = if (tag.contains("sag")) tag.getFloat("sag") else 0.4f
            return WireConnection(targetPos, type, bare, sag)
        }
    }
}
