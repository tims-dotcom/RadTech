package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.menu.MachineMenu
import org.tim.radtech.registry.ModBlockEntities

class MachineBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.MACHINE.get(), pos, state), MenuProvider {

    val items = object : ItemStackHandler(2) {
        override fun onContentsChanged(slot: Int) = setChanged()
    }

    val power = PowerBuffer(TIER, CAPACITY, INPUT_AMPS, 0)

    var progress = 0
    var maxProgress = 100

    val data = object : ContainerData {
        override fun get(index: Int) = when (index) {
            PROGRESS -> progress
            MAX_PROGRESS -> maxProgress
            else -> PowerSync.read(power, index - POWER_OFFSET)
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                PROGRESS -> progress = value
                MAX_PROGRESS -> maxProgress = value
            }
        }

        override fun getCount() = DATA_COUNT
    }

    override fun getDisplayName(): Component = Component.translatable("block.radtech.crusher")

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        MachineMenu(id, inv, this, data)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.put("inv", items.serializeNBT(registries))
        tag.putInt("progress", progress)
        power.save(tag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        items.deserializeNBT(registries, tag.getCompound("inv"))
        progress = tag.getInt("progress")
        power.load(tag)
    }

    companion object {
        val TIER = VoltageTier.INDUSTRIAL
        const val INPUT_AMPS = 1
        const val CAPACITY = 400_000L
        val USE_PER_TICK = TIER.volts.toLong() * INPUT_AMPS

        const val PROGRESS = 0
        const val MAX_PROGRESS = 1
        const val POWER_OFFSET = 2
        const val DATA_COUNT = POWER_OFFSET + PowerSync.SLOTS

        private val RECIPES = mapOf(
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
            "radtech:tungsten_ore" to "radtech:tungsten_dust"
        )

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: MachineBlockEntity) {
            if (level.isClientSide) return
            val input = be.items.getStackInSlot(0)
            val result = RECIPES[BuiltInRegistries.ITEM.getKey(input.item).toString()]
                ?.let { BuiltInRegistries.ITEM.get(ResourceLocation.parse(it)) }
                ?.let { ItemStack(it, 2) }
            if (input.isEmpty || result == null || result.isEmpty) {
                if (be.progress != 0) {
                    be.progress = 0
                    be.setChanged()
                }
                return
            }
            val out = be.items.getStackInSlot(1)
            val fits = out.isEmpty ||
                    (ItemStack.isSameItemSameComponents(out, result) && out.count + result.count <= out.maxStackSize)
            if (!fits || !be.power.consume(USE_PER_TICK)) return
            if (++be.progress >= be.maxProgress) {
                be.progress = 0
                be.items.extractItem(0, 1, false)
                be.items.insertItem(1, result.copy(), false)
            }
            be.setChanged()
        }
    }
}