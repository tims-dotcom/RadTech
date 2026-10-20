package org.tim.radtech

import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
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
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import org.apache.logging.log4j.LogManager
import org.tim.radtech.block.BareCableBlock
import org.tim.radtech.block.BatteryBlock
import org.tim.radtech.block.CableBlock
import org.tim.radtech.block.CableItem
import org.tim.radtech.block.CircuitBreakerBlock
import org.tim.radtech.block.FurnaceBlock
import org.tim.radtech.block.MachineBlock
import org.tim.radtech.block.PressBlock
import org.tim.radtech.block.RollingMillBlock
import org.tim.radtech.block.SolarPanelBlock
import org.tim.radtech.block.ThermalGeneratorBlock
import org.tim.radtech.block.TransformerBlock
import org.tim.radtech.client.ClientEvents
import org.tim.radtech.energy.CableType
import org.tim.radtech.energy.PowerNetworks
import org.tim.radtech.item.GuideBookItem
import org.tim.radtech.item.MultimeterItem
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
        val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)

        val METALS = listOf("tin", "aluminum", "lithium", "neodymium", "titanium", "tungsten")
        val ALL_DUSTS = listOf("tin", "aluminum", "lithium", "neodymium", "titanium", "tungsten", "iron", "copper", "gold")
        // Метали для пластин (мають пластину у пресі)
        val PLATE_METALS = listOf("copper", "tin", "iron", "aluminum")

        val ORES: Map<String, DeferredBlock<Block>> = METALS.associateWith { metal ->
            BLOCKS.registerSimpleBlock(
                "${metal}_ore",
                BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
            ).also { ITEMS.registerSimpleBlockItem(it) }
        }

        val RAW: Map<String, DeferredItem<Item>> = METALS.associateWith { ITEMS.registerSimpleItem("raw_$it") }
        val INGOTS: Map<String, DeferredItem<Item>> = METALS.associateWith { ITEMS.registerSimpleItem("${it}_ingot") }
        val DUSTS: Map<String, DeferredItem<Item>> = ALL_DUSTS.associateWith { ITEMS.registerSimpleItem("${it}_dust") }
        val TIN_NUGGET: DeferredItem<Item> = ITEMS.registerSimpleItem("tin_nugget")

        // Пластини (мідь, олово, залізо, алюміній)
        val PLATES: Map<String, DeferredItem<Item>> = PLATE_METALS.associateWith { ITEMS.registerSimpleItem("${it}_plate") }

        // Гума (ізоляція для кабелів)
        val RUBBER: DeferredItem<Item> = ITEMS.registerSimpleItem("rubber")

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

        val PRESS_BLOCK: DeferredBlock<PressBlock> = BLOCKS.registerBlock(
            "press",
            { p -> PressBlock(p) },
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
        )
        val PRESS_ITEM = ITEMS.registerSimpleBlockItem(PRESS_BLOCK)

        val ROLLING_MILL_BLOCK: DeferredBlock<RollingMillBlock> = BLOCKS.registerBlock(
            "rolling_mill",
            { p -> RollingMillBlock(p) },
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
        )
        val ROLLING_MILL_ITEM = ITEMS.registerSimpleBlockItem(ROLLING_MILL_BLOCK)

        val BATTERY_BLOCK: DeferredBlock<BatteryBlock> = BLOCKS.registerBlock(
            "battery",
            { p -> BatteryBlock(p) },
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
        )
        val BATTERY_ITEM = ITEMS.registerSimpleBlockItem(BATTERY_BLOCK)

        val GUIDE_BOOK: DeferredItem<GuideBookItem> = ITEMS.register("guide_book") { ->
            GuideBookItem(Item.Properties().stacksTo(1))
        }

        val MULTIMETER: DeferredItem<MultimeterItem> = ITEMS.register("multimeter") { ->
            MultimeterItem(Item.Properties().stacksTo(1))
        }

        val CIRCUIT_BREAKER_BLOCK: DeferredBlock<CircuitBreakerBlock> = BLOCKS.registerBlock(
            "circuit_breaker",
            { p -> CircuitBreakerBlock(p) },
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
        )
        val CIRCUIT_BREAKER_ITEM = ITEMS.registerSimpleBlockItem(CIRCUIT_BREAKER_BLOCK)

        val SOLAR_PANEL_BLOCK: DeferredBlock<SolarPanelBlock> = BLOCKS.registerBlock(
            "solar_panel",
            { p -> SolarPanelBlock(p) },
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
        )
        val SOLAR_PANEL_ITEM = ITEMS.registerSimpleBlockItem(SOLAR_PANEL_BLOCK)

        val TRANSFORMER_BLOCK: DeferredBlock<TransformerBlock> = BLOCKS.registerBlock(
            "transformer",
            { p -> TransformerBlock(p) },
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()
        )
        val TRANSFORMER_ITEM = ITEMS.registerSimpleBlockItem(TRANSFORMER_BLOCK)

        // Окрема вкладка мода RadTech у креативному інвентарі
        val RADTECH_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = CREATIVE_MODE_TABS.register("radtech_tab") { ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.radtech"))
                .icon { ItemStack(BATTERY_ITEM.get()) }
                .displayItems { _, output ->
                    // Блоки енергії та машини
                    output.accept(GUIDE_BOOK.get())
                    output.accept(MULTIMETER.get())
                    output.accept(CIRCUIT_BREAKER_ITEM.get())
                    output.accept(BATTERY_ITEM.get())
                    output.accept(SOLAR_PANEL_ITEM.get())
                    output.accept(TRANSFORMER_ITEM.get())
                    output.accept(THERMAL_GENERATOR_ITEM.get())
                    output.accept(MACHINE_ITEM.get())
                    output.accept(FURNACE_ITEM.get())
                    output.accept(PRESS_ITEM.get())
                    output.accept(ROLLING_MILL_ITEM.get())
                    // Кабелі (ізольовані та голі)
                    CABLE_ITEMS.values.forEach { output.accept(it.get()) }
                    BARE_CABLE_ITEMS.values.forEach { output.accept(it.get()) }
                    // Руди
                    ORES.values.forEach { output.accept(it.get()) }
                    // Матеріали та компоненти
                    RAW.values.forEach { output.accept(it.get()) }
                    INGOTS.values.forEach { output.accept(it.get()) }
                    output.accept(TIN_NUGGET.get())
                    DUSTS.values.forEach { output.accept(it.get()) }
                    PLATES.values.forEach { output.accept(it.get()) }
                    output.accept(RUBBER.get())
                }
                .build()
        }

        // Ізольовані кабелі (існуючі)
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

        // Голі (неізольовані) кабелі — б'ють струмом
        val BARE_CABLES: Map<CableType, DeferredBlock<BareCableBlock>> = CableType.entries.associateWith { type ->
            BLOCKS.registerBlock(
                "bare_${type.id}_cable",
                { p -> BareCableBlock(type, p) },
                BlockBehaviour.Properties.of().strength(1.0f).noOcclusion()
            )
        }

        val BARE_CABLE_ITEMS = CableType.entries.associateWith { type ->
            ITEMS.register("bare_${type.id}_cable") { ->
                CableItem(BARE_CABLES.getValue(type).get(), type, Item.Properties())
            }
        }
    }

    init {
        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        CREATIVE_MODE_TABS.register(modEventBus)
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
                listOf(
                    CIRCUIT_BREAKER_ITEM.get(), BATTERY_ITEM.get(), SOLAR_PANEL_ITEM.get(),
                    TRANSFORMER_ITEM.get(), MACHINE_ITEM.get(), FURNACE_ITEM.get(),
                    PRESS_ITEM.get(), THERMAL_GENERATOR_ITEM.get(), ROLLING_MILL_ITEM.get()
                ) +
                        CABLE_ITEMS.values.map { it.get() } +
                        BARE_CABLE_ITEMS.values.map { it.get() }
            CreativeModeTabs.INGREDIENTS ->
                (RAW.values + INGOTS.values + TIN_NUGGET + DUSTS.values +
                 PLATES.values + RUBBER).map { it.get() }
            else -> return
        }
        content.forEach { event.accept(it) }
    }
}
