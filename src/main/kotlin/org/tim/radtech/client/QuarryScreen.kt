package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.client.widget.EnergyBar
import org.tim.radtech.energy.PowerFormat
import org.tim.radtech.menu.QuarryMenu

class QuarryScreen(menu: QuarryMenu, inv: Inventory, title: Component) :
    AbstractContainerScreen<QuarryMenu>(menu, inv, title) {

    init {
        imageWidth = 176
        imageHeight = 180
        inventoryLabelY = 88
    }

    private val energyBar = EnergyBar(152, 14, 12, 34)

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        // Темна металева основа екрану
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF141820.toInt())
        g.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF35445C.toInt())
        g.renderOutline(leftPos + 1, topPos + 1, imageWidth - 2, imageHeight - 2, 0xFF1C222E.toInt())

        // Верхня панель статусу
        g.fill(leftPos + 6, topPos + 12, leftPos + 148, topPos + 48, 0xFF0B0E14.toInt())
        g.renderOutline(leftPos + 6, topPos + 12, 142, 36, 0xFF2A364A.toInt())

        // 18 слотів буфера видобутку (2x9)
        for (r in 0..1) {
            for (c in 0..8) {
                val sx = leftPos + 7 + c * 18
                val sy = topPos + 53 + r * 18
                g.fill(sx, sy, sx + 18, sy + 18, 0xFF2B384E.toInt())
                g.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF101218.toInt())
            }
        }

        // Слоти інвентаря гравця
        for (r in 0..2) {
            for (c in 0..8) {
                val sx = leftPos + 7 + c * 18
                val sy = topPos + 97 + r * 18
                g.fill(sx, sy, sx + 18, sy + 18, 0xFF2B384E.toInt())
                g.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF101218.toInt())
            }
        }
        for (c in 0..8) {
            val sx = leftPos + 7 + c * 18
            val sy = topPos + 155
            g.fill(sx, sy, sx + 18, sy + 18, 0xFF2B384E.toInt())
            g.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF101218.toInt())
        }

        // Шкала енергії
        g.fill(leftPos + 151, topPos + 13, leftPos + 165, topPos + 49, 0xFF2B384E.toInt())
        g.fill(leftPos + 152, topPos + 14, leftPos + 164, topPos + 48, 0xFF101218.toInt())
        energyBar.render(g, leftPos, topPos, menu.stored, menu.capacity)
    }

    override fun renderLabels(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        g.drawString(font, title, 8, 3, 0xCCE0FF, false)

        val statusText = if (menu.isFormed) "§a3×3 ЗІБРАНО" else "§cНЕ ЗІБРАНО (8 каркасів)"
        g.drawString(font, statusText, 10, 15, 0xFFFFFF, false)

        val miningText = if (menu.isMining) "§6⚡ Буріння: §eY = ${menu.currentY}" else if (menu.isFormed) "§7Очікування 380 В" else "§8Зберіть каркас 3×3"
        g.drawString(font, miningText, 10, 26, 0xE0E0E0, false)

        g.drawString(font, "§bВидобуто: §f${menu.blocksMined} бл.", 10, 37, 0x55FFFF, false)

        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x8FA4C5, false)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)

        if (energyBar.isHovered(leftPos, topPos, mouseX, mouseY)) {
            val list = listOf(
                Component.literal("§aЕнергія: §f${PowerFormat.energy(menu.stored)} / ${PowerFormat.energy(menu.capacity)}"),
                Component.literal("§7Напруга: §f380 В (до 64 А)"),
                Component.literal("§eСпоживання: §f16.0 кВт при бурінні")
            )
            g.renderComponentTooltip(font, list, mouseX, mouseY)
        }
    }
}
