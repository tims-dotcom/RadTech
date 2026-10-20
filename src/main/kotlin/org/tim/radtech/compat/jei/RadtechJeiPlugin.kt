package org.tim.radtech.compat.jei

import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.constants.RecipeTypes
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.gui.ingredient.IRecipeSlotsView
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.recipe.category.IRecipeCategory
import mezz.jei.api.registration.IGuiHandlerRegistration
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import org.tim.radtech.Radtech
import org.tim.radtech.block.MachineBlockEntity
import org.tim.radtech.block.PressBlockEntity
import org.tim.radtech.block.RollingMillBlockEntity
import org.tim.radtech.client.ProcessingScreen

data class SimpleMachineJeiRecipe(val input: ItemStack, val output: ItemStack)

@JeiPlugin
class RadtechJeiPlugin : IModPlugin {

    companion object {
        val UID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(Radtech.MODID, "jei_plugin")

        val CRUSHING_TYPE: RecipeType<SimpleMachineJeiRecipe> =
            RecipeType.create(Radtech.MODID, "crushing", SimpleMachineJeiRecipe::class.java)
        val PRESSING_TYPE: RecipeType<SimpleMachineJeiRecipe> =
            RecipeType.create(Radtech.MODID, "pressing", SimpleMachineJeiRecipe::class.java)
        val ROLLING_TYPE: RecipeType<SimpleMachineJeiRecipe> =
            RecipeType.create(Radtech.MODID, "rolling", SimpleMachineJeiRecipe::class.java)
    }

    override fun getPluginUid(): ResourceLocation = UID

    override fun registerCategories(registration: IRecipeCategoryRegistration) {
        val guiHelper = registration.jeiHelpers.guiHelper
        registration.addRecipeCategories(
            RadtechMachineCategory(
                guiHelper,
                CRUSHING_TYPE,
                Component.translatable("block.radtech.crusher"),
                ItemStack(Radtech.MACHINE_ITEM.get())
            ),
            RadtechMachineCategory(
                guiHelper,
                PRESSING_TYPE,
                Component.translatable("block.radtech.press"),
                ItemStack(Radtech.PRESS_ITEM.get())
            ),
            RadtechMachineCategory(
                guiHelper,
                ROLLING_TYPE,
                Component.translatable("block.radtech.rolling_mill"),
                ItemStack(Radtech.ROLLING_MILL_ITEM.get())
            )
        )
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        val crushingRecipes = MachineBlockEntity.RECIPES.mapNotNull { (inId, outId) ->
            val inItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(inId))
            val outItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(outId))
            SimpleMachineJeiRecipe(ItemStack(inItem, 1), ItemStack(outItem, 2))
        }
        registration.addRecipes(CRUSHING_TYPE, crushingRecipes)

        val pressingRecipes = PressBlockEntity.RECIPES.mapNotNull { (inId, pair) ->
            val inItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(inId))
            val outItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(pair.first))
            SimpleMachineJeiRecipe(ItemStack(inItem, 1), ItemStack(outItem, pair.second))
        }
        registration.addRecipes(PRESSING_TYPE, pressingRecipes)

        val rollingRecipes = RollingMillBlockEntity.RECIPES.mapNotNull { (inId, pair) ->
            val inItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(inId))
            val outItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(pair.first))
            SimpleMachineJeiRecipe(ItemStack(inItem, 1), ItemStack(outItem, pair.second))
        }
        registration.addRecipes(ROLLING_TYPE, rollingRecipes)
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        registration.addRecipeCatalyst(ItemStack(Radtech.MACHINE_ITEM.get()), CRUSHING_TYPE)
        registration.addRecipeCatalyst(ItemStack(Radtech.PRESS_ITEM.get()), PRESSING_TYPE)
        registration.addRecipeCatalyst(ItemStack(Radtech.ROLLING_MILL_ITEM.get()), ROLLING_TYPE)
        registration.addRecipeCatalyst(ItemStack(Radtech.FURNACE_ITEM.get()), RecipeTypes.SMELTING, RecipeTypes.BLASTING)
    }

    override fun registerGuiHandlers(registration: IGuiHandlerRegistration) {
        registration.addRecipeClickArea(
            ProcessingScreen::class.java,
            76, 35, 24, 16,
            CRUSHING_TYPE, PRESSING_TYPE, ROLLING_TYPE, RecipeTypes.SMELTING
        )
    }
}

class RadtechMachineCategory(
    guiHelper: IGuiHelper,
    private val type: RecipeType<SimpleMachineJeiRecipe>,
    private val title: Component,
    iconStack: ItemStack
) : IRecipeCategory<SimpleMachineJeiRecipe> {

    private val background: IDrawable = guiHelper.createBlankDrawable(120, 48)
    private val icon: IDrawable = guiHelper.createDrawableItemStack(iconStack)

    override fun getRecipeType(): RecipeType<SimpleMachineJeiRecipe> = type
    override fun getTitle(): Component = title
    override fun getBackground(): IDrawable = background
    override fun getIcon(): IDrawable = icon

    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: SimpleMachineJeiRecipe, focuses: IFocusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 12).addItemStack(recipe.input)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 12).addItemStack(recipe.output)
    }

    override fun draw(
        recipe: SimpleMachineJeiRecipe,
        recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics,
        mouseX: Double,
        mouseY: Double
    ) {
        guiGraphics.fill(4, 2, 116, 46, 0xFF0A0D12.toInt())
        guiGraphics.renderOutline(4, 2, 112, 44, 0xFF2B384E.toInt())

        // Рамка вхідного та вихідного слотів
        guiGraphics.fill(19, 11, 37, 29, 0xFF2B384E.toInt())
        guiGraphics.fill(20, 12, 36, 28, 0xFF101218.toInt())

        guiGraphics.fill(83, 11, 101, 29, 0xFF2B384E.toInt())
        guiGraphics.fill(84, 12, 100, 28, 0xFF101218.toInt())

        // Стрілка прогресу
        val font = Minecraft.getInstance().font
        guiGraphics.drawString(font, "§b───►", 48, 16, 0x55FFFF, false)
        guiGraphics.drawString(font, "§7380 В | 2.28 кВт", 22, 34, 0xAAAAAA, false)
    }
}
