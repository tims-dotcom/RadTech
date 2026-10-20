package org.tim.radtech.item

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import org.tim.radtech.block.BareCableBlock
import org.tim.radtech.block.BatteryBlockEntity
import org.tim.radtech.block.CableBlock
import org.tim.radtech.block.ProcessingMachineBlockEntity
import org.tim.radtech.block.ThermalGeneratorBlockEntity
import org.tim.radtech.energy.CableType
import org.tim.radtech.energy.PowerCapabilities
import org.tim.radtech.energy.PowerFormat
import org.tim.radtech.energy.PowerNetworks

class MultimeterItem(props: Properties) : Item(props) {

    override fun useOn(ctx: UseOnContext): InteractionResult {
        val level = ctx.level
        val pos = ctx.clickedPos
        val player = ctx.player ?: return InteractionResult.PASS

        if (!level.isClientSide) {
            val state = level.getBlockState(pos)
            val block = state.block
            val be = level.getBlockEntity(pos)
            val side = ctx.clickedFace

            // Звук цифрового вимірювача
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.8f)

            // Іскорки вимірювання
            (level as? ServerLevel)?.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                pos.x + 0.5 + side.stepX * 0.5,
                pos.y + 0.5 + side.stepY * 0.5,
                pos.z + 0.5 + side.stepZ * 0.5,
                3, 0.1, 0.1, 0.1, 0.02
            )

            val cableType = (block as? CableBlock)?.type ?: (block as? BareCableBlock)?.type

            if (cableType != null) {
                // Вимірювання кабелю
                val network = PowerNetworks.getNetwork(level, pos)
                val totalTransferred = network?.lastTransferred ?: 0L
                val volts = cableType.tier.volts
                val amps = totalTransferred.toDouble() / volts
                val maxAmps = cableType.maxAmps
                val percent = (amps * 100 / maxAmps).toInt()
                val isBare = block is BareCableBlock
                val cableName = "${if (isBare) "Голий " else ""}${cableType.id.replaceFirstChar { it.uppercase() }} кабель"

                val bar = makeProgressBar(percent)
                val statusText = if (amps > maxAmps) "§c⚠ ПЕРЕВАНТАЖЕННЯ ($percent%)" else "§a✓ В нормі ($percent%)"

                player.sendSystemMessage(Component.literal("§6⚡ ━━━ [ §eМУЛЬТИМЕТР: КАБЕЛЬ §6] ━━━ ⚡"))
                player.sendSystemMessage(Component.literal("§7├─ Лінія:      §f$cableName §7(Макс: §e${maxAmps} А§7)"))
                player.sendSystemMessage(Component.literal("§7├─ Напруга:    §b$volts В §8(Industrial)"))
                player.sendSystemMessage(Component.literal("§7├─ Струм:      §e${String.format("%.1f", amps)} А §8/ §7${maxAmps} А  $bar"))
                player.sendSystemMessage(Component.literal("§7├─ Потужність: §a${PowerFormat.power(totalTransferred)}"))
                player.sendSystemMessage(Component.literal("§7└─ Стан:       $statusText"))
                player.sendSystemMessage(Component.literal("§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"))
                return InteractionResult.SUCCESS
            }

            if (be is BatteryBlockEntity) {
                // Вимірювання акумулятора
                val stored = be.power.stored
                val cap = be.power.capacity
                val percent = if (cap > 0) (stored * 100 / cap).toInt() else 0
                val amps = be.lastAmps
                val bar = makeProgressBar(percent)
                val isHot = state.getValue(org.tim.radtech.block.BatteryBlock.OVERHEATED)
                val statusText = if (isHot) "§c⚠ КРИТИЧНИЙ ПЕРЕГРІВ! (§e${be.heatTicks}/80§c)" 
                                 else if (be.heatTicks > 30) "§6▲ НАГРІВ (§e${be.heatTicks}/80§6)" 
                                 else "§a✓ НОРМА (§e${be.heatTicks}/80§a)"

                player.sendSystemMessage(Component.literal("§6⚡ ━━━ [ §eМУЛЬТИМЕТР: АКУМУЛЯТОР §6] ━━━ ⚡"))
                player.sendSystemMessage(Component.literal("§7├─ Пристрій:   §fПромисловий Акумулятор"))
                player.sendSystemMessage(Component.literal("§7├─ Заряд:      §e${PowerFormat.energy(stored)} §8/ §7${PowerFormat.energy(cap)} $bar"))
                player.sendSystemMessage(Component.literal("§7├─ Напруга:    §b${be.power.tier.volts} В"))
                player.sendSystemMessage(Component.literal("§7├─ Потік:      §e${String.format("%.1f", amps)} А §8(§a${PowerFormat.power((amps * be.power.tier.volts).toLong())}§8)"))
                player.sendSystemMessage(Component.literal("§7└─ Стан:       $statusText"))
                player.sendSystemMessage(Component.literal("§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"))
                return InteractionResult.SUCCESS
            }

            if (be is ThermalGeneratorBlockEntity) {
                // Вимірювання генератора
                val isBurning = be.burnTime > 0
                val genAmps = if (isBurning) ThermalGeneratorBlockEntity.OUTPUT_AMPS else 0
                val statusText = if (isBurning) "§a✓ ГЕНЕРАЦІЯ (${be.burnTime / 20}с палива)" else "§7○ ОЧІКУВАННЯ ПАЛИВА"

                player.sendSystemMessage(Component.literal("§6⚡ ━━━ [ §eМУЛЬТИМЕТР: ГЕНЕРАТОР §6] ━━━ ⚡"))
                player.sendSystemMessage(Component.literal("§7├─ Пристрій:   §fТеплогенератор"))
                player.sendSystemMessage(Component.literal("§7├─ Вихід:      §b${ThermalGeneratorBlockEntity.TIER.volts} В §8| §e$genAmps А"))
                player.sendSystemMessage(Component.literal("§7├─ Потужність: §a${PowerFormat.power(ThermalGeneratorBlockEntity.GENERATION)}"))
                player.sendSystemMessage(Component.literal("§7├─ Буфер:      §e${PowerFormat.energy(be.power.stored)} §8/ §7${PowerFormat.energy(be.power.capacity)}"))
                player.sendSystemMessage(Component.literal("§7└─ Стан:       $statusText"))
                player.sendSystemMessage(Component.literal("§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"))
                return InteractionResult.SUCCESS
            }

            if (be is ProcessingMachineBlockEntity) {
                // Вимірювання машини
                val power = be.power
                val percent = if (power.capacity > 0) (power.stored * 100 / power.capacity).toInt() else 0
                val isWorking = be.progress > 0
                val statusText = if (isWorking) "§a✓ ПРАЦЮЄ (${be.progress * 100 / be.maxProgress}%)" else "§7○ ПРОСТІЙ"

                player.sendSystemMessage(Component.literal("§6⚡ ━━━ [ §eМУЛЬТИМЕТР: ОБЛАДНАННЯ §6] ━━━ ⚡"))
                player.sendSystemMessage(Component.literal("§7├─ Машина:     §f${be.displayName.string}"))
                player.sendSystemMessage(Component.literal("§7├─ Споживання: §b${power.tier.volts} В §8| §e${power.maxInputAmps} А"))
                player.sendSystemMessage(Component.literal("§7├─ Енергія:    §e${PowerFormat.energy(power.stored)} §8/ §7${PowerFormat.energy(power.capacity)} (${percent}%)"))
                player.sendSystemMessage(Component.literal("§7└─ Стан:       $statusText"))
                player.sendSystemMessage(Component.literal("§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"))
                return InteractionResult.SUCCESS
            }

            // Будь-який інший блок із живленням
            val storage = level.getCapability(PowerCapabilities.POWER, pos, side)
            if (storage != null) {
                player.sendSystemMessage(Component.literal("§6⚡ ━━━ [ §eМУЛЬТИМЕТР: ЕЛЕКТРОБЛОК §6] ━━━ ⚡"))
                player.sendSystemMessage(Component.literal("§7├─ Напруга: §b${storage.tier.volts} В"))
                player.sendSystemMessage(Component.literal("§7├─ Енергія: §e${PowerFormat.energy(storage.stored)} §8/ §7${PowerFormat.energy(storage.capacity)}"))
                player.sendSystemMessage(Component.literal("§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"))
                return InteractionResult.SUCCESS
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    private fun makeProgressBar(percent: Int): String {
        val total = 8
        val filled = ((percent.coerceIn(0, 100) * total) / 100)
        val color = if (percent > 100) "§c" else if (percent > 75) "§6" else "§a"
        val sb = StringBuilder("§8[")
        sb.append(color)
        for (i in 0 until total) {
            if (i < filled) sb.append("■") else sb.append("§7□")
        }
        sb.append("§8]")
        return sb.toString()
    }
}
