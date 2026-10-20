package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.tim.radtech.energy.PowerNetworks
import org.tim.radtech.registry.ModBlockEntities

class CableBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.CABLE.get(), pos, state) {

    override fun onLoad() {
        super.onLoad()
        val cable = blockState.block as? CableBlock ?: return
        level?.takeUnless { it.isClientSide }?.let { PowerNetworks.register(it, worldPosition, cable.type) }
    }

    override fun setRemoved() {
        level?.takeUnless { it.isClientSide }?.let { PowerNetworks.unregister(it, worldPosition) }
        super.setRemoved()
    }
}
