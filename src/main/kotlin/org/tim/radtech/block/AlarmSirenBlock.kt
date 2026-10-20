package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.DirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import org.tim.radtech.registry.ModBlockEntities

class AlarmSirenBlock(props: BlockBehaviour.Properties) : BaseEntityBlock(props) {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ALARM, false))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, ALARM)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? =
        defaultBlockState()
            .setValue(FACING, ctx.clickedFace)
            .setValue(ALARM, ctx.level.hasNeighborSignal(ctx.clickedPos))

    override fun neighborChanged(state: BlockState, level: Level, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos, movedByPiston: Boolean) {
        if (!level.isClientSide) {
            val hasSignal = level.hasNeighborSignal(pos)
            if (state.getValue(ALARM) != hasSignal) {
                level.setBlock(pos, state.setValue(ALARM, hasSignal), 3)
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston)
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = AlarmSirenBlockEntity(pos, state)

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? =
        createTickerHelper(type, ModBlockEntities.ALARM_SIREN.get()) { l, p, s, be -> AlarmSirenBlockEntity.tick(l, p, s, be) }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return when (state.getValue(FACING)) {
            Direction.UP    -> SHAPE_FLOOR
            Direction.DOWN  -> SHAPE_CEILING
            Direction.NORTH -> SHAPE_NORTH
            Direction.SOUTH -> SHAPE_SOUTH
            Direction.WEST  -> SHAPE_WEST
            Direction.EAST  -> SHAPE_EAST
        }
    }

    companion object {
        val FACING = DirectionalBlock.FACING
        val ALARM: BooleanProperty = BooleanProperty.create("alarm")
        val CODEC: MapCodec<AlarmSirenBlock> = simpleCodec { AlarmSirenBlock(it) }

        // Bounding box з моделі ext_lamb.json: X: 2..14, Y: 0..6, Z: 5..11
        private val SHAPE_FLOOR   = Block.box(2.0, 0.0, 5.0, 14.0, 6.0, 11.0)
        private val SHAPE_CEILING = Block.box(2.0, 10.0, 5.0, 14.0, 16.0, 11.0)
        private val SHAPE_NORTH   = Block.box(2.0, 5.0, 10.0, 14.0, 11.0, 16.0)
        private val SHAPE_SOUTH   = Block.box(2.0, 5.0, 0.0, 14.0, 11.0, 6.0)
        private val SHAPE_WEST    = Block.box(10.0, 5.0, 2.0, 16.0, 11.0, 14.0)
        private val SHAPE_EAST    = Block.box(0.0, 5.0, 2.0, 6.0, 11.0, 14.0)
    }
}
