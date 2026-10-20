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

class PressBlockEntity(pos: BlockPos, state: BlockState) :
    ProcessingMachineBlockEntity(ModBlockEntities.PRESS.get(), pos, state, SPEC) {

    override val menuType: MenuType<*> get() = ModMenus.PRESS.get()

    override fun findRecipe(input: ItemStack, level: Level): MachineRecipe? {
        val itemId = BuiltInRegistries.ITEM.getKey(input.item).toString()
        val recipeDef = RECIPES[itemId] ?: return null
        val item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(recipeDef.first))
        return MachineRecipe(ItemStack(item, recipeDef.second), PROCESS_TIME)
    }

    companion object {
        const val PROCESS_TIME = 100

        private val SPEC = MachineSpec(VoltageTier.INDUSTRIAL, 6, 400_000L, "block.radtech.press")

        // itemId -> Pair(resultItemId, count)
        // Прес штампує злитки у металеві пластини (2 шт зі злитка — вдвічі вигідніше за ручний крафт)
        val RECIPES = mapOf(
            "minecraft:copper_ingot" to Pair("radtech:copper_plate", 2),
            "minecraft:iron_ingot" to Pair("radtech:iron_plate", 2),
            "radtech:tin_ingot" to Pair("radtech:tin_plate", 2),
            "radtech:aluminum_ingot" to Pair("radtech:aluminum_plate", 2),
            "radtech:titanium_ingot" to Pair("radtech:titanium_plate", 2)
        )
    }
}
