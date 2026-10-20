package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import org.tim.radtech.registry.ModBlockEntities

class PressBlock(props: BlockBehaviour.Properties) :
    ProcessingMachineBlock<PressBlockEntity>(props, { ModBlockEntities.PRESS.get() }) {

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    companion object {
        val CODEC: MapCodec<PressBlock> = simpleCodec { PressBlock(it) }
    }
}
