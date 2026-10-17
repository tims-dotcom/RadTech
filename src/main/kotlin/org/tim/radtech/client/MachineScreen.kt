package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.menu.MachineMenu

class MachineScreen(menu: MachineMenu, inv: Inventory, title: Component, private val texture: ResourceLocation) :
    AbstractContainerScreen<MachineMenu>(menu, inv, title) {

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight)
        val w = menu.progress * 24 / menu.maxProgress
        g.blit(texture, leftPos + 79, topPos + 34, 176, 0, w, 17)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)
    }
}