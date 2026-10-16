package org.tim.radtech

import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredRegister
import org.apache.logging.log4j.LogManager

@Mod(Radtech.MODID)
class Radtech(modEventBus: IEventBus) {

    companion object {
        const val MODID = "radtech"
        val LOGGER = LogManager.getLogger(MODID)

        val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(MODID)
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MODID)

        private fun ore(name: String): DeferredBlock<*> =
            BLOCKS.registerSimpleBlock(
                name,
                BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
            ).also { ITEMS.registerSimpleBlockItem(it) }

        val TIN_ORE = ore("tin_ore")
        val ALUMINUM_ORE = ore("aluminum_ore")
        val LITHIUM_ORE = ore("lithium_ore")
        val NEODYMIUM_ORE = ore("neodymium_ore")
        val TITANIUM_ORE = ore("titanium_ore")
        val TUNGSTEN_ORE = ore("tungsten_ore")

        val RAW_TIN = ITEMS.registerSimpleItem("raw_tin")
        val RAW_ALUMINUM = ITEMS.registerSimpleItem("raw_aluminum")
        val RAW_LITHIUM = ITEMS.registerSimpleItem("raw_lithium")
        val RAW_NEODYMIUM = ITEMS.registerSimpleItem("raw_neodymium")
        val RAW_TITANIUM = ITEMS.registerSimpleItem("raw_titanium")
        val RAW_TUNGSTEN = ITEMS.registerSimpleItem("raw_tungsten")
        val TIN_INGOT = ITEMS.registerSimpleItem("tin_ingot")
        val ALUMINUM_INGOT = ITEMS.registerSimpleItem("aluminum_ingot")
        val LITHIUM_INGOT = ITEMS.registerSimpleItem("lithium_ingot")
        val NEODYMIUM_INGOT = ITEMS.registerSimpleItem("neodymium_ingot")
        val TITANIUM_INGOT = ITEMS.registerSimpleItem("titanium_ingot")
        val TUNGSTEN_INGOT = ITEMS.registerSimpleItem("tungsten_ingot")
        val TIN_NUGGET = ITEMS.registerSimpleItem("tin_nugget")
    }

    init {
        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        modEventBus.addListener(::addCreative)
        LOGGER.info("Radtech mod initialized!")
    }

    private fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        when (event.tabKey) {
            CreativeModeTabs.NATURAL_BLOCKS -> listOf(
                TIN_ORE, ALUMINUM_ORE, LITHIUM_ORE, NEODYMIUM_ORE, TITANIUM_ORE, TUNGSTEN_ORE
            ).forEach { event.accept(it) }
            CreativeModeTabs.INGREDIENTS -> listOf(
                RAW_TIN, RAW_ALUMINUM, RAW_LITHIUM, RAW_NEODYMIUM, RAW_TITANIUM, RAW_TUNGSTEN,
                TIN_INGOT, ALUMINUM_INGOT, LITHIUM_INGOT, NEODYMIUM_INGOT, TITANIUM_INGOT, TUNGSTEN_INGOT, TIN_NUGGET
            ).forEach { event.accept(it) }
            else -> {}
        }
    }
}
