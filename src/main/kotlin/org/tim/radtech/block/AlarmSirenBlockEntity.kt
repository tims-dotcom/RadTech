package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.tim.radtech.registry.ModBlockEntities

class AlarmSirenBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.ALARM_SIREN.get(), pos, state) {

    companion object {
        fun tick(level: Level, pos: BlockPos, state: BlockState, be: AlarmSirenBlockEntity) {
            if (state.getValue(AlarmSirenBlock.ALARM)) {
                if (level.gameTime % 20 == 0L) {
                    level.playSound(
                        null, pos,
                        SoundEvents.NOTE_BLOCK_BELL.value(),
                        SoundSource.BLOCKS,
                        1.5f, 0.6f
                    )
                }
            }
        }
    }
}
