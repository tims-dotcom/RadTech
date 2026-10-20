package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.BlockHitResult
import org.tim.radtech.energy.PowerFormat
import org.tim.radtech.registry.ModBlockEntities

class BatteryBlock(props: BlockBehaviour.Properties) : BaseEntityBlock(props) {

    init {
        registerDefaultState(stateDefinition.any().setValue(OVERHEATED, false))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(OVERHEATED)
    }

    override fun getRenderShape(state: BlockState) = RenderShape.MODEL

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        BatteryBlockEntity(pos, state)

    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? =
        createTickerHelper(type, ModBlockEntities.BATTERY.get(), BatteryBlockEntity::tick)

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (!level.isClientSide) {
            val be = level.getBlockEntity(pos) as? BatteryBlockEntity
            if (be != null) {
                player.openMenu(be, pos)
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (state.getValue(OVERHEATED)) {
            val px = pos.x + 0.5 + (random.nextDouble() - 0.5) * 0.4
            val py = pos.y + 1.0
            val pz = pos.z + 0.5 + (random.nextDouble() - 0.5) * 0.4
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, 0.0, 0.06, 0.0)
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0, 0.03, 0.0)
            }
        }
    }

    companion object {
        val OVERHEATED: BooleanProperty = BooleanProperty.create("overheated")
        val CODEC: MapCodec<BatteryBlock> = simpleCodec { BatteryBlock(it) }
    }
}
