package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.damagesource.DamageSources
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import org.tim.radtech.energy.CableType
import org.tim.radtech.energy.PowerCapabilities
import org.tim.radtech.energy.PowerNetworks

class BareCableBlock(val type: CableType, props: BlockBehaviour.Properties) : BaseEntityBlock(props) {

    init {
        registerDefaultState(Direction.entries.fold(stateDefinition.any()) { state, dir ->
            state.setValue(property(dir), false)
        })
    }

    override fun codec() = throw UnsupportedOperationException("BareCableBlock has no codec")

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        Direction.entries.forEach { builder.add(property(it)) }
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? =
        Direction.entries.fold(defaultBlockState()) { state, dir ->
            val neighborPos = ctx.clickedPos.relative(dir)
            state.setValue(property(dir), connects(ctx.level, ctx.level.getBlockState(neighborPos), neighborPos, dir))
        }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        level: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState = state.setValue(property(direction), connects(level, neighborState, neighborPos, direction))

    override fun neighborChanged(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        neighborBlock: Block,
        neighborPos: BlockPos,
        movedByPiston: Boolean
    ) {
        if (!level.isClientSide) PowerNetworks.markDirty(level)
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        SHAPES[maskOf(state)]

    override fun getRenderShape(state: BlockState) = RenderShape.MODEL

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = CableBlockEntity(pos, state)

    // Бʼє струмом гравця коли він торкається голого кабелю
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (level.isClientSide) return
        if (entity !is LivingEntity) return
        if (level.gameTime % 10 != 0L) return // кожні 10 тіків, не кожен тік
        entity.hurt(level.damageSources().lightningBolt(), 4.0f) // 2 серця = 4 HP
    }

    private fun connects(level: LevelAccessor, neighborState: BlockState, neighborPos: BlockPos, dir: Direction): Boolean =
        neighborState.block is CableBlock ||
        neighborState.block is BareCableBlock ||
        (level as? Level)?.getCapability(PowerCapabilities.POWER, neighborPos, dir.opposite) != null

    companion object {
        private val PROPERTIES: Map<Direction, BooleanProperty> = mapOf(
            Direction.NORTH to BlockStateProperties.NORTH,
            Direction.EAST  to BlockStateProperties.EAST,
            Direction.SOUTH to BlockStateProperties.SOUTH,
            Direction.WEST  to BlockStateProperties.WEST,
            Direction.UP    to BlockStateProperties.UP,
            Direction.DOWN  to BlockStateProperties.DOWN
        )

        private val CORE: VoxelShape = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0)

        private val SHAPES: Array<VoxelShape> = Array(1 shl 6) { mask ->
            Direction.entries.foldIndexed(CORE) { i, shape, dir ->
                if (mask and (1 shl i) != 0) Shapes.or(shape, arm(dir)) else shape
            }
        }

        fun property(dir: Direction): BooleanProperty = PROPERTIES.getValue(dir)

        private fun maskOf(state: BlockState): Int =
            Direction.entries.foldIndexed(0) { i, mask, dir ->
                if (state.getValue(property(dir))) mask or (1 shl i) else mask
            }

        private fun arm(dir: Direction): VoxelShape = when (dir) {
            Direction.NORTH -> Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0)
            Direction.SOUTH -> Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0)
            Direction.WEST  -> Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0)
            Direction.EAST  -> Block.box(10.0, 6.0, 6.0, 16.0, 10.0, 10.0)
            Direction.DOWN  -> Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0)
            Direction.UP    -> Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0)
        }
    }
}
