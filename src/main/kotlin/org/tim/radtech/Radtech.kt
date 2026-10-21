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
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.event. BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import org.apache.logging.log4j.LogManager
import org.tim.radtech.block.AlarmSirenBlock
import org.tim.radtech.block.BareCableBlock
import org.tim.radtech.block.BatteryBlock
import org.tim.radtech.block.BoilerCasingBlock
import org.tim.radtech.block.CableBlock
import org.tim.radtech.block.CableItem
import org.tim.radtech.block.CircuitBreakerBlock
import org.tim.radtech.block.ConnectorBlock
import org.tim.radtech.block.FurnaceBlock
import org.tim.radtech.block.IndustrialLampBlock
import org.tim.radtech.block.LargeThermalGeneratorBlock
import org.tim.radtech.block.LithiumBatteryBlock
import org.tim.radtech.block.MachineBlock
import org.tim.radtech.block.PanelMeterBlock
import org.tim.radtech.block.PressBlock
import org.tim.radtech.block.QuarryBlock
import org.tim.radtech.block.QuarryFrameBlock
import org.tim.radtech.block.RollingMillBlock
import org.tim.radtech.block.SolarPanelBlock
import org.tim.radtech.block.ThermalGeneratorBlock
import org.tim.radtech.block.TransformerBlock
import org.tim.radtech.client.ClientEvents
import org.tim.radtech.energy.CableType
import org.tim.radtech.energy.PowerNetworks
import org.tim.radtech.item.ElectricChainsawItem
import org.tim.radtech.item.ElectricDrillItem
import org.tim.radtech.item.GuideBookItem
import org.tim.radtech.item.MultimeterItem
import org.tim.radtech.item.WireCoilItem
import org.tim.radtech.item.WireCutterItem
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
        val PLATE_METALS = listOf("copper", "tin", "iron", "aluminum", "titanium")

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

        // Пластини (мідь, олово, залізо, алюміній, титан)
        val PLATES: Map<String, DeferredItem<Item>> = PLATE_METALS.associateWith { ITEMS.registerSimpleItem("${it}_plate") }

        // Нові компоненти з металів мода та побічні деталі для крафту заводів
        val NEODYMIUM_MAGNET: DeferredItem<Item> = ITEMS.registerSimpleItem("neodymium_magnet")
        val TUNGSTEN_ELEMENT: DeferredItem<Item> = ITEMS.registerSimpleItem("tungsten_element")
        val MACHINE_CASING: DeferredItem<Item> = ITEMS.registerSimpleItem("machine_casing")
        val COPPER_COIL: DeferredItem<Item> = ITEMS.registerSimpleItem("copper_coil")
        val ELECTRIC_MOTOR: DeferredItem<Item> = ITEMS.registerSimpleItem("electric_motor")
        val ELECTRONIC_CIRCUIT: DeferredItem<Item> = ITEMS.registerSimpleItem("electronic_circuit")
        val LITHIUM_CELL: DeferredItem<Item> = ITEMS.registerSimpleItem("lithium_cell")

        // Гума (ізоляція для кабелів)
        val RUBBER: DeferredItem<Item> = ITEMS.registerSimpleItem("rubber")

        // Конектор для підвісних проводів
        val CONNECTOR_BLOCK: DeferredBlock<ConnectorBlock> = BLOCKS.registerBlock(
            "connector",
            { p -> ConnectorBlock(p) },
            BlockBehaviour.Properties.of().strength(1.5f).noOcclusion()
        )
        val CONNECTOR_ITEM = ITEMS.registerSimpleBlockItem(CONNECTOR_BLOCK)

        // Промисловий світильник з вбудованими конекторами
        val INDUSTRIAL_LAMP_BLOCK: DeferredBlock<IndustrialLampBlock> = BLOCKS.registerBlock(
            "industrial_lamp",
            { p -> IndustrialLampBlock(p) },
            BlockBehaviour.Properties.of().strength(2.0f).noOcclusion()
                .lightLevel { if (it.getValue(IndustrialLampBlock.LIT)) 15 else 0 }
        )
        val INDUSTRIAL_LAMP_ITEM = ITEMS.registerSimpleBlockItem(INDUSTRIAL_LAMP_BLOCK)

        // Кусачки для регулювання натягу та зняття проводів
        val WIRE_CUTTER: DeferredItem<WireCutterItem> = ITEMS.register("wire_cutter") { ->
            WireCutterItem(Item.Properties().stacksTo(1))
        }

        // Котушки підвісних проводів (ізольовані)
        val WIRE_COILS: Map<CableType, DeferredItem<WireCoilItem>> = CableType.entries.associateWith { type ->
            ITEMS.register("${type.id}_wire_coil") { ->
                WireCoilItem(type, false, Item.Properties().stacksTo(64))
            }
        }

        // Котушки підвісних проводів (голі)
        val BARE_WIRE_COILS: Map<CableType, DeferredItem<WireCoilItem>> = CableType.entries.associateWith { type ->
            ITEMS.register("bare_${type.id}_wire_coil") { ->
                WireCoilItem(type, true, Item.Properties().stacksTo(64))
            }
        }

        // Літій-іонний акумулятор високої ємності
        val LITHIUM_BATTERY_BLOCK: DeferredBlock<LithiumBatteryBlock> = BLOCKS.registerBlock(
            "lithium_battery",
            { p -> LithiumBatteryBlock(p) },
            BlockBehaviour.Properties.of().strength(4.0f, 4.0f).requiresCorrectToolForDrops()
        )
        val LITHIUM_BATTERY_ITEM = ITEMS.registerSimpleBlockItem(LITHIUM_BATTERY_BLOCK)

        // Настінний вольтметр / амперметр
        val PANEL_METER_BLOCK: DeferredBlock<PanelMeterBlock> = BLOCKS.registerBlock(
            "panel_meter",
            { p -> PanelMeterBlock(p) },
            BlockBehaviour.Properties.of().strength(2.0f).noOcclusion()
        )
        val PANEL_METER_ITEM = ITEMS.registerSimpleBlockItem(PANEL_METER_BLOCK)

        // Аварійна сирена зі звуком та спалахами
        val ALARM_SIREN_BLOCK: DeferredBlock<AlarmSirenBlock> = BLOCKS.registerBlock(
            "alarm_siren",
            { p -> AlarmSirenBlock(p) },
            BlockBehaviour.Properties.of().strength(2.5f).noOcclusion()
        )
        val ALARM_SIREN_ITEM = ITEMS.registerSimpleBlockItem(ALARM_SIREN_BLOCK)

        // Багатоблочний котел: Кожух котла (Boiler Casing)
        val BOILER_CASING_BLOCK: DeferredBlock<BoilerCasingBlock> = BLOCKS.registerBlock(
            "boiler_casing",
            { p -> BoilerCasingBlock(p) },
            BlockBehaviour.Properties.of().strength(4.0f).requiresCorrectToolForDrops()
        )
        val BOILER_CASING_ITEM = ITEMS.registerSimpleBlockItem(BOILER_CASING_BLOCK)

        // Багатоблочний котел: Контролер великого теплогенератора (3x3x3 ТЕС)
        val LARGE_THERMAL_GEN_BLOCK: DeferredBlock<LargeThermalGeneratorBlock> = BLOCKS.registerBlock(
            "large_thermal_generator",
            { p -> LargeThermalGeneratorBlock(p) },
            BlockBehaviour.Properties.of().strength(4.0f).requiresCorrectToolForDrops()
        )
        val LARGE_THERMAL_GEN_ITEM = ITEMS.registerSimpleBlockItem(LARGE_THERMAL_GEN_BLOCK)

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

        // Мультиблочний кар'єр (Quarry)
        val QUARRY_BLOCK: DeferredBlock<QuarryBlock> = BLOCKS.registerBlock(
            "quarry",
            { p -> QuarryBlock(p) },
            BlockBehaviour.Properties.of().strength(5.0f, 6.0f).requiresCorrectToolForDrops()
        )
        val QUARRY_ITEM = ITEMS.registerSimpleBlockItem(QUARRY_BLOCK)

        val QUARRY_FRAME_BLOCK: DeferredBlock<QuarryFrameBlock> = BLOCKS.registerBlock(
            "quarry_frame",
            { p -> QuarryFrameBlock(p) },
            BlockBehaviour.Properties.of().strength(5.0f, 6.0f).requiresCorrectToolForDrops()
        )
        val QUARRY_FRAME_ITEM = ITEMS.registerSimpleBlockItem(QUARRY_FRAME_BLOCK)

        // Електроінструменти (працюють на заряді)
        val ELECTRIC_DRILL: DeferredItem<ElectricDrillItem> = ITEMS.register("electric_drill") { ->
            ElectricDrillItem(Item.Properties().stacksTo(1))
        }

        val ELECTRIC_CHAINSAW: DeferredItem<ElectricChainsawItem> = ITEMS.register("electric_chainsaw") { ->
            ElectricChainsawItem(Item.Properties().stacksTo(1))
        }

        // Окрема вкладка мода RadTech у креативному інвентарі
        val RADTECH_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = CREATIVE_MODE_TABS.register("radtech_tab") { ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.radtech"))
                .icon { ItemStack(BATTERY_ITEM.get()) }
                .displayItems { _, output ->
                    // Блоки енергії та машини
                    output.accept(GUIDE_BOOK.get())
                    output.accept(MULTIMETER.get())
                    output.accept(WIRE_CUTTER.get())
                    output.accept(ELECTRIC_DRILL.get())
                    output.accept(ELECTRIC_CHAINSAW.get())
                    output.accept(CONNECTOR_ITEM.get())
                    output.accept(INDUSTRIAL_LAMP_ITEM.get())
                    output.accept(PANEL_METER_ITEM.get())
                    output.accept(ALARM_SIREN_ITEM.get())
                    output.accept(CIRCUIT_BREAKER_ITEM.get())
                    output.accept(BATTERY_ITEM.get())
                    output.accept(LITHIUM_BATTERY_ITEM.get())
                    output.accept(SOLAR_PANEL_ITEM.get())
                    output.accept(TRANSFORMER_ITEM.get())
                    output.accept(THERMAL_GENERATOR_ITEM.get())
                    output.accept(LARGE_THERMAL_GEN_ITEM.get())
                    output.accept(BOILER_CASING_ITEM.get())
                    output.accept(QUARRY_ITEM.get())
                    output.accept(QUARRY_FRAME_ITEM.get())
                    output.accept(MACHINE_ITEM.get())
                    output.accept(FURNACE_ITEM.get())
                    output.accept(PRESS_ITEM.get())
                    output.accept(ROLLING_MILL_ITEM.get())
                    // Котушки підвісних проводів
                    WIRE_COILS.values.forEach { output.accept(it.get()) }
                    BARE_WIRE_COILS.values.forEach { output.accept(it.get()) }
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
                    output.accept(NEODYMIUM_MAGNET.get())
                    output.accept(TUNGSTEN_ELEMENT.get())
                    output.accept(MACHINE_CASING.get())
                    output.accept(COPPER_COIL.get())
                    output.accept(ELECTRIC_MOTOR.get())
                    output.accept(ELECTRONIC_CIRCUIT.get())
                    output.accept(LITHIUM_CELL.get())
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
            modEventBus.addListener(ClientEvents::onRegisterRenderers)
        }
        NeoForge.EVENT_BUS.addListener<LevelTickEvent.Post>(PowerNetworks::onLevelTick)
        NeoForge.EVENT_BUS.addListener<ServerTickEvent.Post>(PowerNetworks::onServerTick)
        NeoForge.EVENT_BUS.addListener<ServerStoppedEvent>(PowerNetworks::onServerStopped)
        NeoForge.EVENT_BUS.addListener<PlayerEvent.PlayerLoggedInEvent> { event ->
            unlockAllModRecipes(event.entity as? ServerPlayer)
        }
        NeoForge.EVENT_BUS.addListener<PlayerEvent.PlayerRespawnEvent> { event ->
            unlockAllModRecipes(event.entity as? ServerPlayer)
        }
        LOGGER.info("Radtech mod initialized!")
    }

    private fun unlockAllModRecipes(player: ServerPlayer?) {
        val sp = player ?: return
        val server = sp.server ?: return
        val modRecipes = server.recipeManager.recipes.filter { it.id.namespace == MODID }
        if (modRecipes.isNotEmpty()) {
            sp.awardRecipes(modRecipes)
        }
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
