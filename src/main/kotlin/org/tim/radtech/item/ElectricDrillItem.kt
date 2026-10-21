package org.tim.radtech.item

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import org.tim.radtech.energy.PowerFormat
import kotlin.math.roundToInt

class ElectricDrillItem(props: Properties) : Item(props.stacksTo(1)), IElectricItem {

    override val capacity: Long = 250_000L
    private val energyPerBlock: Long = 200L

    fun is3x3(stack: ItemStack): Boolean {
        val data = stack.get(DataComponents.CUSTOM_DATA) ?: return false
        return data.copyTag().getBoolean("Mode3x3")
    }

    fun set3x3(stack: ItemStack, value: Boolean) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack) { tag ->
            tag.putBoolean("Mode3x3", value)
        }
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        if (player.isShiftKeyDown) {
            if (!level.isClientSide) {
                val newMode = !is3x3(stack)
                set3x3(stack, newMode)
                val msg = if (newMode) "§6⚡ Бур: §cРежим 3×3 (Тунельний)" else "§6⚡ Бур: §aРежим 1×1 (Точний)"
                player.displayClientMessage(Component.literal(msg), true)
                level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.6f, 1.5f)
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
        }
        return super.use(level, player, hand)
    }

    override fun getDestroySpeed(stack: ItemStack, state: BlockState): Float {
        if (getStored(stack) < energyPerBlock) return 1.0f
        return if (state.`is`(BlockTags.MINEABLE_WITH_PICKAXE) || state.`is`(BlockTags.MINEABLE_WITH_SHOVEL)) {
            14.0f
        } else {
            1.0f
        }
    }

    override fun isCorrectToolForDrops(stack: ItemStack, state: BlockState): Boolean {
        if (getStored(stack) < energyPerBlock) return false
        return state.`is`(BlockTags.MINEABLE_WITH_PICKAXE) || state.`is`(BlockTags.MINEABLE_WITH_SHOVEL)
    }

    override fun mineBlock(stack: ItemStack, level: Level, state: BlockState, pos: BlockPos, entity: LivingEntity): Boolean {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0f) {
            extract(stack, energyPerBlock, false)

            val player = entity as? ServerPlayer
            if (player != null && is3x3(stack) && getStored(stack) >= energyPerBlock * 8) {
                mine3x3(level, pos, player, stack)
            }
        }
        return true
    }

    private fun mine3x3(level: Level, center: BlockPos, player: ServerPlayer, stack: ItemStack) {
        // Визначаємо площину копання за напрямком погляду гравця
        val lookDir = player.direction
        val pitch = player.xRot

        val positions = ArrayList<BlockPos>()
        if (pitch > 55.0f || pitch < -55.0f) {
            // Дивиться вгору/вниз — копаємо в площині XZ
            for (dx in -1..1) {
                for (dz in -1..1) {
                    if (dx == 0 && dz == 0) continue
                    positions.add(center.offset(dx, 0, dz))
                }
            }
        } else if (lookDir.axis == Direction.Axis.X) {
            // Дивиться на схід/захід — копаємо в площині YZ
            for (dy in -1..1) {
                for (dz in -1..1) {
                    if (dy == 0 && dz == 0) continue
                    positions.add(center.offset(0, dy, dz))
                }
            }
        } else {
            // Дивиться на північ/південь — копаємо в площині XY
            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) continue
                    positions.add(center.offset(dx, dy, 0))
                }
            }
        }

        for (p in positions) {
            if (getStored(stack) < energyPerBlock) break
            val targetState = level.getBlockState(p)
            if (targetState.getDestroySpeed(level, p) >= 0.0f &&
                (targetState.`is`(BlockTags.MINEABLE_WITH_PICKAXE) || targetState.`is`(BlockTags.MINEABLE_WITH_SHOVEL))) {
                if (level.destroyBlock(p, true, player)) {
                    extract(stack, energyPerBlock, false)
                }
            }
        }
    }

    override fun isBarVisible(stack: ItemStack): Boolean = true
    override fun getBarWidth(stack: ItemStack): Int =
        ((getStored(stack).toFloat() / capacity) * 13f).roundToInt().coerceIn(0, 13)
    override fun getBarColor(stack: ItemStack): Int = 0x00FF88

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        val stored = getStored(stack)
        tooltipComponents.add(Component.literal("§aЗаряд: §f${PowerFormat.energy(stored)} / ${PowerFormat.energy(capacity)}"))
        val mode = if (is3x3(stack)) "§c3×3 (Тунельний)" else "§a1×1 (Точний)"
        tooltipComponents.add(Component.literal("§7Режим: $mode §8(Shift+ПКМ)"))
        tooltipComponents.add(Component.literal("§8Ефективний для кайла та лопати"))
    }
}
