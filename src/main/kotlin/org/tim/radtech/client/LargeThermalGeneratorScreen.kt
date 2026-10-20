package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.Radtech
import org.tim.radtech.client.widget.EnergyBar
import org.tim.radtech.energy.PowerFormat
import org.tim.radtech.menu.LargeThermalGeneratorMenu

class LargeThermalGeneratorScreen(menu: LargeThermalGeneratorMenu, inv: Inventory, title: Component) :
    AbstractContainerScreen<LargeThermalGeneratorMenu>(menu, inv, title) {

    private val energyBar = EnergyBar(147, 24, 12, 42)

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight)

        // Очищуємо внутрішню область верхньої панелі від графіки малого теплогенератора
        g.fill(leftPos + 8, topPos + 22, leftPos + 168, topPos + 68, 0xFF06080C.toInt())

        // Малюємо 4 рамки слотів палива (2x2 сітка: 96,26 | 114,26 | 96,44 | 114,44)
        val slotCoords = arrayOf(95 to 25, 113 to 25, 95 to 43, 113 to 43)
        for ((sx, sy) in slotCoords) {
            val x = leftPos + sx
            val y = topPos + sy
            g.fill(x, y, x + 18, y + 18, 0xFF2B384E.toInt())
            g.fill(x + 1, y + 1, x + 17, y + 17, 0xFF101218.toInt())
        }

        // Індикатор горіння (вертикальна шкала полум'я зліва від слотів палива)
        val fx = leftPos + 81
        val fy = topPos + 26
        val fw = 8
        val fh = 36
        g.fill(fx - 1, fy - 1, fx + fw + 1, fy + fh + 1, 0xFF2B384E.toInt())
        g.fill(fx, fy, fx + fw, fy + fh, 0xFF101218.toInt())

        if (menu.burnTime > 0 && menu.maxBurnTime > 0) {
            val h = (menu.burnTime * fh / menu.maxBurnTime).coerceIn(1, fh)
            g.fillGradient(fx, fy + fh - h, fx + fw, fy + fh, 0xFFFFCC22.toInt(), 0xFFFF5500.toInt())
        }

        // Рамка під шкалу енергії та сама шкала
        g.fill(leftPos + 146, topPos + 23, leftPos + 160, topPos + 67, 0xFF975120.toInt())
        g.fill(leftPos + 147, topPos + 24, leftPos + 159, topPos + 66, 0xFF030508.toInt())
        energyBar.render(g, leftPos, topPos, menu.stored, menu.capacity)
    }

    override fun renderLabels(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        g.drawString(font, title, 8, 7, 0xCCE0FF, false)
        g.drawString(font, playerInventoryTitle, 8, 74, 0x8FA4C5, false)

        val statusText = if (menu.isFormed) "§a3x3x3 ЗІБРАНО" else "§cНЕ ЗІБРАНО"
        g.drawString(font, statusText, 11, 25, 0xFFFFFF, false)

        if (menu.isFormed && menu.burnTime > 0) {
            g.drawString(font, "§6⚡ 18.24 кВт", 11, 38, 0xFFB040, false)
            g.drawString(font, "§e380 В  48 А", 11, 51, 0xE0E0E0, false)
        } else {
            g.drawString(font, "§70.00 кВт", 11, 38, 0x888888, false)
            g.drawString(font, "§8380 В  0 А", 11, 51, 0x666666, false)
        }
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)

        if (energyBar.isHovered(leftPos, topPos, mouseX, mouseY)) {
            val power = menu.be.power
            val out = Component.translatable(
                "gui.radtech.output",
                PowerFormat.voltage(power.tier.volts),
                power.maxOutputAmps,
                PowerFormat.power(power.maxOutputPerTick)
            )
            g.renderComponentTooltip(font, energyBar.tooltip(menu.stored, menu.capacity, out), mouseX, mouseY)
        }
    }

    private companion object {
        val TEXTURE: ResourceLocation = ResourceLocation.fromNamespaceAndPath(Radtech.MODID, "textures/gui/thermal_generator.png")
    }
}
