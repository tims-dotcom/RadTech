package org.tim.radtech.item

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import org.tim.radtech.Radtech
import org.tim.radtech.energy.IWireConnectable

class WireCutterItem(props: Properties) : Item(props) {

    override fun useOn(ctx: UseOnContext): InteractionResult {
        val level = ctx.level
        val pos = ctx.clickedPos
        val player = ctx.player ?: return InteractionResult.PASS
        val be = level.getBlockEntity(pos) as? IWireConnectable ?: return InteractionResult.PASS

        if (be.connections.isEmpty()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("§7[RadTech] До цього конектора не під'єднано жодного дроту."), true)
            }
            return InteractionResult.FAIL
        }

        if (level.isClientSide) return InteractionResult.SUCCESS

        val state = level.getBlockState(pos)

        // Якщо затиснуто Shift або Ctrl — мінімальне провисання (ідеально рівна струна)
        if (player.isShiftKeyDown) {
            val targetSag = 0.0f
            for (conn in be.connections) {
                conn.sag = targetSag
                val other = level.getBlockEntity(conn.targetPos) as? IWireConnectable
                other?.setSag(pos, targetSag)
            }
            level.sendBlockUpdated(pos, state, state, 3)
            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0f, 1.4f)
            player.displayClientMessage(
                Component.literal("§b✂ [RadTech] §fНатяг дроту: §a0% §f(Ідеально рівна струна як струна!)"),
                true
            )
            return InteractionResult.SUCCESS
        }

        // Звичайний ПКМ — перемикання натягу / провисання по колу: 0.0 -> 0.35 -> 0.75 -> 0.0
        val currentSag = be.connections.first().sag
        val nextSag = when {
            currentSag < 0.1f -> 0.35f
            currentSag < 0.5f -> 0.75f
            else -> 0.0f
        }

        for (conn in be.connections) {
            conn.sag = nextSag
            val other = level.getBlockEntity(conn.targetPos) as? IWireConnectable
            other?.setSag(pos, nextSag)
        }
        level.sendBlockUpdated(pos, state, state, 3)
        level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0f, 1.0f)

        val desc = when (nextSag) {
            0.0f -> "§a0% (Рівна пряма струна)"
            0.35f -> "§e35% (Помірне природне провисання)"
            else -> "§675% (Глибоке атмосферне провисання ЛЕП)"
        }
        player.displayClientMessage(
            Component.literal("§e✂ [RadTech] §fНатяг дроту: $desc"),
            true
        )

        return InteractionResult.SUCCESS
    }

    // Допоміжний метод для демонтажу дротів
    companion object {
        fun cutWires(level: net.minecraft.world.level.Level, pos: BlockPos, be: IWireConnectable) {
            if (level.isClientSide) return
            val state = level.getBlockState(pos)
            for (conn in be.connections) {
                val other = level.getBlockEntity(conn.targetPos) as? IWireConnectable
                other?.removeConnection(pos)
                val coil = if (conn.isBare) Radtech.BARE_WIRE_COILS[conn.type]?.get() else Radtech.WIRE_COILS[conn.type]?.get()
                if (coil != null) {
                    Containers.dropItemStack(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, ItemStack(coil))
                }
            }
            be.connections.clear()
            level.sendBlockUpdated(pos, state, state, 3)
            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 0.8f)
        }
    }
}
