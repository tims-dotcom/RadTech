package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.registry.ModBlockEntities
import org.tim.radtech.registry.ModMenus

class FurnaceBlockEntity(pos: BlockPos, state: BlockState) :
    ProcessingMachineBlockEntity(ModBlockEntities.FURNACE.get(), pos, state, SPEC) {

    override val menuType: MenuType<*> get() = ModMenus.FURNACE.get()

    override fun findRecipe(input: ItemStack, level: Level): MachineRecipe? =
        level.recipeManager.getRecipeFor(RecipeType.SMELTING, SingleRecipeInput(input), level)
            .map { holder ->
                val recipe = holder.value()
                MachineRecipe(
                    recipe.getResultItem(level.registryAccess()).copy(),
                    (recipe.cookingTime / SPEED_FACTOR).coerceAtLeast(MIN_TIME)
                )
            }
            .orElse(null)

    companion object {
        const val SPEED_FACTOR = 2
        const val MIN_TIME = 20

        private val SPEC = MachineSpec(VoltageTier.INDUSTRIAL, 1, 400_000L, "block.radtech.furnace")
    }
}
