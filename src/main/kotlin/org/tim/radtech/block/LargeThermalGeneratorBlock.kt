package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
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
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.BlockHitResult
import org.tim.radtech.registry.ModBlockEntities

class LargeThermalGeneratorBlock(props: BlockBehaviour.Properties) : BaseEntityBlock(props) {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(FORMED, false)
        )
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, LIT, FORMED)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? =
        defaultBlockState().setValue(FACING, ctx.horizontalDirection.opposite)

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        LargeThermalGeneratorBlockEntity(pos, state)

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!level.isClientSide) {
            val be = level.getBlockEntity(pos) as? LargeThermalGeneratorBlockEntity
            if (be != null) {
                val formed = be.checkStructure(level, pos, state)
                if (formed) {
                    player.openMenu(be, pos)
                } else {
                    player.displayClientMessage(
                        Component.literal("§c[RadTech] Багатоблочну структуру 3x3x3 не зібрано! Побудуйте куб 3x3x3 з Кожухів котла (Boiler Casing) навколо контролера."),
                        true
                    )
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean) {
        if (!state.`is`(newState.block)) {
            (level.getBlockEntity(pos) as? LargeThermalGeneratorBlockEntity)?.let { be ->
                for (i in 0 until be.items.slots) {
                    Containers.dropItemStack(level, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), be.items.getStackInSlot(i))
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? =
        createTickerHelper(type, ModBlockEntities.LARGE_THERMAL_GEN.get()) { l, p, s, be ->
            LargeThermalGeneratorBlockEntity.tick(l, p, s, be)
        }

    companion object {
        val FACING = HorizontalDirectionalBlock.FACING
        val LIT: BooleanProperty = BooleanProperty.create("lit")
        val FORMED: BooleanProperty = BooleanProperty.create("formed")
        val CODEC: MapCodec<LargeThermalGeneratorBlock> = simpleCodec { LargeThermalGeneratorBlock(it) }
    }
}
