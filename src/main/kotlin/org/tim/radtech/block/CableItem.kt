package org.tim.radtech.block

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.block.Block
import org.tim.radtech.energy.CableType
import org.tim.radtech.energy.PowerFormat

class CableItem(block: Block, private val type: CableType, props: Properties) : BlockItem(block, props) {

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component>,
        flag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltip, flag)
        tooltip += Component.translatable(
            "tooltip.radtech.cable",
            type.maxAmps,
            PowerFormat.power(type.maxPerTick),
            PowerFormat.voltage(type.tier.volts)
        ).withStyle(ChatFormatting.GRAY)
    }
}
