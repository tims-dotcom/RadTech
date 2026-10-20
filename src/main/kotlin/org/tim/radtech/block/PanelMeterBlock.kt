package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import org.tim.radtech.energy.PowerNetworks

class PanelMeterBlock(props: BlockBehaviour.Properties) : BaseEntityBlock(props) {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? =
        defaultBlockState().setValue(FACING, ctx.horizontalDirection.opposite)

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = PanelMeterBlockEntity(pos, state)

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!level.isClientSide) {
            val facing = state.getValue(FACING)
            val backPos = pos.relative(facing.opposite)
            val net = PowerNetworks.getNetwork(level, backPos) ?: PowerNetworks.getNetwork(level, pos)
            if (net != null) {
                val amps = net.lastTransferred.toDouble() / net.tier.volts
                val kw = net.lastTransferred / 1000.0
                player.displayClientMessage(
                    Component.literal("§b📟 [Вольтметр-Амперметр] §fЛінія: §a${net.tier.volts} В§f, Струм: §e${String.format("%.1f", amps)} А§f, Потужність: §6${String.format("%.2f", kw)} кВт§f."),
                    true
                )
            } else {
                player.displayClientMessage(Component.literal("§7[Вольтметр-Амперметр] Немає підключення до активної мережі."), true)
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return when (state.getValue(FACING)) {
            Direction.NORTH -> SHAPE_NORTH
            Direction.SOUTH -> SHAPE_SOUTH
            Direction.WEST  -> SHAPE_WEST
            Direction.EAST  -> SHAPE_EAST
            else -> SHAPE_NORTH
        }
    }

    companion object {
        val FACING = HorizontalDirectionalBlock.FACING
        val CODEC: MapCodec<PanelMeterBlock> = simpleCodec { PanelMeterBlock(it) }

        private val SHAPE_NORTH = Block.box(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
        private val SHAPE_SOUTH = Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 2.0)
        private val SHAPE_WEST  = Block.box(14.0, 2.0, 2.0, 16.0, 14.0, 14.0)
        private val SHAPE_EAST  = Block.box(0.0, 2.0, 2.0, 2.0, 14.0, 14.0)
    }
}
