package org.tim.radtech.block

import com.mojang.serialization.MapCodec
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import org.tim.radtech.registry.ModBlockEntities

class MachineBlock(props: BlockBehaviour.Properties) :
    ProcessingMachineBlock<MachineBlockEntity>(props, { ModBlockEntities.MACHINE.get() }) {

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    companion object {
        val CODEC: MapCodec<MachineBlock> = simpleCodec { MachineBlock(it) }
    }
}
