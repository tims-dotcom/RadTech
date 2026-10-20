package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.Radtech
import org.tim.radtech.menu.CircuitBreakerMenu

class CircuitBreakerScreen(menu: CircuitBreakerMenu, inv: Inventory, title: Component) :
    AbstractContainerScreen<CircuitBreakerMenu>(menu, inv, title) {

    override fun init() {
        super.init()

        // Кнопка перемикача / скидання (Reset)
        addRenderableWidget(
            Button.builder(Component.literal("ВКЛ")) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, 0)
            }.bounds(leftPos + 56, topPos + 26, 36, 18).build()
        )

        // Кнопки вибору ліміту струму
        val limits = listOf(16, 32, 66, 120)
        limits.forEachIndexed { i, lim ->
            val bx = leftPos + 98 + (i % 2) * 36
            val by = topPos + 26 + (i / 2) * 22
            addRenderableWidget(
                Button.builder(Component.literal("${lim}A")) {
                    minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, i + 1)
                }.bounds(bx, by, 34, 18).build()
            )
        }
    }

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight)

        // Стильний тумблер: 28x22 на (18, 25)
        val isTripped = menu.isTripped
        val leverU = 176
        val leverV = if (isTripped) 24 else 0
        g.blit(TEXTURE, leftPos + 18, topPos + 25, leverU, leverV, 28, 22)
    }

    override fun renderLabels(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        // Заголовок зверху
        g.drawString(font, title, 8, 7, 0xCCE0FF, false)
        // Заголовок інвентаря
        g.drawString(font, playerInventoryTitle, 8, 74, 0x8FA4C5, false)

        // Статус під тумблером
        val statusText = if (menu.isTripped) "§cВИБИТО!" else "§aНОРМА"
        g.drawString(font, statusText, 18, 52, 0xFFFFFF, false)

        // Показники струму
        val infoText = "§7Струм: §b${String.format("%.1f", menu.currentAmps)} A §8/ §e${menu.tripLimitAmps} A"
        g.drawString(font, infoText, 56, 52, 0x55FF55, false)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)
    }

    private companion object {
        val TEXTURE: ResourceLocation = ResourceLocation.fromNamespaceAndPath(Radtech.MODID, "textures/gui/circuit_breaker.png")
    }
}
