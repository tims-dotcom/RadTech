package org.tim.radtech.energy

import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.capabilities.BlockCapability

object PowerCapabilities {
    val POWER: BlockCapability<IPowerStorage, Direction?> =
        BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("radtech", "power"), IPowerStorage::class.java)
}