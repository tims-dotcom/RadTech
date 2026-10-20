package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.Radtech
import org.tim.radtech.client.widget.EnergyBar
import org.tim.radtech.energy.PowerFormat
import org.tim.radtech.menu.ProcessingMenu

class ProcessingScreen(menu: ProcessingMenu, inv: Inventory, title: Component) :
    AbstractContainerScreen<ProcessingMenu>(menu, inv, title) {

    private val energyBar = EnergyBar(19, 24, 12, 42)

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight)
        val width = menu.progress * 24 / menu.maxProgress
        g.blit(TEXTURE, leftPos + 79, topPos + 36, 176, 0, width, 17)
        energyBar.render(g, leftPos, topPos, menu.stored, menu.capacity)
    }

    override fun renderLabels(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        g.drawString(font, title, 8, 7, 0xCCE0FF, false)
        g.drawString(font, playerInventoryTitle, 8, 74, 0x8FA4C5, false)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)
        if (energyBar.isHovered(leftPos, topPos, mouseX, mouseY)) {
            val power = menu.be.power
            val input = Component.translatable(
                "gui.radtech.input",
                PowerFormat.voltage(power.tier.volts),
                power.maxInputAmps,
                PowerFormat.power(power.maxInputPerTick)
            )
            g.renderComponentTooltip(font, energyBar.tooltip(menu.stored, menu.capacity, input), mouseX, mouseY)
        }
    }

    private companion object {
        val TEXTURE: ResourceLocation = ResourceLocation.fromNamespaceAndPath(Radtech.MODID, "textures/gui/crusher.png")
    }
}
