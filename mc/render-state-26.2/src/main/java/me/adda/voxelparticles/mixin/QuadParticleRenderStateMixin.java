package me.adda.voxelparticles.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import me.adda.voxelparticles.render.ParticleVoxelizer;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QuadParticleRenderState.class, priority = 900)
public abstract class QuadParticleRenderStateMixin {
	@Inject(method = "buildLayer", at = @At("HEAD"))
	private void voxelparticles$beginLayer(SingleQuadParticle.Layer layer, VertexConsumer bufferBuilder, CallbackInfo ci) {
		ParticleVoxelizer.beginLayer(layer);
	}

	@Inject(method = "buildLayer", at = @At("RETURN"))
	private void voxelparticles$endLayer(SingleQuadParticle.Layer layer, VertexConsumer bufferBuilder, CallbackInfo ci) {
		ParticleVoxelizer.endLayer();
	}

	@Inject(method = "renderRotatedQuad", at = @At("HEAD"), cancellable = true)
	private void voxelparticles$renderVoxels(
			VertexConsumer builder, float x, float y, float z,
			float xRot, float yRot, float zRot, float wRot, float scale,
			float u0, float u1, float v0, float v1, int color, int lightCoords,
			CallbackInfo ci
	) {
		if (ParticleVoxelizer.render(builder, x, y, z, xRot, yRot, zRot, wRot, scale, u0, u1, v0, v1, color, lightCoords)) {
			ci.cancel();
		}
	}
}
