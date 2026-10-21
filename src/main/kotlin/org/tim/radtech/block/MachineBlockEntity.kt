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

class MachineBlockEntity(pos: BlockPos, state: BlockState) :
    ProcessingMachineBlockEntity(ModBlockEntities.MACHINE.get(), pos, state, SPEC) {

    override val menuType: MenuType<*> get() = ModMenus.MACHINE.get()

    override fun findRecipe(input: ItemStack, level: Level): MachineRecipe? {
        val resultItem = ITEM_LOOKUP[input.item] ?: return null
        return MachineRecipe(ItemStack(resultItem, 2), CRUSH_TIME)
    }

    companion object {
        const val CRUSH_TIME = 100

        private val SPEC = MachineSpec(VoltageTier.INDUSTRIAL, 6, 400_000L, "block.radtech.crusher")

        val RECIPES = mapOf(
            // RadTech руди та raw матеріали -> подвоєний пил
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
            "radtech:tungsten_ore" to "radtech:tungsten_dust",
            // Ванільні руди та сирі метали -> подвоєний пил для переплавки в злитки
            "minecraft:raw_iron" to "radtech:iron_dust",
            "minecraft:iron_ore" to "radtech:iron_dust",
            "minecraft:deepslate_iron_ore" to "radtech:iron_dust",
            "minecraft:raw_copper" to "radtech:copper_dust",
            "minecraft:copper_ore" to "radtech:copper_dust",
            "minecraft:deepslate_copper_ore" to "radtech:copper_dust",
            "minecraft:raw_gold" to "radtech:gold_dust",
            "minecraft:gold_ore" to "radtech:gold_dust",
            "minecraft:deepslate_gold_ore" to "radtech:gold_dust",
            "minecraft:nether_gold_ore" to "radtech:gold_dust"
        )

        // Кешований прямий пошук за посиланням на Item (O(1), без створення об'єктів стрічок на купі пам'яті)
        private val ITEM_LOOKUP: Map<Item, Item> by lazy {
            RECIPES.mapNotNull { (inId, outId) ->
                val inItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(inId))
                val outItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(outId))
                if (inItem != Items.AIR && outItem != Items.AIR) inItem to outItem else null
            }.toMap()
        }
    }
}
