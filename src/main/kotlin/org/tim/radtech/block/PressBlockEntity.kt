package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.registry.ModBlockEntities
import org.tim.radtech.registry.ModMenus

class PressBlockEntity(pos: BlockPos, state: BlockState) :
    ProcessingMachineBlockEntity(ModBlockEntities.PRESS.get(), pos, state, SPEC) {

    override val menuType: MenuType<*> get() = ModMenus.PRESS.get()

    override fun findRecipe(input: ItemStack, level: Level): MachineRecipe? {
        val (outItem, count) = ITEM_LOOKUP[input.item] ?: return null
        return MachineRecipe(ItemStack(outItem, count), PROCESS_TIME)
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

        // Кешований прямий пошук за посиланням на Item (O(1), без створення об'єктів стрічок на купі пам'яті)
        private val ITEM_LOOKUP: Map<Item, Pair<Item, Int>> by lazy {
            RECIPES.mapNotNull { (inId, outDef) ->
                val inItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(inId))
                val outItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(outDef.first))
                if (inItem != Items.AIR && outItem != Items.AIR) {
                    inItem to Pair(outItem, outDef.second)
                } else null
            }.toMap()
        }
    }
}
