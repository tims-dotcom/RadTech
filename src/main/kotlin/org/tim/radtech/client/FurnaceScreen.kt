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
    private val litSprite = ResourceLocation.withDefaultNamespace("container/furnace/lit_progress")
    private val burnSprite = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress")

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight)
        if (menu.isLit) {
            val h = menu.flameHeight
            g.blitSprite(litSprite, 14, 14, 0, 14 - h, leftPos + 56, topPos + 36 + 14 - h, 14, h)
        }
        g.blitSprite(burnSprite, 24, 16, 0, 0, leftPos + 79, topPos + 34, menu.arrowWidth, 16)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)
    }
}