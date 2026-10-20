package org.tim.radtech.block

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
import net.minecraft.world.phys.BlockHitResult

abstract class ProcessingMachineBlock<E : ProcessingMachineBlockEntity>(
    props: BlockBehaviour.Properties,
    private val entityType: () -> BlockEntityType<E>
) : BaseEntityBlock(props) {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? =
        defaultBlockState().setValue(FACING, ctx.horizontalDirection.opposite)

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = entityType().create(pos, state)

    override fun getRenderShape(state: BlockState) = RenderShape.MODEL

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!level.isClientSide) {
            (level.getBlockEntity(pos) as? ProcessingMachineBlockEntity)?.let { player.openMenu(it, pos) }
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean) {
        if (!state.`is`(newState.block)) {
            (level.getBlockEntity(pos) as? ProcessingMachineBlockEntity)?.let { be ->
                for (i in 0 until be.items.slots) {
                    Containers.dropItemStack(level, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), be.items.getStackInSlot(i))
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? =
        createTickerHelper(type, entityType()) { l, p, s, be -> ProcessingMachineBlockEntity.tick(l, p, s, be) }

    companion object {
        val FACING = HorizontalDirectionalBlock.FACING
    }
}
