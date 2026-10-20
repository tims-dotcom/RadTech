package org.tim.radtech.client

import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import org.tim.radtech.Radtech
import org.tim.radtech.registry.ModMenus

object ClientEvents {
    private val CRUSHER_TEX = ResourceLocation.fromNamespaceAndPath(Radtech.MODID, "textures/gui/crusher.png")

    fun onRegisterScreens(e: RegisterMenuScreensEvent) {
        e.register(ModMenus.MACHINE.get()) { menu, inv, title -> MachineScreen(menu, inv, title, CRUSHER_TEX) }
        e.register(ModMenus.FURNACE.get()) { menu, inv, title -> FurnaceScreen(menu, inv, title) }
        e.register(ModMenus.THERMAL_GENERATOR.get()) { menu, inv, title -> ThermalGeneratorScreen(menu, inv, title) }
    }
}