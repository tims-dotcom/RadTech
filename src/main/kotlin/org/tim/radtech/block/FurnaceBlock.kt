package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import org.tim.radtech.registry.ModBlockEntities

class FurnaceBlock(props: BlockBehaviour.Properties) :
    ProcessingMachineBlock<FurnaceBlockEntity>(props, { ModBlockEntities.FURNACE.get() }) {

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    companion object {
        val CODEC: MapCodec<FurnaceBlock> = simpleCodec { FurnaceBlock(it) }
    }
}
