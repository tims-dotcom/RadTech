package org.tim.radtech.registry

import net.minecraft.core.registries.Registries
import net.minecraft.world.inventory.MenuType
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import org.tim.radtech.Radtech
import org.tim.radtech.menu.BatteryMenu
import org.tim.radtech.menu.CircuitBreakerMenu
import org.tim.radtech.menu.LargeThermalGeneratorMenu
import org.tim.radtech.menu.ProcessingMenu
import org.tim.radtech.menu.SolarPanelMenu
import org.tim.radtech.menu.ThermalGeneratorMenu
import org.tim.radtech.menu.TransformerMenu

object ModMenus {
    val MENUS: DeferredRegister<MenuType<*>> = DeferredRegister.create(Registries.MENU, Radtech.MODID)

    val MACHINE: DeferredHolder<MenuType<*>, MenuType<ProcessingMenu>> =
        MENUS.register("machine") { ->
            IMenuTypeExtension.create { id, inv, buf -> ProcessingMenu(ModMenus.MACHINE.get(), id, inv, buf) }
        }

    val FURNACE: DeferredHolder<MenuType<*>, MenuType<ProcessingMenu>> =
        MENUS.register("furnace") { ->
            IMenuTypeExtension.create { id, inv, buf -> ProcessingMenu(ModMenus.FURNACE.get(), id, inv, buf) }
        }

    val THERMAL_GENERATOR: DeferredHolder<MenuType<*>, MenuType<ThermalGeneratorMenu>> =
        MENUS.register("thermal_generator") { ->
            IMenuTypeExtension.create { id, inv, buf -> ThermalGeneratorMenu(id, inv, buf) }
        }

    val LARGE_THERMAL_GEN: DeferredHolder<MenuType<*>, MenuType<LargeThermalGeneratorMenu>> =
        MENUS.register("large_thermal_generator") { ->
            IMenuTypeExtension.create { id, inv, buf -> LargeThermalGeneratorMenu(id, inv, buf) }
        }

    val ROLLING_MILL: DeferredHolder<MenuType<*>, MenuType<ProcessingMenu>> =
        MENUS.register("rolling_mill") { ->
            IMenuTypeExtension.create { id, inv, buf -> ProcessingMenu(ModMenus.ROLLING_MILL.get(), id, inv, buf) }
        }

    val PRESS: DeferredHolder<MenuType<*>, MenuType<ProcessingMenu>> =
        MENUS.register("press") { ->
            IMenuTypeExtension.create { id, inv, buf -> ProcessingMenu(ModMenus.PRESS.get(), id, inv, buf) }
        }

    val BATTERY: DeferredHolder<MenuType<*>, MenuType<BatteryMenu>> =
        MENUS.register("battery") { ->
            IMenuTypeExtension.create { id, inv, buf -> BatteryMenu(id, inv, buf) }
        }

    val CIRCUIT_BREAKER: DeferredHolder<MenuType<*>, MenuType<CircuitBreakerMenu>> =
        MENUS.register("circuit_breaker") { ->
            IMenuTypeExtension.create { id, inv, buf -> CircuitBreakerMenu(id, inv, buf) }
        }

    val SOLAR_PANEL: DeferredHolder<MenuType<*>, MenuType<SolarPanelMenu>> =
        MENUS.register("solar_panel") { ->
            IMenuTypeExtension.create { id, inv, buf -> SolarPanelMenu(id, inv, buf) }
        }

    val TRANSFORMER: DeferredHolder<MenuType<*>, MenuType<TransformerMenu>> =
        MENUS.register("transformer") { ->
            IMenuTypeExtension.create { id, inv, buf -> TransformerMenu(id, inv, buf) }
        }
}
