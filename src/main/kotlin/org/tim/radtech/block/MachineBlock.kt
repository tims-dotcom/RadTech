package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.*
import net.minecraft.world.level.block.state.*
import net.minecraft.world.phys.BlockHitResult
import org.tim.radtech.registry.ModBlockEntities
import net.minecraft.world.Containers

class MachineBlock(props: BlockBehaviour.Properties) : BaseEntityBlock(props) {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? =
        defaultBlockState().setValue(FACING, ctx.horizontalDirection.opposite)

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = MachineBlockEntity(pos, state)

    override fun getRenderShape(state: BlockState) = RenderShape.MODEL

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!level.isClientSide) {
            (level.getBlockEntity(pos) as? MachineBlockEntity)?.let { player.openMenu(it, pos) }
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? =
        createTickerHelper(type, ModBlockEntities.MACHINE.get()) { l, p, s, be -> MachineBlockEntity.tick(l, p, s, be) }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean) {
        if (!state.`is`(newState.block)) {
            (level.getBlockEntity(pos) as? MachineBlockEntity)?.let { be ->
                for (i in 0 until be.items.slots) {
                    Containers.dropItemStack(level, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), be.items.getStackInSlot(i))
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    companion object {
        val FACING = HorizontalDirectionalBlock.FACING
        val CODEC: MapCodec<MachineBlock> = simpleCodec { MachineBlock(it) }
    }
}