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

        // Кнопка перемикача / скидання (Reset) - розміщуємо по центру зліва
        addRenderableWidget(
            Button.builder(Component.literal("ВКЛ")) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, 0)
            }.bounds(leftPos + 48, topPos + 45, 48, 18).build()
        )

        // Кнопки вибору ліміту струму - розміщуємо праворуч у 2 колонки
        val limits = listOf(16, 32, 66, 120)
        limits.forEachIndexed { i, lim ->
            val col = i % 2
            val row = i / 2
            val bx = leftPos + 104 + col * 34
            val by = topPos + 22 + row * 23
            addRenderableWidget(
                Button.builder(Component.literal("${lim}A")) {
                    minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, i + 1)
                }.bounds(bx, by, 32, 18).build()
            )
        }
    }

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight)

        // Стильний тумблер: 28x22 на (14, 21)
        val isTripped = menu.isTripped
        val leverU = 176
        val leverV = if (isTripped) 24 else 0
        g.blit(TEXTURE, leftPos + 14, topPos + 21, leverU, leverV, 28, 22)
    }

    override fun renderLabels(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        // Заголовок зверху
        g.drawString(font, title, 8, 6, 0xCCE0FF, false)
        // Заголовок інвентаря
        g.drawString(font, playerInventoryTitle, 8, 74, 0x8FA4C5, false)

        // Статус під тумблером (по центру під тумблером 14..42)
        val statusText = if (menu.isTripped) "§cВИБИТО!" else "§aНОРМА"
        g.drawString(font, statusText, 12, 48, 0xFFFFFF, false)

        // Показники струму - акуратно над кнопкою ВКЛ, не налізаючи на кнопки вибору ліміту
        val currentStr = "§7Струм: §b${String.format("%.1f", menu.currentAmps)} A"
        val limitStr = "§7Поріг: §e${menu.tripLimitAmps} A"
        g.drawString(font, currentStr, 48, 21, 0x55FF55, false)
        g.drawString(font, limitStr, 48, 33, 0xFFE066, false)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)
    }

    private companion object {
        val TEXTURE: ResourceLocation = ResourceLocation.fromNamespaceAndPath(Radtech.MODID, "textures/gui/circuit_breaker.png")
    }
}
