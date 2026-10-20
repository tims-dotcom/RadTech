package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.Containers
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.DirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import org.tim.radtech.Radtech
import org.tim.radtech.energy.PowerNetworks

class ConnectorBlock(props: BlockBehaviour.Properties) : BaseEntityBlock(props) {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? =
        defaultBlockState().setValue(FACING, ctx.clickedFace)

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ConnectorBlockEntity(pos, state)

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        if (!level.isClientSide && !state.`is`(oldState.block)) {
            PowerNetworks.registerConnectable(level, pos)
        }
        super.onPlace(state, level, pos, oldState, isMoving)
    }

    override fun neighborChanged(state: BlockState, level: Level, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos, movedByPiston: Boolean) {
        if (!level.isClientSide) {
            PowerNetworks.markDirty(level)
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston)
    }

    override fun updateShape(state: BlockState, direction: Direction, neighborState: BlockState, level: LevelAccessor, pos: BlockPos, neighborPos: BlockPos): BlockState {
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean) {
        if (!state.`is`(newState.block)) {
            val be = level.getBlockEntity(pos) as? ConnectorBlockEntity
            if (be != null && !level.isClientSide) {
                // Від'єднуємо сусідів та повертаємо котушки дроту
                for (conn in be.connections) {
                    val other = level.getBlockEntity(conn.targetPos) as? ConnectorBlockEntity
                    other?.removeConnection(pos)
                    val coilItem = if (conn.isBare) {
                        Radtech.BARE_WIRE_COILS[conn.type]?.get()
                    } else {
                        Radtech.WIRE_COILS[conn.type]?.get()
                    }
                    if (coilItem != null) {
                        Containers.dropItemStack(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, ItemStack(coilItem))
                    }
                }
                PowerNetworks.unregisterConnectable(level, pos)
                PowerNetworks.markDirty(level)
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return when (state.getValue(FACING)) {
            Direction.DOWN  -> SHAPE_DOWN
            Direction.UP    -> SHAPE_UP
            Direction.NORTH -> SHAPE_NORTH
            Direction.SOUTH -> SHAPE_SOUTH
            Direction.WEST  -> SHAPE_WEST
            Direction.EAST  -> SHAPE_EAST
        }
    }

    companion object {
        val FACING = DirectionalBlock.FACING
        val CODEC: MapCodec<ConnectorBlock> = simpleCodec { ConnectorBlock(it) }

        // Акуратний порцеляновий ізолятор 6x6x6 на поверхні
        private val SHAPE_UP    = Block.box(5.0, 0.0, 5.0, 11.0, 8.0, 11.0)
        private val SHAPE_DOWN  = Block.box(5.0, 8.0, 5.0, 11.0, 16.0, 11.0)
        private val SHAPE_NORTH = Block.box(5.0, 5.0, 8.0, 11.0, 11.0, 16.0)
        private val SHAPE_SOUTH = Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 8.0)
        private val SHAPE_WEST  = Block.box(8.0, 5.0, 5.0, 16.0, 11.0, 11.0)
        private val SHAPE_EAST  = Block.box(0.0, 5.0, 5.0, 8.0, 11.0, 11.0)
    }
}
