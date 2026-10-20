package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.Radtech
import org.tim.radtech.energy.PowerFormat
import org.tim.radtech.menu.BatteryMenu

class BatteryScreen(menu: BatteryMenu, inv: Inventory, title: Component) :
    AbstractContainerScreen<BatteryMenu>(menu, inv, title) {

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight)

        // 1. Шкала заряду (x=16, y=25, w=14, h=40)
        val cap = menu.capacity
        val stored = menu.stored
        if (cap > 0) {
            val fillH = ((stored * 40) / cap).toInt().coerceIn(0, 40)
            if (fillH > 0) {
                g.blit(TEXTURE, leftPos + 16, topPos + 25 + 40 - fillH, 176, 40 - fillH, 14, fillH)
            }
        }

        // 2. Шкала тепла (x=38, y=25, w=8, h=40)
        val isLithium = menu.be is org.tim.radtech.block.LithiumBatteryBlockEntity
        val maxHeat = if (isLithium) 120 else 80
        val heat = menu.heatTicks.coerceIn(0, maxHeat)
        val heatH = ((heat * 40) / maxHeat).coerceIn(0, 40)
        if (heatH > 0) {
            g.blit(TEXTURE, leftPos + 38, topPos + 25 + 40 - heatH, 192, 40 - heatH, 8, heatH)
        }
    }

    override fun renderLabels(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        // Заголовок зверху
        g.drawString(font, title, 8, 7, 0xCCE0FF, false)
        // Заголовок інвентаря
        g.drawString(font, playerInventoryTitle, 8, 74, 0x8FA4C5, false)

        // Цифрове табло праворуч від шкал
        val percent = if (menu.capacity > 0) (menu.stored * 100 / menu.capacity) else 0
        val chargeColor = if (percent > 20) 0x55FF55 else 0xFFAA00
        g.drawString(font, "Заряд: $percent%", 54, 25, chargeColor, false)

        g.drawString(font, "Напруга: 380 В", 54, 36, 0x55FFFF, false)

        val isLithium = menu.be is org.tim.radtech.block.LithiumBatteryBlockEntity
        val safeAmps = if (isLithium) 64.0 else 32.0
        val maxHeat = if (isLithium) 120 else 80

        val ampsText = String.format("Струм: %.1f А", menu.amps)
        val ampsColor = if (menu.amps > safeAmps) 0xFF5555 else 0xFFFF55
        g.drawString(font, ampsText, 54, 47, ampsColor, false)

        val (statusText, statusColor) = if (menu.isOverheated) {
            "СТАН: ПЕРЕГРІВ!" to 0xFF2222
        } else if (menu.heatTicks > 30) {
            "СТАН: НАГРІВ" to 0xFFAA00
        } else {
            "СТАН: НОРМА" to 0x55FF55
        }
        g.drawString(font, statusText, 54, 58, statusColor, false)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)

        // Tooltip для шкали заряду
        if (mouseX in (leftPos + 14)..(leftPos + 32) && mouseY in (topPos + 23)..(topPos + 67)) {
            val list = listOf(
                Component.literal("§aЕнергія: §f${PowerFormat.energy(menu.stored)} / ${PowerFormat.energy(menu.capacity)}"),
                Component.literal("§7Заряд: §f${if (menu.capacity > 0) menu.stored * 100 / menu.capacity else 0}%")
            )
            g.renderComponentTooltip(font, list, mouseX, mouseY)
        }

        // Tooltip для шкали тепла
        if (mouseX in (leftPos + 36)..(leftPos + 48) && mouseY in (topPos + 23)..(topPos + 67)) {
            val isLithium = menu.be is org.tim.radtech.block.LithiumBatteryBlockEntity
            val safeStr = if (isLithium) "до 64 А" else "до 32 А"
            val maxHeat = if (isLithium) 120 else 80
            val list = listOf(
                Component.literal("§cТемпература: §f${menu.heatTicks} / $maxHeat"),
                Component.literal("§7Безпечний струм: §f$safeStr"),
                if (menu.isOverheated) Component.literal("§cУВАГА: Загроза вибуху!") else Component.literal("§aОхолодження в нормі")
            )
            g.renderComponentTooltip(font, list, mouseX, mouseY)
        }
    }

    private companion object {
        val TEXTURE: ResourceLocation = ResourceLocation.fromNamespaceAndPath(Radtech.MODID, "textures/gui/battery.png")
    }
}
