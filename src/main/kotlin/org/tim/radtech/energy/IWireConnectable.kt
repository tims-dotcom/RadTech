package org.tim.radtech.energy

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

interface IWireConnectable {
    val connections: MutableList<WireConnection>
    fun addConnection(conn: WireConnection)
    fun removeConnection(targetPos: BlockPos): Boolean
    fun setSag(targetPos: BlockPos, sag: Float)
    fun getWireAnchor(): Vec3
    fun getConnectedPowerPos(): BlockPos?
}
