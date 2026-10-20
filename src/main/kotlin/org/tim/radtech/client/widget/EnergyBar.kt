package org.tim.radtech.client.widget

import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import org.tim.radtech.energy.PowerFormat

class EnergyBar(private val x: Int, private val y: Int, private val width: Int, private val height: Int) {

    fun render(g: GuiGraphics, left: Int, top: Int, stored: Long, capacity: Long) {
        val bx = left + x
        val by = top + y
        g.fill(bx - 1, by - 1, bx + width + 1, by + height + 1, LIGHT)
        g.fill(bx - 1, by - 1, bx + width, by + height, DARK)
        g.fill(bx, by, bx + width, by + height, EMPTY)
        val filled = if (capacity > 0) (stored * height / capacity).toInt().coerceIn(0, height) else 0
        if (filled > 0) {
            val fillTop = by + height - filled
            g.fill(bx, fillTop, bx + width, by + height, FULL)
            g.fill(bx, fillTop, bx + width, fillTop + 1, GLOW)
        }
        for (i in 1..3) {
            val ty = by + height * i / 4
            g.fill(bx, ty, bx + width, ty + 1, TICK)
        }
    }

    fun isHovered(left: Int, top: Int, mouseX: Int, mouseY: Int) =
        mouseX in (left + x) until (left + x + width) && mouseY in (top + y) until (top + y + height)

    fun tooltip(stored: Long, capacity: Long, vararg extra: Component): List<Component> {
        val percent = if (capacity > 0) stored * 100 / capacity else 0
        return buildList {
            add(Component.translatable("gui.radtech.energy", PowerFormat.energy(stored), PowerFormat.energy(capacity)))
            add(Component.translatable("gui.radtech.charge", percent).withStyle(ChatFormatting.GRAY))
            addAll(extra)
        }
    }

    private companion object {
        val LIGHT = 0xFFFFFFFF.toInt()
        val DARK = 0xFF373737.toInt()
        val EMPTY = 0xFF1A1A1A.toInt()
        val FULL = 0xFFF2B705.toInt()
        val GLOW = 0xFFFFE27A.toInt()
        val TICK = 0x55000000
    }
}