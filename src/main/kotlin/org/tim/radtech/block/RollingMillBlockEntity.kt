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

class RollingMillBlockEntity(pos: BlockPos, state: BlockState) :
    ProcessingMachineBlockEntity(ModBlockEntities.ROLLING_MILL.get(), pos, state, SPEC) {

    override val menuType: MenuType<*> get() = ModMenus.ROLLING_MILL.get()

    override fun findRecipe(input: ItemStack, level: Level): MachineRecipe? {
        val (outItem, count) = ITEM_LOOKUP[input.item] ?: return null
        return MachineRecipe(ItemStack(outItem, count), PROCESS_TIME)
    }

    companion object {
        const val PROCESS_TIME = 100

        private val SPEC = MachineSpec(VoltageTier.INDUSTRIAL, 6, 400_000L, "block.radtech.rolling_mill")

        // itemId -> Pair(resultItemId, count)
        // Прокатний стан витягує пластини у котушки оголеного дроту (4 шт з 1 пластини)
        val RECIPES = mapOf(
            "radtech:tin_plate" to Pair("radtech:bare_tin_wire_coil", 4),
            "radtech:copper_plate" to Pair("radtech:bare_copper_wire_coil", 4),
            "radtech:aluminum_plate" to Pair("radtech:bare_aluminum_wire_coil", 4),
            "radtech:iron_plate" to Pair("radtech:bare_iron_wire_coil", 4)
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
