package org.tim.radtech.compat.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import me.shedaniel.rei.api.common.util.EntryStacks
import me.shedaniel.rei.forge.REIPluginClient
import me.shedaniel.rei.plugin.common.BuiltinPlugin
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import org.tim.radtech.Radtech
import org.tim.radtech.block.MachineBlockEntity
import org.tim.radtech.block.PressBlockEntity
import org.tim.radtech.block.RollingMillBlockEntity

class RadtechReiDisplay(
    inputs: List<EntryIngredient>,
    outputs: List<EntryIngredient>,
    private val catId: CategoryIdentifier<RadtechReiDisplay>
) : BasicDisplay(inputs, outputs) {
    override fun getCategoryIdentifier(): CategoryIdentifier<*> = catId
}

@REIPluginClient
class RadtechReiPlugin : REIClientPlugin {

    companion object {
        val CRUSHING_ID: CategoryIdentifier<RadtechReiDisplay> =
            CategoryIdentifier.of(Radtech.MODID, "crushing")
        val PRESSING_ID: CategoryIdentifier<RadtechReiDisplay> =
            CategoryIdentifier.of(Radtech.MODID, "pressing")
        val ROLLING_ID: CategoryIdentifier<RadtechReiDisplay> =
            CategoryIdentifier.of(Radtech.MODID, "rolling")
    }

    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(
            RadtechReiCategory(
                CRUSHING_ID,
                Component.translatable("block.radtech.crusher"),
                EntryStacks.of(ItemStack(Radtech.MACHINE_ITEM.get()))
            ),
            RadtechReiCategory(
                PRESSING_ID,
                Component.translatable("block.radtech.press"),
                EntryStacks.of(ItemStack(Radtech.PRESS_ITEM.get()))
            ),
            RadtechReiCategory(
                ROLLING_ID,
                Component.translatable("block.radtech.rolling_mill"),
                EntryStacks.of(ItemStack(Radtech.ROLLING_MILL_ITEM.get()))
            )
        )

        registry.addWorkstations(CRUSHING_ID, EntryStacks.of(ItemStack(Radtech.MACHINE_ITEM.get())))
        registry.addWorkstations(PRESSING_ID, EntryStacks.of(ItemStack(Radtech.PRESS_ITEM.get())))
        registry.addWorkstations(ROLLING_ID, EntryStacks.of(ItemStack(Radtech.ROLLING_MILL_ITEM.get())))
        registry.addWorkstations(BuiltinPlugin.SMELTING, EntryStacks.of(ItemStack(Radtech.FURNACE_ITEM.get())))
        registry.addWorkstations(BuiltinPlugin.BLASTING, EntryStacks.of(ItemStack(Radtech.FURNACE_ITEM.get())))
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        for ((inId, outId) in MachineBlockEntity.RECIPES) {
            val inItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(inId))
            val outItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(outId))
            registry.add(
                RadtechReiDisplay(
                    listOf(EntryIngredients.of(ItemStack(inItem, 1))),
                    listOf(EntryIngredients.of(ItemStack(outItem, 2))),
                    CRUSHING_ID
                )
            )
        }

        for ((inId, pair) in PressBlockEntity.RECIPES) {
            val inItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(inId))
            val outItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(pair.first))
            registry.add(
                RadtechReiDisplay(
                    listOf(EntryIngredients.of(ItemStack(inItem, 1))),
                    listOf(EntryIngredients.of(ItemStack(outItem, pair.second))),
                    PRESSING_ID
                )
            )
        }

        for ((inId, pair) in RollingMillBlockEntity.RECIPES) {
            val inItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(inId))
            val outItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(pair.first))
            registry.add(
                RadtechReiDisplay(
                    listOf(EntryIngredients.of(ItemStack(inItem, 1))),
                    listOf(EntryIngredients.of(ItemStack(outItem, pair.second))),
                    ROLLING_ID
                )
            )
        }
    }
}

class RadtechReiCategory(
    private val catId: CategoryIdentifier<RadtechReiDisplay>,
    private val title: Component,
    private val icon: Renderer
) : DisplayCategory<RadtechReiDisplay> {

    override fun getCategoryIdentifier(): CategoryIdentifier<out RadtechReiDisplay> = catId
    override fun getTitle(): Component = title
    override fun getIcon(): Renderer = icon
    override fun getDisplayHeight(): Int = 56
    override fun getDisplayWidth(display: RadtechReiDisplay): Int = 136

    override fun setupDisplay(display: RadtechReiDisplay, bounds: Rectangle): List<Widget> {
        val widgets = ArrayList<Widget>()
        widgets.add(Widgets.createRecipeBase(bounds))

        val startX = bounds.x + 24
        val startY = bounds.y + 12

        widgets.add(Widgets.createArrow(Point(startX + 28, startY)).animationDurationTicks(100.0))
        widgets.add(
            Widgets.createSlot(Point(startX, startY))
                .entries(display.inputEntries[0])
                .markInput()
        )
        widgets.add(
            Widgets.createSlot(Point(startX + 64, startY))
                .entries(display.outputEntries[0])
                .markOutput()
        )
        widgets.add(
            Widgets.createLabel(
                Point(bounds.centerX, bounds.y + 38),
                Component.literal("§7380 В | 2.28 кВт")
            ).noShadow().centered()
        )
        return widgets
    }
}
