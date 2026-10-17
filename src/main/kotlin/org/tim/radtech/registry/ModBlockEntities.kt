package org.tim.radtech.registry

import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.neoforge.registries.DeferredRegister
import org.tim.radtech.Radtech
import org.tim.radtech.block.MachineBlockEntity

object ModBlockEntities {
    val TYPES: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Radtech.MODID)

    val MACHINE = TYPES.register("machine") { ->
        BlockEntityType.Builder.of(
            BlockEntityType.BlockEntitySupplier { pos, state -> MachineBlockEntity(pos, state) },
            Radtech.MACHINE_BLOCK.get()
        ).build(null)
    }
}