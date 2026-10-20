package org.tim.radtech.item

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import org.tim.radtech.energy.CableType
import org.tim.radtech.energy.IWireConnectable
import org.tim.radtech.energy.WireConnection

class WireCoilItem(val type: CableType, val isBare: Boolean, props: Properties) : Item(props) {

    override fun useOn(ctx: UseOnContext): InteractionResult {
        val level = ctx.level
        val pos = ctx.clickedPos
        val player = ctx.player ?: return InteractionResult.PASS
        val be = level.getBlockEntity(pos) as? IWireConnectable ?: return InteractionResult.PASS

        if (level.isClientSide) return InteractionResult.SUCCESS

        val pData = player.persistentData
        val key = "radtech_wire_first_pos"

        if (!pData.contains(key)) {
            // Запам'ятовуємо першу точку
            pData.putLong(key, pos.asLong())
            player.displayClientMessage(
                Component.literal("§e⚡ [RadTech] §fПерший контакт вибрано: §b[${pos.x}, ${pos.y}, ${pos.z}]§f. Натисніть ПКМ по другому конектору."),
                true
            )
            level.playSound(null, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 1.0f, 1.2f)
            return InteractionResult.SUCCESS
        }

        // Друга точка
        val firstPosLong = pData.getLong(key)
        pData.remove(key)
        val firstPos = BlockPos.of(firstPosLong)

        if (firstPos == pos) {
            player.displayClientMessage(Component.literal("§c[RadTech] Не можна підключити дріт до того самого конектора!"), true)
            return InteractionResult.FAIL
        }

        val firstBe = level.getBlockEntity(firstPos) as? IWireConnectable
        if (firstBe == null) {
            player.displayClientMessage(Component.literal("§c[RadTech] Перший конектор більше не існує!"), true)
            return InteractionResult.FAIL
        }

        if (firstPos.distSqr(pos) > MAX_DISTANCE * MAX_DISTANCE) {
            player.displayClientMessage(Component.literal("§c[RadTech] Занадто велика відстань між конекторами (максимум $MAX_DISTANCE блоків)!"), true)
            return InteractionResult.FAIL
        }

        // Додаємо з'єднання
        firstBe.addConnection(WireConnection(pos, type, isBare, 0.4f))
        be.addConnection(WireConnection(firstPos, type, isBare, 0.4f))

        if (!player.isCreative) {
            ctx.itemInHand.shrink(1)
        }

        level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_CHAIN.value(), SoundSource.BLOCKS, 1.0f, 1.1f)
        val bareStr = if (isBare) "§c(Голий)" else "§a(Ізольований)"
        player.displayClientMessage(
            Component.literal("§a⚡ [RadTech] §fНатягнуто ${type.name} дріт §e(${type.maxAmps} A) $bareStr§f!"),
            true
        )

        return InteractionResult.SUCCESS
    }

    companion object {
        const val MAX_DISTANCE = 32
    }
}
