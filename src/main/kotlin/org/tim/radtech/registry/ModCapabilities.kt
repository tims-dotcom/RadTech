package org.tim.radtech.registry

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import org.tim.radtech.energy.PowerCapabilities

object ModCapabilities {
    fun register(event: RegisterCapabilitiesEvent) {
        event.registerBlockEntity(PowerCapabilities.POWER, ModBlockEntities.MACHINE.get()) { be, _ -> be.power }
        event.registerBlockEntity(PowerCapabilities.POWER, ModBlockEntities.FURNACE.get()) { be, _ -> be.power }
        event.registerBlockEntity(PowerCapabilities.POWER, ModBlockEntities.THERMAL_GENERATOR.get()) { be, _ -> be.power }
        event.registerBlockEntity(PowerCapabilities.POWER, ModBlockEntities.ROLLING_MILL.get()) { be, _ -> be.power }
        event.registerBlockEntity(PowerCapabilities.POWER, ModBlockEntities.BATTERY.get()) { be, _ -> be.power }
        event.registerBlockEntity(PowerCapabilities.POWER, ModBlockEntities.CIRCUIT_BREAKER.get()) { be, _ -> be.power }
        event.registerBlockEntity(PowerCapabilities.POWER, ModBlockEntities.SOLAR_PANEL.get()) { be, _ -> be.power }
        event.registerBlockEntity(PowerCapabilities.POWER, ModBlockEntities.TRANSFORMER.get()) { be, side -> be.getCapability(side) }
    }
}
