package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.phys.BlockHitResult
import org.tim.radtech.energy.PowerNetworks
import org.tim.radtech.registry.ModBlockEntities

class QuarryBlock(props: BlockBehaviour.Properties) : BaseEntityBlock(props) {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, LIT)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? =
        defaultBlockState().setValue(FACING, ctx.horizontalDirection.opposite)

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = QuarryBlockEntity(pos, state)

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        if (!level.isClientSide && !state.`is`(oldState.block)) {
            PowerNetworks.registerConnectable(level, pos)
        }
        super.onPlace(state, level, pos, oldState, isMoving)
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (!state.`is`(newState.block)) {
            val be = level.getBlockEntity(pos) as? QuarryBlockEntity
            if (be != null && !level.isClientSide) {
                for (i in 0 until be.items.slots) {
                    val stack = be.items.getStackInSlot(i)
                    if (!stack.isEmpty) {
                        Containers.dropItemStack(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, stack)
                    }
                }
                PowerNetworks.unregisterConnectable(level, pos)
                PowerNetworks.markDirty(level)
            }
        }
        super.onRemove(state, level, pos, newState, isMoving)
    }

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        if (!level.isClientSide) {
            val be = level.getBlockEntity(pos) as? QuarryBlockEntity ?: return InteractionResult.FAIL
            player.openMenu(be, pos)
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? =
        createTickerHelper(type, ModBlockEntities.QUARRY.get()) { l, p, s, be -> QuarryBlockEntity.tick(l, p, s, be) }

    companion object {
        val FACING: DirectionProperty = HorizontalDirectionalBlock.FACING
        val LIT: BooleanProperty = BlockStateProperties.LIT
        val CODEC: MapCodec<QuarryBlock> = simpleCodec { QuarryBlock(it) }
    }
}
