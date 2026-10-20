package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.tim.radtech.registry.ModBlockEntities

class PanelMeterBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.PANEL_METER.get(), pos, state)
