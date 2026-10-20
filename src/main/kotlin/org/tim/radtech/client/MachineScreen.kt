package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.tim.radtech.block.MachineBlockEntity
import org.tim.radtech.client.widget.EnergyBar
import org.tim.radtech.energy.PowerFormat
import org.tim.radtech.menu.MachineMenu

class MachineScreen(menu: MachineMenu, inv: Inventory, title: Component, private val texture: ResourceLocation) :
    AbstractContainerScreen<MachineMenu>(menu, inv, title) {

    private val energyBar = EnergyBar(30, 17, 10, 50)

    override fun renderBg(g: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        g.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight)
        val width = menu.progress * 24 / menu.maxProgress
        g.blit(texture, leftPos + 79, topPos + 34, 176, 0, width, 17)
        energyBar.render(g, leftPos, topPos, menu.stored, menu.capacity)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(g, mouseX, mouseY, partialTick)
        renderTooltip(g, mouseX, mouseY)
        if (energyBar.isHovered(leftPos, topPos, mouseX, mouseY)) {
            val input = Component.translatable(
                "gui.radtech.input",
                PowerFormat.voltage(MachineBlockEntity.TIER.volts),
                MachineBlockEntity.INPUT_AMPS,
                PowerFormat.power(MachineBlockEntity.USE_PER_TICK)
            )
            g.renderComponentTooltip(font, energyBar.tooltip(menu.stored, menu.capacity, input), mouseX, mouseY)
        }
    }
}