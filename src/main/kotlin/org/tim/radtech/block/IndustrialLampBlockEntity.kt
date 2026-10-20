package org.tim.radtech.block

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import org.tim.radtech.energy.IWireConnectable
import org.tim.radtech.energy.PowerBuffer
import org.tim.radtech.energy.PowerNetworks
import org.tim.radtech.energy.VoltageTier
import org.tim.radtech.energy.WireConnection
import org.tim.radtech.registry.ModBlockEntities

class IndustrialLampBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.LAMP.get(), pos, state), IWireConnectable {

    override val connections = ArrayList<WireConnection>()

    override fun onLoad() {
        super.onLoad()
        level?.let { if (!it.isClientSide) PowerNetworks.registerConnectable(it, blockPos) }
    }

    // Лампа споживає 380 В (до 1 А, але споживання всього 76 Вт на тік = 0.2 А)
    val power = PowerBuffer(VoltageTier.INDUSTRIAL, 20_000L, 1, 0)

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
            level?.let {
                it.sendBlockUpdated(blockPos, blockState, blockState, 3)
            }
        }
    }

    override fun getWireAnchor(): Vec3 {
        // Клеми контактів на базі світильника (відповідає 3D моделі lamb.json)
        val facing = blockState.getValue(IndustrialLampBlock.FACING)
        return Vec3(
            blockPos.x + 0.5 - facing.stepX * 0.3125,
            blockPos.y + 0.5 - facing.stepY * 0.3125,
            blockPos.z + 0.5 - facing.stepZ * 0.3125
        )
    }

    override fun getConnectedPowerPos(): BlockPos? = blockPos

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        val list = ListTag()
        for (c in connections) {
            list.add(c.serializeNBT())
        }
        tag.put("wires", list)
        power.save(tag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        connections.clear()
        if (tag.contains("wires", Tag.TAG_LIST.toInt())) {
            val list = tag.getList("wires", Tag.TAG_COMPOUND.toInt())
            for (i in 0 until list.size) {
                val c = WireConnection.deserializeNBT(list.getCompound(i))
                if (c != null) connections.add(c)
            }
        }
        power.load(tag)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        val tag = super.getUpdateTag(registries)
        saveAdditional(tag, registries)
        return tag
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? =
        ClientboundBlockEntityDataPacket.create(this)

    companion object {
        const val POWER_PER_TICK = 76L // 380 В * 0.2 А = 76 Вт

        fun tick(level: Level, pos: BlockPos, state: BlockState, be: IndustrialLampBlockEntity) {
            if (level.isClientSide) return
            val hasPower = be.power.consume(POWER_PER_TICK)
            val currentLit = state.getValue(IndustrialLampBlock.LIT)
            if (hasPower != currentLit) {
                level.setBlock(pos, state.setValue(IndustrialLampBlock.LIT, hasPower), 3)
            }
        }
    }
}
