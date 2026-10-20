package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
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

class CableBlock(val type: CableType, props: BlockBehaviour.Properties) : BaseEntityBlock(props) {

    init {
        registerDefaultState(Direction.entries.fold(stateDefinition.any()) { state, dir ->
            state.setValue(property(dir), false)
        })
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

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

    private fun connects(level: LevelAccessor, neighborState: BlockState, neighborPos: BlockPos, dir: Direction): Boolean =
        neighborState.block is CableBlock ||
        neighborState.block is BareCableBlock ||
                (level as? Level)?.getCapability(PowerCapabilities.POWER, neighborPos, dir.opposite) != null

    companion object {
        val CODEC: MapCodec<CableBlock> = RecordCodecBuilder.mapCodec<CableBlock> { instance ->
            instance.group(
                CableType.CODEC.fieldOf("type").forGetter { it.type },
                propertiesCodec<CableBlock>()
            ).apply(instance) { type, props -> CableBlock(type, props) }
        }

        private val PROPERTIES: Map<Direction, BooleanProperty> = mapOf(
            Direction.NORTH to BlockStateProperties.NORTH,
            Direction.EAST to BlockStateProperties.EAST,
            Direction.SOUTH to BlockStateProperties.SOUTH,
            Direction.WEST to BlockStateProperties.WEST,
            Direction.UP to BlockStateProperties.UP,
            Direction.DOWN to BlockStateProperties.DOWN
        )

        private val CORE: VoxelShape = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0)

        private val SHAPES: Array<VoxelShape> = Array(1 shl 6) { mask ->
            Direction.entries.foldIndexed(CORE) { i, shape, dir ->
                if (mask and (1 shl i) != 0) Shapes.or(shape, arm(dir)) else shape
            }
        }

        private fun property(dir: Direction): BooleanProperty = PROPERTIES.getValue(dir)

        private fun maskOf(state: BlockState): Int =
            Direction.entries.foldIndexed(0) { i, mask, dir ->
                if (state.getValue(property(dir))) mask or (1 shl i) else mask
            }

        private fun arm(dir: Direction): VoxelShape = when (dir) {
            Direction.NORTH -> Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0)
            Direction.SOUTH -> Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0)
            Direction.WEST -> Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0)
            Direction.EAST -> Block.box(10.0, 6.0, 6.0, 16.0, 10.0, 10.0)
            Direction.DOWN -> Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0)
            Direction.UP -> Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0)
        }
    }
}
