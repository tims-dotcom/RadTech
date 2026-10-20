package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import org.tim.radtech.registry.ModBlockEntities

class RollingMillBlock(props: BlockBehaviour.Properties) :
    ProcessingMachineBlock<RollingMillBlockEntity>(props, { ModBlockEntities.ROLLING_MILL.get() }) {

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    companion object {
        val CODEC: MapCodec<RollingMillBlock> = simpleCodec { RollingMillBlock(it) }
    }
}
