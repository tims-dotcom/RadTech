package org.tim.radtech.registry

import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import org.tim.radtech.Radtech
import org.tim.radtech.block.BatteryBlockEntity
import org.tim.radtech.block.CableBlockEntity
import org.tim.radtech.block.CircuitBreakerBlockEntity
import org.tim.radtech.block.FurnaceBlockEntity
import org.tim.radtech.block.MachineBlockEntity
import org.tim.radtech.block.RollingMillBlockEntity
import org.tim.radtech.block.SolarPanelBlockEntity
import org.tim.radtech.block.ThermalGeneratorBlockEntity
import org.tim.radtech.block.TransformerBlockEntity

object ModBlockEntities {
    val TYPES: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Radtech.MODID)

    val MACHINE: DeferredHolder<BlockEntityType<*>, BlockEntityType<MachineBlockEntity>> =
        TYPES.register("machine") { ->
            BlockEntityType.Builder.of(
                BlockEntityType.BlockEntitySupplier { pos, state -> MachineBlockEntity(pos, state) },
                Radtech.MACHINE_BLOCK.get()
            ).build(null)
        }

    val FURNACE: DeferredHolder<BlockEntityType<*>, BlockEntityType<FurnaceBlockEntity>> =
        TYPES.register("furnace") { ->
            BlockEntityType.Builder.of(
                BlockEntityType.BlockEntitySupplier { pos, state -> FurnaceBlockEntity(pos, state) },
                Radtech.FURNACE_BLOCK.get()
            ).build(null)
        }

    val THERMAL_GENERATOR: DeferredHolder<BlockEntityType<*>, BlockEntityType<ThermalGeneratorBlockEntity>> =
        TYPES.register("thermal_generator") { ->
            BlockEntityType.Builder.of(
                BlockEntityType.BlockEntitySupplier { pos, state -> ThermalGeneratorBlockEntity(pos, state) },
                Radtech.THERMAL_GENERATOR_BLOCK.get()
            ).build(null)
        }

    val ROLLING_MILL: DeferredHolder<BlockEntityType<*>, BlockEntityType<RollingMillBlockEntity>> =
        TYPES.register("rolling_mill") { ->
            BlockEntityType.Builder.of(
                BlockEntityType.BlockEntitySupplier { pos, state -> RollingMillBlockEntity(pos, state) },
                Radtech.ROLLING_MILL_BLOCK.get()
            ).build(null)
        }

    val BATTERY: DeferredHolder<BlockEntityType<*>, BlockEntityType<BatteryBlockEntity>> =
        TYPES.register("battery") { ->
            BlockEntityType.Builder.of(
                BlockEntityType.BlockEntitySupplier { pos, state -> BatteryBlockEntity(pos, state) },
                Radtech.BATTERY_BLOCK.get()
            ).build(null)
        }

    val CIRCUIT_BREAKER: DeferredHolder<BlockEntityType<*>, BlockEntityType<CircuitBreakerBlockEntity>> =
        TYPES.register("circuit_breaker") { ->
            BlockEntityType.Builder.of(
                BlockEntityType.BlockEntitySupplier { pos, state -> CircuitBreakerBlockEntity(pos, state) },
                Radtech.CIRCUIT_BREAKER_BLOCK.get()
            ).build(null)
        }

    val SOLAR_PANEL: DeferredHolder<BlockEntityType<*>, BlockEntityType<SolarPanelBlockEntity>> =
        TYPES.register("solar_panel") { ->
            BlockEntityType.Builder.of(
                BlockEntityType.BlockEntitySupplier { pos, state -> SolarPanelBlockEntity(pos, state) },
                Radtech.SOLAR_PANEL_BLOCK.get()
            ).build(null)
        }

    val TRANSFORMER: DeferredHolder<BlockEntityType<*>, BlockEntityType<TransformerBlockEntity>> =
        TYPES.register("transformer") { ->
            BlockEntityType.Builder.of(
                BlockEntityType.BlockEntitySupplier { pos, state -> TransformerBlockEntity(pos, state) },
                Radtech.TRANSFORMER_BLOCK.get()
            ).build(null)
        }

    val CABLE: DeferredHolder<BlockEntityType<*>, BlockEntityType<CableBlockEntity>> =
        TYPES.register("cable") { ->
            BlockEntityType.Builder.of(
                BlockEntityType.BlockEntitySupplier { pos, state -> CableBlockEntity(pos, state) },
                *(Radtech.CABLES.values.map { it.get() } +
                  Radtech.BARE_CABLES.values.map { it.get() }).toTypedArray()
            ).build(null)
        }
}
