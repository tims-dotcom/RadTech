package org.tim.radtech

import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.tim.radtech.block.MachineBlock
import org.tim.radtech.client.ClientEvents
import org.tim.radtech.registry.ModBlockEntities
import org.tim.radtech.registry.ModMenus

@Mod(Radtech.MODID)
class Radtech(modEventBus: IEventBus) {

    companion object {
        const val MODID = "radtech"
        val LOGGER: Logger = LogManager.getLogger(MODID)

        val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(MODID)
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MODID)

        /** Метали мода: ім'я -> (руда, сира руда, пил, злиток). */
        private val METALS = listOf("tin", "aluminum", "lithium", "neodymium", "titanium", "tungsten")

        private fun machineProps() =
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).requiresCorrectToolForDrops()

        // Руди (блок + BlockItem)
        val ORES: Map<String, DeferredBlock<Block>> = METALS.associateWith { metal ->
            val name = "${metal}_ore"
            val block = BLOCKS.registerSimpleBlock(name, machineProps())
            ITEMS.registerSimpleBlockItem(name, block)
            block
        }

        val MACHINE_BLOCK: DeferredBlock<MachineBlock> =
            BLOCKS.registerBlock("crusher", ::MachineBlock, machineProps())
        val MACHINE_ITEM: DeferredItem<BlockItem> =
            ITEMS.registerSimpleBlockItem("crusher", MACHINE_BLOCK)

        val RAW: Map<String, DeferredItem<Item>> = METALS.associateWith { ITEMS.registerSimpleItem("raw_$it") }
        val INGOTS: Map<String, DeferredItem<Item>> = METALS.associateWith { ITEMS.registerSimpleItem("${it}_ingot") }
        val DUSTS: Map<String, DeferredItem<Item>> = METALS.associateWith { ITEMS.registerSimpleItem("${it}_dust") }
        val TIN_NUGGET: DeferredItem<Item> = ITEMS.registerSimpleItem("tin_nugget")
    }

    init {
        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        ModBlockEntities.TYPES.register(modEventBus)
        ModMenus.MENUS.register(modEventBus)

        // Тип події вказуємо явно: Kotlin-лямбди стирають generic-типи,
        // і addListener(Consumer<T>) не зміг би визначити подію.
        modEventBus.addListener(EventPriority.NORMAL, false, BuildCreativeModeTabContentsEvent::class.java) { addCreative(it) }
        if (FMLEnvironment.dist.isClient) {
            modEventBus.addListener(EventPriority.NORMAL, false, RegisterMenuScreensEvent::class.java) { ClientEvents.onRegisterScreens(it) }
        }
    }

    private fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        val content: List<ItemLike> = when (event.tabKey) {
            CreativeModeTabs.NATURAL_BLOCKS -> ORES.values.map { it.get() }
            CreativeModeTabs.FUNCTIONAL_BLOCKS -> listOf(MACHINE_ITEM.get())
            CreativeModeTabs.INGREDIENTS ->
                (RAW.values + INGOTS.values + TIN_NUGGET + DUSTS.values).map { it.get() }
            else -> return
        }
        content.forEach(event::accept)
    }
}
