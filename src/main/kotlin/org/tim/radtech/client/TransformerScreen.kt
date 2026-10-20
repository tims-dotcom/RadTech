package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.Radtech
import org.tim.radtech.menu.TransformerMenu

class TransformerScreen(menu: TransformerMenu, inv: Inventory, title: Component) :
    AbstractContainerScreen<TransformerMenu>(menu, inv, title) {

    private lateinit var modeBtn: Button

    override fun init() {
        super.init()
        modeBtn = Button.builder(Component.literal("ЗМІНИТИ РЕЖИМ")) {
            minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, 0)
        }.bounds(leftPos + 40, topPos + 48, 96, 18).build()
        addRenderableWidget(modeBtn)
    }

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    }

    override fun renderLabels(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        g.drawString(font, title, 8, 7, 0xCCE0FF, false)
        g.drawString(font, playerInventoryTitle, 8, 74, 0x8FA4C5, false)

        val modeText = if (menu.mode == 0) "ПІДВИЩУВАЛЬНИЙ (Step-Up)" else "ЗНИЖУВАЛЬНИЙ (Step-Down)"
        val modeColor = if (menu.mode == 0) 0x55FFFF else 0xFFAA00
        g.drawString(font, modeText, 20, 23, modeColor, false)

        val transText = if (menu.mode == 0) "380 В  ──►  10 000 В" else "10 000 В  ──►  380 В"
        g.drawString(font, transText, 32, 35, 0x55FF55, false)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)
    }

    private companion object {
        val TEXTURE: ResourceLocation = ResourceLocation.fromNamespaceAndPath(Radtech.MODID, "textures/gui/transformer.png")
    }
}
