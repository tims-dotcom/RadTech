package org.tim.radtech.client

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import org.tim.radtech.registry.ModMenus

object ClientEvents {
    fun onRegisterScreens(e: RegisterMenuScreensEvent) {
        e.register(ModMenus.MACHINE.get()) { menu, inv, title -> ProcessingScreen(menu, inv, title) }
        e.register(ModMenus.FURNACE.get()) { menu, inv, title -> ProcessingScreen(menu, inv, title) }
        e.register(ModMenus.THERMAL_GENERATOR.get()) { menu, inv, title -> ThermalGeneratorScreen(menu, inv, title) }
        e.register(ModMenus.ROLLING_MILL.get()) { menu, inv, title -> ProcessingScreen(menu, inv, title) }
        e.register(ModMenus.BATTERY.get()) { menu, inv, title -> BatteryScreen(menu, inv, title) }
        e.register(ModMenus.CIRCUIT_BREAKER.get()) { menu, inv, title -> CircuitBreakerScreen(menu, inv, title) }
        e.register(ModMenus.SOLAR_PANEL.get()) { menu, inv, title -> SolarPanelScreen(menu, inv, title) }
        e.register(ModMenus.TRANSFORMER.get()) { menu, inv, title -> TransformerScreen(menu, inv, title) }
    }
}
