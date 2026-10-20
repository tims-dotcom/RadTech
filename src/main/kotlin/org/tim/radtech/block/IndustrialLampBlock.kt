package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.Containers
import net.minecraft.world.item.ItemStack
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import org.tim.radtech.Radtech
import org.tim.radtech.energy.PowerNetworks
import org.tim.radtech.registry.ModBlockEntities

class IndustrialLampBlock(props: BlockBehaviour.Properties) : BaseEntityBlock(props) {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(LIT, false))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, LIT)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? =
        defaultBlockState().setValue(FACING, ctx.clickedFace)

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = IndustrialLampBlockEntity(pos, state)

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        if (!level.isClientSide && !state.`is`(oldState.block)) {
            PowerNetworks.registerConnectable(level, pos)
        }
        super.onPlace(state, level, pos, oldState, isMoving)
    }

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? =
        createTickerHelper(type, ModBlockEntities.LAMP.get()) { l, p, s, be -> IndustrialLampBlockEntity.tick(l, p, s, be) }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean) {
        if (!state.`is`(newState.block)) {
            val be = level.getBlockEntity(pos) as? IndustrialLampBlockEntity
            if (be != null && !level.isClientSide) {
                for (conn in be.connections) {
                    val other = level.getBlockEntity(conn.targetPos) as? org.tim.radtech.energy.IWireConnectable
                    other?.removeConnection(pos)
                    val coil = if (conn.isBare) Radtech.BARE_WIRE_COILS[conn.type]?.get() else Radtech.WIRE_COILS[conn.type]?.get()
                    if (coil != null) {
                        Containers.dropItemStack(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, ItemStack(coil))
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
            Direction.UP    -> SHAPE_FLOOR   // Стоїть на підлозі
            Direction.DOWN  -> SHAPE_CEILING // Прикріплена до стелі, звисає вниз
            Direction.NORTH -> SHAPE_NORTH
            Direction.SOUTH -> SHAPE_SOUTH
            Direction.WEST  -> SHAPE_WEST
            Direction.EAST  -> SHAPE_EAST
        }
    }

    companion object {
        val FACING = DirectionalBlock.FACING
        val LIT: BooleanProperty = BlockStateProperties.LIT
        val CODEC: MapCodec<IndustrialLampBlock> = simpleCodec { IndustrialLampBlock(it) }

        // Нова 3D модель з конекторами на базі (lamb.json)
        private val SHAPE_FLOOR   = Block.box(2.0, 0.0, 5.0, 14.0, 12.0, 11.0)
        private val SHAPE_CEILING = Block.box(2.0, 4.0, 5.0, 14.0, 16.0, 11.0)
        private val SHAPE_NORTH   = Block.box(2.0, 5.0, 4.0, 14.0, 11.0, 16.0)
        private val SHAPE_SOUTH   = Block.box(2.0, 5.0, 0.0, 14.0, 11.0, 12.0)
        private val SHAPE_WEST    = Block.box(4.0, 5.0, 2.0, 16.0, 11.0, 14.0)
        private val SHAPE_EAST    = Block.box(0.0, 5.0, 2.0, 12.0, 11.0, 14.0)
    }
}
