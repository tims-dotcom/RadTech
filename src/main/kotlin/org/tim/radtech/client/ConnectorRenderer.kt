package org.tim.radtech.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.AABB
import org.tim.radtech.energy.CableType
import org.tim.radtech.energy.IWireConnectable
import org.tim.radtech.energy.WireConnection
import kotlin.math.sqrt

class ConnectorRenderer<T>(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<T>
        where T : BlockEntity, T : IWireConnectable {

    companion object {
        private val WHITE_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/misc/white.png")
    }

    override fun render(
        be: T,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val level = be.level ?: return
        val pos = be.blockPos
        val anchorA = be.getWireAnchor()
        val startX = (anchorA.x - pos.x).toFloat()
        val startY = (anchorA.y - pos.y).toFloat()
        val startZ = (anchorA.z - pos.z).toFloat()

        val buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(WHITE_TEXTURE))
        val pose = poseStack.last()

        for (conn in be.connections) {
            // Малюємо кожен провід лише з одного кінця
            if (pos.asLong() > conn.targetPos.asLong()) continue

            val targetBe = level.getBlockEntity(conn.targetPos) as? IWireConnectable ?: continue
            val anchorB = targetBe.getWireAnchor()
            val endX = (anchorB.x - pos.x).toFloat()
            val endY = (anchorB.y - pos.y).toFloat()
            val endZ = (anchorB.z - pos.z).toFloat()

            renderThickWireTube(buffer, pose, startX, startY, startZ, endX, endY, endZ, conn, packedLight)
        }
    }

    private fun getWireColor(conn: WireConnection): Triple<Float, Float, Float> {
        // Однотонний колір дроту залежно від металу та наявності ізоляції
        return when (conn.type) {
            CableType.TIN      -> if (conn.isBare) Triple(0.72f, 0.76f, 0.80f) else Triple(0.20f, 0.22f, 0.25f)
            CableType.COPPER   -> if (conn.isBare) Triple(0.85f, 0.48f, 0.24f) else Triple(0.22f, 0.16f, 0.14f)
            CableType.ALUMINUM -> if (conn.isBare) Triple(0.86f, 0.89f, 0.92f) else Triple(0.26f, 0.28f, 0.30f)
            CableType.IRON     -> if (conn.isBare) Triple(0.52f, 0.55f, 0.58f) else Triple(0.15f, 0.16f, 0.18f)
        }
    }

    private fun renderThickWireTube(
        buffer: VertexConsumer,
        pose: PoseStack.Pose,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        conn: WireConnection,
        packedLight: Int
    ) {
        val segments = 16
        val sag = conn.sag
        val radius = 0.0225f // В 2 рази тонший (було 0.045f)

        val (r, g, b) = getWireColor(conn)

        val dx = x2 - x1
        val dy = y2 - y1
        val dz = z2 - z1

        // Горизонтальний перпендикулярний вектор U (стабільний вздовж усього проводу)
        val horizLen = sqrt(dx * dx + dz * dz)
        val ux: Float
        val uz: Float
        if (horizLen > 1e-4f) {
            ux = (-dz / horizLen) * radius
            uz = (dx / horizLen) * radius
        } else {
            ux = radius
            uz = 0.0f
        }

        // Обчислюємо центральні точки параболічного провисання та 4 кути перерізу кабелю в кожній точці
        val corners = Array(segments + 1) { i ->
            val t = i.toFloat() / segments
            val px = x1 + dx * t
            val pz = z1 + dz * t
            val sagOffset = 4.0f * sag * t * (1.0f - t)
            val py = y1 + dy * t - sagOffset

            val txRaw = dx
            val tyRaw = dy - 4.0f * sag * (1.0f - 2.0f * t)
            val tzRaw = dz
            val tLen = sqrt(txRaw * txRaw + tyRaw * tyRaw + tzRaw * tzRaw).coerceAtLeast(1e-4f)
            val tx = txRaw / tLen
            val ty = tyRaw / tLen
            val tz = tzRaw / tLen

            val vx: Float
            val vy: Float
            val vz: Float
            if (horizLen > 1e-4f) {
                vx = ty * uz
                vy = tz * ux - tx * uz
                vz = -ty * ux
            } else {
                vx = 0.0f
                vy = 0.0f
                vz = radius
            }

            floatArrayOf(
                px - ux - vx, py - vy, pz - uz - vz, // C0 (0, 1, 2)
                px + ux - vx, py - vy, pz + uz - vz, // C1 (3, 4, 5)
                px + ux + vx, py + vy, pz + uz + vz, // C2 (6, 7, 8)
                px - ux + vx, py + vy, pz - uz + vz  // C3 (9, 10, 11)
            )
        }

        // Малюємо всі 4 грані однотонним кольором як незалежні QUADS
        for (i in 0 until segments) {
            val c = corners[i]
            val n = corners[i + 1]

            // Верхня грань (C3 -> C2 -> N2 -> N3)
            emitQuad(
                buffer, pose,
                c[9], c[10], c[11],
                c[6], c[7], c[8],
                n[6], n[7], n[8],
                n[9], n[10], n[11],
                r, g, b, packedLight
            )

            // Нижня грань (C0 -> C1 -> N1 -> N0)
            emitQuad(
                buffer, pose,
                c[0], c[1], c[2],
                c[3], c[4], c[5],
                n[3], n[4], n[5],
                n[0], n[1], n[2],
                r, g, b, packedLight
            )

            // Права бічна грань (C1 -> C2 -> N2 -> N1)
            emitQuad(
                buffer, pose,
                c[3], c[4], c[5],
                c[6], c[7], c[8],
                n[6], n[7], n[8],
                n[3], n[4], n[5],
                r, g, b, packedLight
            )

            // Ліва бічна грань (C3 -> C0 -> N0 -> N3)
            emitQuad(
                buffer, pose,
                c[9], c[10], c[11],
                c[0], c[1], c[2],
                n[0], n[1], n[2],
                n[9], n[10], n[11],
                r, g, b, packedLight
            )
        }
    }

    private fun emitQuad(
        buffer: VertexConsumer,
        pose: PoseStack.Pose,
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        x3: Float, y3: Float, z3: Float,
        r: Float, g: Float, b: Float,
        packedLight: Int
    ) {
        buffer.addVertex(pose, x0, y0, z0)
            .setColor(r, g, b, 1.0f)
            .setUv(0f, 0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(pose, 0f, 1f, 0f)
        buffer.addVertex(pose, x1, y1, z1)
            .setColor(r, g, b, 1.0f)
            .setUv(0f, 1f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(pose, 0f, 1f, 0f)
        buffer.addVertex(pose, x2, y2, z2)
            .setColor(r, g, b, 1.0f)
            .setUv(1f, 1f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(pose, 0f, 1f, 0f)
        buffer.addVertex(pose, x3, y3, z3)
            .setColor(r, g, b, 1.0f)
            .setUv(1f, 0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(pose, 0f, 1f, 0f)
    }

    override fun shouldRenderOffScreen(be: T): Boolean = true

    override fun getRenderBoundingBox(be: T): AABB = AABB.INFINITE

    override fun getViewDistance(): Int = 96
}
