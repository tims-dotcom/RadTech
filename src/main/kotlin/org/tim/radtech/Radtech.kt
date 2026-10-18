package org.tim.radtech

import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import org.apache.logging.log4j.LogManager
import org.tim.radtech.block.MachineBlock
import org.tim.radtech.registry.ModBlockEntities
import org.tim.radtech.registry.ModMenus
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.loading.FMLEnvironment
import org.tim.radtech.client.ClientEvents
import org.tim.radtech.block.FurnaceBlock
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import org.tim.radtech.energy.PowerCapabilities

@Mod(Radtech.MODID)
class Radtech(modEventBus: IEventBus) {

    companion object {
        const val MODID = "radtech"
        val LOGGER = LogManager.getLogger(MODID)

        val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(MODID)
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MODID)

        val METALS = listOf("tin", "aluminum", "lithium", "neodymium", "titanium", "tungsten")

        val ORES: Map<String, DeferredBlock<Block>> = METALS.associateWith { metal ->
            BLOCKS.registerSimpleBlock(
                "${metal}_ore",
                BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
            ).also { ITEMS.registerSimpleBlockItem(it) }
        }

        val RAW: Map<String, DeferredItem<Item>> = METALS.associateWith { ITEMS.registerSimpleItem("raw_$it") }
        val INGOTS: Map<String, DeferredItem<Item>> = METALS.associateWith { ITEMS.registerSimpleItem("${it}_ingot") }
        val DUSTS: Map<String, DeferredItem<Item>> = METALS.associateWith { ITEMS.registerSimpleItem("${it}_dust") }
        val TIN_NUGGET: DeferredItem<Item> = ITEMS.registerSimpleItem("tin_nugget")

        val MACHINE_BLOCK: DeferredBlock<MachineBlock> = BLOCKS.registerBlock(
            "crusher",
            { p -> MachineBlock(p) },
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
        )
        val MACHINE_ITEM = ITEMS.registerSimpleBlockItem(MACHINE_BLOCK)

        val FURNACE_BLOCK: DeferredBlock<FurnaceBlock> = BLOCKS.registerBlock(
            "furnace",
            { p -> FurnaceBlock(p) },
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
        )
        val FURNACE_ITEM = ITEMS.registerSimpleBlockItem(FURNACE_BLOCK)
    }



    init {
        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        ModBlockEntities.TYPES.register(modEventBus)
        ModMenus.MENUS.register(modEventBus)
        modEventBus.addListener(::addCreative)
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ClientEvents::onRegisterScreens)
        }
        modEventBus.addListener(::registerCapabilities)

        LOGGER.info("Radtech mod initialized!")
    }

    private fun registerCapabilities(event: RegisterCapabilitiesEvent) {
        event.registerBlockEntity(PowerCapabilities.POWER, ModBlockEntities.MACHINE.get()) { be, _ -> be.power }
    }

    private fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        val content: List<ItemLike> = when (event.tabKey) {
            CreativeModeTabs.NATURAL_BLOCKS -> ORES.values.map { it.get() }
            CreativeModeTabs.FUNCTIONAL_BLOCKS -> listOf(MACHINE_ITEM.get(), FURNACE_ITEM.get())
            CreativeModeTabs.INGREDIENTS ->
                (RAW.values + INGOTS.values + TIN_NUGGET + DUSTS.values).map { it.get() }
            else -> return
        }
        content.forEach { event.accept(it) }
    }
}