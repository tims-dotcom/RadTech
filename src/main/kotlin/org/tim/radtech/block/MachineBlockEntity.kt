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

class MachineBlockEntity(pos: BlockPos, state: BlockState) :
    ProcessingMachineBlockEntity(ModBlockEntities.MACHINE.get(), pos, state, SPEC) {

    override val menuType: MenuType<*> get() = ModMenus.MACHINE.get()

    override fun findRecipe(input: ItemStack, level: Level): MachineRecipe? {
        val resultId = RECIPES[BuiltInRegistries.ITEM.getKey(input.item).toString()] ?: return null
        val item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(resultId))
        return MachineRecipe(ItemStack(item, 2), CRUSH_TIME)
    }

    companion object {
        const val CRUSH_TIME = 100

        private val SPEC = MachineSpec(VoltageTier.INDUSTRIAL, 1, 400_000L, "block.radtech.crusher")

        private val RECIPES = mapOf(
            "radtech:raw_tin" to "radtech:tin_dust",
            "radtech:raw_aluminum" to "radtech:aluminum_dust",
            "radtech:raw_lithium" to "radtech:lithium_dust",
            "radtech:raw_neodymium" to "radtech:neodymium_dust",
            "radtech:raw_titanium" to "radtech:titanium_dust",
            "radtech:raw_tungsten" to "radtech:tungsten_dust",
            "radtech:tin_ore" to "radtech:tin_dust",
            "radtech:aluminum_ore" to "radtech:aluminum_dust",
            "radtech:lithium_ore" to "radtech:lithium_dust",
            "radtech:neodymium_ore" to "radtech:neodymium_dust",
            "radtech:titanium_ore" to "radtech:titanium_dust",
            "radtech:tungsten_ore" to "radtech:tungsten_dust"
        )
    }
}
