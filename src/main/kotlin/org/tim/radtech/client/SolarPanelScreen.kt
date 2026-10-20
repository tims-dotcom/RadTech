package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.Radtech
import org.tim.radtech.energy.PowerFormat
import org.tim.radtech.menu.SolarPanelMenu

class SolarPanelScreen(menu: SolarPanelMenu, inv: Inventory, title: Component) :
    AbstractContainerScreen<SolarPanelMenu>(menu, inv, title) {

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight)

        // Іконка сонця, якщо є освітленість (32x32)
        if (menu.sunPercent > 0) {
            g.blit(TEXTURE, leftPos + 22, topPos + 26, 176, 0, 32, 32)
        }
    }

    override fun renderLabels(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        g.drawString(font, title, 8, 7, 0xCCE0FF, false)
        g.drawString(font, playerInventoryTitle, 8, 74, 0x8FA4C5, false)

        val sunColor = if (menu.sunPercent > 50) 0xFFEE44 else if (menu.sunPercent > 0) 0xFFAA22 else 0x888888
        val sunText = "Освітленість: ${menu.sunPercent}%"
        g.drawString(font, sunText, 62, 24, sunColor, false)

        val genText = "Генерація: ${PowerFormat.power(menu.genWatts.toLong())}"
        g.drawString(font, genText, 62, 35, 0x55FF55, false)

        val amps = String.format("%.1f", menu.genWatts.toDouble() / 380.0)
        g.drawString(font, "Струм: $amps А (380 В)", 62, 46, 0x55FFFF, false)

        val bufText = "Буфер: ${PowerFormat.energy(menu.stored)}"
        g.drawString(font, bufText, 62, 57, 0xAAAAAA, false)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)
    }

    private companion object {
        val TEXTURE: ResourceLocation = ResourceLocation.fromNamespaceAndPath(Radtech.MODID, "textures/gui/solar_panel.png")
    }
}
