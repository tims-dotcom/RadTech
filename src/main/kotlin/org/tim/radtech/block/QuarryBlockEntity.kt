package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.Containers
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.items.IItemHandler
import net.neoforged.neoforge.items.ItemHandlerHelper
import net.neoforged.neoforge.items.ItemStackHandler
import org.tim.radtech.Radtech
import org.tim.radtech.energy.IWireConnectable
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerNetworks
import org.tim.radtech.energy.PowerSync
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.energy.WireConnection
import org.tim.radtech.menu.QuarryMenu
import org.tim.radtech.registry.ModBlockEntities

class QuarryBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.QUARRY.get(), pos, state), MenuProvider, IWireConnectable {

    override val connections = ArrayList<WireConnection>()

    val power = PowerBuffer(VoltageTier.INDUSTRIAL, CAPACITY, MAX_INPUT_AMPS, 0)

    val items = object : ItemStackHandler(18) {
        override fun onContentsChanged(slot: Int) = setChanged()
    }

    var isFormed: Boolean = false
    var isMining: Boolean = false
    var currentY: Int = pos.y - 1
    var currentRelX: Int = -RADIUS
    var currentRelZ: Int = -RADIUS
    var blocksMined: Int = 0

    val data = object : ContainerData {
        override fun get(index: Int): Int = when (index) {
            0, 1, 2, 3 -> PowerSync.read(power, index)
            4 -> currentY + 100 // зміщення щоб уникнути від'ємних чисел у контейнері
            5 -> blocksMined
            6 -> if (isFormed) 1 else 0
            7 -> if (isMining) 1 else 0
            else -> 0
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                4 -> currentY = value - 100
                5 -> blocksMined = value
                6 -> isFormed = value == 1
                7 -> isMining = value == 1
            }
        }

        override fun getCount(): Int = 8
    }

    override fun getDisplayName(): Component = Component.translatable("block.radtech.quarry")

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu =
        QuarryMenu(id, inv, this, data)

    override fun onLoad() {
        super.onLoad()
        level?.let { if (!it.isClientSide) PowerNetworks.registerConnectable(it, blockPos) }
    }

    override fun addConnection(conn: WireConnection) {
        connections.removeAll { it.targetPos == conn.targetPos }
        connections.add(conn)
        setChanged()
        level?.let {
            PowerNetworks.markDirty(it)
            it.sendBlockUpdated(blockPos, blockState, blockState, 3)
        }
    }

    override fun removeConnection(targetPos: BlockPos): Boolean {
        val removed = connections.removeAll { it.targetPos == targetPos }
        if (removed) {
            setChanged()
            level?.let {
                PowerNetworks.markDirty(it)
                it.sendBlockUpdated(blockPos, blockState, blockState, 3)
            }
        }
        return removed
    }

    override fun setSag(targetPos: BlockPos, sag: Float) {
        val conn = connections.firstOrNull { it.targetPos == targetPos }
        if (conn != null) {
            conn.sag = sag
            setChanged()
            level?.let { it.sendBlockUpdated(blockPos, blockState, blockState, 3) }
        }
    }

    override fun getWireAnchor(): Vec3 = Vec3(blockPos.x + 0.5, blockPos.y + 0.9, blockPos.z + 0.5)

    override fun getConnectedPowerPos(): BlockPos? = blockPos

    fun checkStructure(level: Level, pos: BlockPos): Boolean {
        // Перевіряємо 8 сусідніх блоків на тому ж рівні Y (3x3 основа)
        for (dx in -1..1) {
            for (dz in -1..1) {
                if (dx == 0 && dz == 0) continue
                val checkPos = pos.offset(dx, 0, dz)
                val block = level.getBlockState(checkPos).block
                if (block != Radtech.QUARRY_FRAME_BLOCK.get()) {
                    return false
                }
            }
        }
        return true
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        power.save(tag)
        tag.put("inv", items.serializeNBT(registries))
        tag.putBoolean("formed", isFormed)
        tag.putBoolean("mining", isMining)
        tag.putInt("currentY", currentY)
        tag.putInt("currentRelX", currentRelX)
        tag.putInt("currentRelZ", currentRelZ)
        tag.putInt("blocksMined", blocksMined)

        val wireList = ListTag()
        for (c in connections) wireList.add(c.serializeNBT())
        tag.put("wires", wireList)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        power.load(tag)
        if (tag.contains("inv")) items.deserializeNBT(registries, tag.getCompound("inv"))
        isFormed = tag.getBoolean("formed")
        isMining = tag.getBoolean("mining")
        currentY = tag.getInt("currentY")
        currentRelX = tag.getInt("currentRelX")
        currentRelZ = tag.getInt("currentRelZ")
        blocksMined = tag.getInt("blocksMined")

        connections.clear()
        if (tag.contains("wires", Tag.TAG_LIST.toInt())) {
            val list = tag.getList("wires", Tag.TAG_COMPOUND.toInt())
            for (i in 0 until list.size) {
                val c = WireConnection.deserializeNBT(list.getCompound(i))
                if (c != null) connections.add(c)
            }
        }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? =
        ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        val tag = super.getUpdateTag(registries)
        saveAdditional(tag, registries)
        return tag
    }

    companion object {
        const val CAPACITY = 1_000_000L
        const val MAX_INPUT_AMPS = 64
        const val ENERGY_PER_BLOCK = 800L
        const val RADIUS = 5 // 11x11 площа (від -5 до +5)

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: QuarryBlockEntity) {
            if (level.isClientSide) return
            val sLevel = level as? ServerLevel ?: return

            // Кожні 20 тіків перевіряємо мультиблочну структуру
            if (level.gameTime % 20 == 0L) {
                val formedNow = be.checkStructure(level, pos)
                if (formedNow != be.isFormed) {
                    be.isFormed = formedNow
                    be.setChanged()
                }
            }

            if (!be.isFormed) {
                if (be.isMining) {
                    be.isMining = false
                    level.setBlock(pos, state.setValue(QuarryBlock.LIT, false), 3)
                }
                return
            }

            // Працює кожні 6 тіків (~3.3 блоки на секунду)
            if (level.gameTime % 6 == 0L) {
                if (be.power.stored < ENERGY_PER_BLOCK || be.currentY < level.minBuildHeight) {
                    if (be.isMining) {
                        be.isMining = false
                        level.setBlock(pos, state.setValue(QuarryBlock.LIT, false), 3)
                    }
                    return
                }

                // Шукаємо наступний твердий блок
                var foundBlock = false
                var attempts = 0
                val maxAttemptsPerStep = 32
                val mutablePos = BlockPos.MutableBlockPos()

                while (!foundBlock && attempts < maxAttemptsPerStep && be.currentY >= level.minBuildHeight) {
                    mutablePos.set(pos.x + be.currentRelX, be.currentY, pos.z + be.currentRelZ)
                    val targetState = level.getBlockState(mutablePos)
                    attempts++

                    if (targetState.isAir || targetState.canBeReplaced() || targetState.getDestroySpeed(level, mutablePos) < 0.0f) {
                        // Повітря, рідина або незламний блок (бедрок) — пропускаємо
                        advancePosition(be, level)
                    } else {
                        // Твердий корисний блок — добуваємо!
                        foundBlock = true
                        be.power.extract(ENERGY_PER_BLOCK, false)

                        val immutableTarget = mutablePos.immutable()
                        val drops = Block.getDrops(targetState, sLevel, immutableTarget, null)
                        level.setBlock(immutableTarget, Blocks.AIR.defaultBlockState(), 3)
                        be.blocksMined++

                        // Спершу пробуємо скинути предмети у сусідню скриню зверху або позаду
                        depositDrops(sLevel, pos, drops, be.items)

                        // Звук та частинки буріння
                        level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_BREAK, SoundSource.BLOCKS, 0.7f, 1.2f)
                        sLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x + 0.5, pos.y + 1.1, pos.z + 0.5, 2, 0.2, 0.1, 0.2, 0.02)

                        advancePosition(be, level)
                        be.setChanged()
                    }
                }

                val shouldBeLit = foundBlock && be.power.stored >= ENERGY_PER_BLOCK
                if (be.isMining != shouldBeLit) {
                    be.isMining = shouldBeLit
                    level.setBlock(pos, state.setValue(QuarryBlock.LIT, shouldBeLit), 3)
                }
            }
        }

        private fun advancePosition(be: QuarryBlockEntity, level: Level) {
            be.currentRelX++
            if (be.currentRelX > RADIUS) {
                be.currentRelX = -RADIUS
                be.currentRelZ++
                if (be.currentRelZ > RADIUS) {
                    be.currentRelZ = -RADIUS
                    be.currentY--
                }
            }
        }

        private fun depositDrops(level: ServerLevel, pos: BlockPos, drops: List<ItemStack>, fallbackInv: ItemStackHandler) {
            // Перевіряємо скриню або контейнер зверху кар'єру
            val topHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.above(), Direction.DOWN)
            for (stack in drops) {
                var remaining = stack.copy()
                if (topHandler != null) {
                    remaining = ItemHandlerHelper.insertItem(topHandler, remaining, false)
                }
                if (!remaining.isEmpty) {
                    // Якщо немає скрині або вона повна — кладемо у внутрішній буфер
                    remaining = ItemHandlerHelper.insertItem(fallbackInv, remaining, false)
                }
                if (!remaining.isEmpty) {
                    // Якщо і буфер повний — викидаємо в світ біля кар'єру
                    Containers.dropItemStack(level, pos.x + 0.5, pos.y + 1.2, pos.z + 0.5, remaining)
                }
            }
        }
    }
}
