package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.menu.FurnaceMenu

class FurnaceScreen(menu: FurnaceMenu, inv: Inventory, title: Component) :
    AbstractContainerScreen<FurnaceMenu>(menu, inv, title) {

    private val texture = ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png")

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight)
        if (menu.isLit) {
            val h = menu.flameHeight
            g.blit(texture, leftPos + 56, topPos + 36 + 12 - h, 176, 12 - h, 14, h + 1)
            g.drawString(font, "${menu.data.get(0)} ${menu.data.get(1)} ${menu.data.get(2)}/${menu.data.get(3)}", leftPos + 8, topPos + 60, 0x404040, false)
        }
        g.blit(texture, leftPos + 79, topPos + 34, 176, 14, menu.arrowWidth + 1, 16)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)
    }
}