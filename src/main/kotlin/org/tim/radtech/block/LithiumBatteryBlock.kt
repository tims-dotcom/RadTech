package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
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

class LithiumBatteryBlock(props: BlockBehaviour.Properties) : BaseEntityBlock(props) {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OVERHEATED, false)
        )
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, OVERHEATED)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? =
        defaultBlockState().setValue(FACING, ctx.horizontalDirection.opposite)

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        LithiumBatteryBlockEntity(pos, state)

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!level.isClientSide) {
            (level.getBlockEntity(pos) as? LithiumBatteryBlockEntity)?.let { player.openMenu(it, pos) }
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean) {
        if (!state.`is`(newState.block)) {
            (level.getBlockEntity(pos) as? LithiumBatteryBlockEntity)?.let { be ->
                for (i in 0 until be.items.slots) {
                    Containers.dropItemStack(level, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), be.items.getStackInSlot(i))
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (state.getValue(OVERHEATED)) {
            val x = pos.x + 0.5 + (random.nextDouble() - 0.5) * 0.6
            val y = pos.y + 0.9 + random.nextDouble() * 0.2
            val z = pos.z + 0.5 + (random.nextDouble() - 0.5) * 0.6
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0, 0.05, 0.0)
            if (random.nextInt(3) == 0) {
                level.playLocalSound(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 1.2f, false)
            }
        }
    }

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? =
        createTickerHelper(type, ModBlockEntities.LITHIUM_BATTERY.get()) { l, p, s, be ->
            LithiumBatteryBlockEntity.tick(l, p, s, be)
        }

    companion object {
        val FACING = HorizontalDirectionalBlock.FACING
        val OVERHEATED: BooleanProperty = BooleanProperty.create("overheated")
        val CODEC: MapCodec<LithiumBatteryBlock> = simpleCodec { LithiumBatteryBlock(it) }
    }
}
