package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.block.ThermalGeneratorBlockEntity
import org.tim.radtech.client.widget.EnergyBar
import org.tim.radtech.energy.PowerFormat
import org.tim.radtech.menu.ThermalGeneratorMenu

class ThermalGeneratorScreen(menu: ThermalGeneratorMenu, inv: Inventory, title: Component) :
    AbstractContainerScreen<ThermalGeneratorMenu>(menu, inv, title) {

    private val energyBar = EnergyBar(30, 17, 10, 50)

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight)
        if (menu.isBurning) {
            val h = menu.flameHeight
            val size = ThermalGeneratorMenu.FLAME_SIZE
            g.blit(TEXTURE, leftPos + FLAME_X, topPos + FLAME_Y + size - h, 176, size - h, size, h)
        }
        energyBar.render(g, leftPos, topPos, menu.stored, menu.capacity)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)
        if (energyBar.isHovered(leftPos, topPos, mouseX, mouseY)) {
            val output = Component.translatable(
                "gui.radtech.output",
                PowerFormat.voltage(ThermalGeneratorBlockEntity.TIER.volts),
                ThermalGeneratorBlockEntity.OUTPUT_AMPS,
                PowerFormat.power(ThermalGeneratorBlockEntity.GENERATION)
            )
            g.renderComponentTooltip(font, energyBar.tooltip(menu.stored, menu.capacity, output), mouseX, mouseY)
        }
    }

    private companion object {
        val TEXTURE: ResourceLocation = ResourceLocation.fromNamespaceAndPath("radtech", "textures/gui/thermal_generator.png")
        const val FLAME_X = 81
        const val FLAME_Y = 36
    }
}