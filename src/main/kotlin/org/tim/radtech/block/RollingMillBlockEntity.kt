package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.registry.ModBlockEntities
import org.tim.radtech.registry.ModMenus

class RollingMillBlockEntity(pos: BlockPos, state: BlockState) :
    ProcessingMachineBlockEntity(ModBlockEntities.ROLLING_MILL.get(), pos, state, SPEC) {

    override val menuType: MenuType<*> get() = ModMenus.ROLLING_MILL.get()

    override fun findRecipe(input: ItemStack, level: Level): MachineRecipe? {
        val itemId = BuiltInRegistries.ITEM.getKey(input.item).toString()
        val recipeDef = RECIPES[itemId] ?: return null
        val item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(recipeDef.first))
        return MachineRecipe(ItemStack(item, recipeDef.second), PROCESS_TIME)
    }

    companion object {
        const val PROCESS_TIME = 100

        private val SPEC = MachineSpec(VoltageTier.INDUSTRIAL, 6, 400_000L, "block.radtech.rolling_mill")

        // itemId -> Pair(resultItemId, count)
        // Прокатний стан витягує виключно пластини у готові кабелі
        private val RECIPES = mapOf(
            "radtech:tin_plate" to Pair("radtech:bare_tin_cable", 2),
            "radtech:copper_plate" to Pair("radtech:bare_copper_cable", 2),
            "radtech:aluminum_plate" to Pair("radtech:bare_aluminum_cable", 2),
            "radtech:iron_plate" to Pair("radtech:bare_iron_cable", 2)
        )
    }
}
