package org.tim.radtech.registry

import net.minecraft.core.registries.Registries
import net.minecraft.world.inventory.MenuType
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import org.tim.radtech.Radtech
import org.tim.radtech.menu.FurnaceMenu
import org.tim.radtech.menu.MachineMenu
import org.tim.radtech.menu.ThermalGeneratorMenu

object ModMenus {
    val MENUS: DeferredRegister<MenuType<*>> = DeferredRegister.create(Registries.MENU, Radtech.MODID)

    val MACHINE: DeferredHolder<MenuType<*>, MenuType<MachineMenu>> =
        MENUS.register("machine") { ->
            IMenuTypeExtension.create { id, inv, buf -> MachineMenu(id, inv, buf) }
        }

    val FURNACE: DeferredHolder<MenuType<*>, MenuType<FurnaceMenu>> =
        MENUS.register("furnace") { ->
            IMenuTypeExtension.create { id, inv, buf -> FurnaceMenu(id, inv, buf) }
        }

    val THERMAL_GENERATOR: DeferredHolder<MenuType<*>, MenuType<ThermalGeneratorMenu>> =
        MENUS.register("thermal_generator") { ->
            IMenuTypeExtension.create { id, inv, buf -> ThermalGeneratorMenu(id, inv, buf) }
        }
}