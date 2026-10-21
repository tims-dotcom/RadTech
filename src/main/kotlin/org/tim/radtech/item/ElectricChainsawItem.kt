package org.tim.radtech.item

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.tim.radtech.energy.PowerFormat
import java.util.ArrayDeque
import kotlin.math.roundToInt

class ElectricChainsawItem(props: Properties) : Item(props.stacksTo(1)), IElectricItem {

    override val capacity: Long = 250_000L
    private val energyPerLog: Long = 150L

    override fun getDestroySpeed(stack: ItemStack, state: BlockState): Float {
        if (getStored(stack) < energyPerLog) return 1.0f
        return if (state.`is`(BlockTags.MINEABLE_WITH_AXE) || state.`is`(BlockTags.LEAVES)) {
            16.0f
        } else {
            1.0f
        }
    }

    override fun isCorrectToolForDrops(stack: ItemStack, state: BlockState): Boolean {
        if (getStored(stack) < energyPerLog) return false
        return state.`is`(BlockTags.MINEABLE_WITH_AXE) || state.`is`(BlockTags.LEAVES)
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        if (getStored(stack) >= 200L) {
            extract(stack, 200L, false)
            target.level().playSound(null, target.blockPosition(), SoundEvents.BEE_LOOP, SoundSource.PLAYERS, 1.0f, 0.6f)
            target.hurt(target.damageSources().playerAttack(attacker as? Player ?: return true), 9.0f)
        }
        return true
    }

    override fun mineBlock(stack: ItemStack, level: Level, state: BlockState, pos: BlockPos, entity: LivingEntity): Boolean {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0f) {
            extract(stack, energyPerLog, false)

            val player = entity as? Player
            if (player != null && state.`is`(BlockTags.LOGS) && player.isShiftKeyDown) {
                fellTree(level as ServerLevel, pos, player, stack)
            }
        }
        return true
    }

    private fun fellTree(level: ServerLevel, startPos: BlockPos, player: Player, stack: ItemStack) {
        val queue = ArrayDeque<BlockPos>()
        val visited = HashSet<BlockPos>()
        queue.add(startPos)
        visited.add(startPos)

        var felled = 0
        val maxLogs = 48

        while (queue.isNotEmpty() && felled < maxLogs) {
            val curr = queue.poll()
            for (dx in -1..1) {
                for (dy in 0..1) {
                    for (dz in -1..1) {
                        val next = curr.offset(dx, dy, dz)
                        if (visited.add(next)) {
                            val nextState = level.getBlockState(next)
                            if (nextState.`is`(BlockTags.LOGS)) {
                                if (getStored(stack) < energyPerLog) return
                                if (level.destroyBlock(next, true, player)) {
                                    extract(stack, energyPerLog, false)
                                    felled++
                                    queue.add(next)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun isBarVisible(stack: ItemStack): Boolean = true
    override fun getBarWidth(stack: ItemStack): Int =
        ((getStored(stack).toFloat() / capacity) * 13f).roundToInt().coerceIn(0, 13)
    override fun getBarColor(stack: ItemStack): Int = 0x00E5FF

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        val stored = getStored(stack)
        tooltipComponents.add(Component.literal("§aЗаряд: §f${PowerFormat.energy(stored)} / ${PowerFormat.energy(capacity)}"))
        tooltipComponents.add(Component.literal("§7Валка дерева: §fЗрубує все дерево зі Shift"))
        tooltipComponents.add(Component.literal("§cУрон у ближньому бою: 9"))
    }
}
