package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.block.MachineBlockEntity
import org.tim.radtech.menu.MachineMenu

class MachineScreen(menu: MachineMenu, inv: Inventory, title: Component, private val texture: ResourceLocation) :
    AbstractContainerScreen<MachineMenu>(menu, inv, title) {

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight)
        val w = menu.progress * 24 / menu.maxProgress
        g.blit(texture, leftPos + 79, topPos + 34, 176, 0, w, 17)
        val x = leftPos + BAR_X
        val y = topPos + BAR_Y
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, BORDER)
        g.fill(x, y, x + BAR_W, y + BAR_H, EMPTY)
        val h = (menu.energy * BAR_H / MachineBlockEntity.CAPACITY).toInt().coerceIn(0, BAR_H)
        g.fill(x, y + BAR_H - h, x + BAR_W, y + BAR_H, FULL)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)
        if (isHovering(BAR_X, BAR_Y, BAR_W, BAR_H, mouseX.toDouble(), mouseY.toDouble())) {
            g.renderComponentTooltip(
                font,
                listOf(
                    Component.literal("Енергія: ${menu.energy} / ${MachineBlockEntity.CAPACITY}"),
                    Component.literal("12 В, до 5 А")
                ),
                mouseX,
                mouseY
            )
        }
    }

    companion object {
        private const val BAR_X = 30
        private const val BAR_Y = 17
        private const val BAR_W = 10
        private const val BAR_H = 50
        private val BORDER = 0xFF373737.toInt()
        private val EMPTY = 0xFF1A1A1A.toInt()
        private val FULL = 0xFFF2B705.toInt()
    }
}