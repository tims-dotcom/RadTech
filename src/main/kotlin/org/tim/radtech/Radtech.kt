package org.tim.radtech

import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import org.apache.logging.log4j.LogManager
import org.tim.radtech.block.CableBlock
import org.tim.radtech.block.CableItem
import org.tim.radtech.block.FurnaceBlock
import org.tim.radtech.block.MachineBlock
import org.tim.radtech.block.ThermalGeneratorBlock
import org.tim.radtech.client.ClientEvents
import org.tim.radtech.energy.CableType
import org.tim.radtech.energy.PowerNetworks
import org.tim.radtech.registry.ModBlockEntities
import org.tim.radtech.registry.ModCapabilities
import org.tim.radtech.registry.ModMenus

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

        val THERMAL_GENERATOR_BLOCK: DeferredBlock<ThermalGeneratorBlock> = BLOCKS.registerBlock(
            "thermal_generator",
            { p -> ThermalGeneratorBlock(p) },
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
        )
        val THERMAL_GENERATOR_ITEM = ITEMS.registerSimpleBlockItem(THERMAL_GENERATOR_BLOCK)

        val CABLES: Map<CableType, DeferredBlock<CableBlock>> = CableType.entries.associateWith { type ->
            BLOCKS.registerBlock(
                "${type.id}_cable",
                { p -> CableBlock(type, p) },
                BlockBehaviour.Properties.of().strength(1.0f).noOcclusion()
            )
        }

        val CABLE_ITEMS = CableType.entries.associateWith { type ->
            ITEMS.register("${type.id}_cable") { ->
                CableItem(CABLES.getValue(type).get(), type, Item.Properties())
            }
        }
    }

    init {
        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        ModBlockEntities.TYPES.register(modEventBus)
        ModMenus.MENUS.register(modEventBus)
        modEventBus.addListener(::addCreative)
        modEventBus.addListener(ModCapabilities::register)
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ClientEvents::onRegisterScreens)
        }
        NeoForge.EVENT_BUS.addListener<LevelTickEvent.Post>(PowerNetworks::onLevelTick)
        NeoForge.EVENT_BUS.addListener<ServerTickEvent.Post>(PowerNetworks::onServerTick)
        NeoForge.EVENT_BUS.addListener<ServerStoppedEvent>(PowerNetworks::onServerStopped)
        LOGGER.info("Radtech mod initialized!")
    }

    private fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        val content: List<ItemLike> = when (event.tabKey) {
            CreativeModeTabs.NATURAL_BLOCKS -> ORES.values.map { it.get() }
            CreativeModeTabs.FUNCTIONAL_BLOCKS ->
                listOf(MACHINE_ITEM.get(), FURNACE_ITEM.get(), THERMAL_GENERATOR_ITEM.get()) +
                        CABLE_ITEMS.values.map { it.get() }
            CreativeModeTabs.INGREDIENTS ->
                (RAW.values + INGOTS.values + TIN_NUGGET + DUSTS.values).map { it.get() }
            else -> return
        }
        content.forEach { event.accept(it) }
    }
}
