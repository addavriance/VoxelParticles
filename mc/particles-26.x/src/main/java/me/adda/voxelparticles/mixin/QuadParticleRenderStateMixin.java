package me.adda.voxelparticles.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.adda.voxelparticles.render.ParticleVoxelizer;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(value = QuadParticleRenderState.class, priority = 900)
public abstract class QuadParticleRenderStateMixin {
	private static final String FOR_EACH = "Lnet/minecraft/client/renderer/state/level/QuadParticleRenderState$Storage;forEachParticle(Lnet/minecraft/client/renderer/state/level/QuadParticleRenderState$ParticleConsumer;)V";
	private static final String COUNT = "Lnet/minecraft/client/renderer/state/level/QuadParticleRenderState$Storage;count()I";

	@Unique
	private final Map<SingleQuadParticle.Layer, Integer> voxelparticles$quads = new IdentityHashMap<>();

	@Unique
	private int voxelparticles$verticesBefore;

	@Inject(method = "prepare", at = @At("HEAD"))
	private void voxelparticles$reset(CallbackInfoReturnable<?> cir) {
		voxelparticles$quads.clear();
	}

	// Vanilla sizes the buffer for exactly four vertices per particle, voxels need more
	@WrapOperation(method = "prepare", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;exactlySized(I)Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;"))
	private ByteBufferBuilder voxelparticles$growableBuffer(int capacity, Operation<ByteBufferBuilder> original) {
		return new ByteBufferBuilder(capacity);
	}

	@Inject(method = "prepare", at = @At(value = "INVOKE", target = FOR_EACH))
	private void voxelparticles$beginLayer(
			CallbackInfoReturnable<?> cir,
			@Local Map.Entry<SingleQuadParticle.Layer, ?> entry, @Local BufferBuilder builder
	) {
		voxelparticles$verticesBefore = ((BufferBuilderAccessor) builder).voxelparticles$getVertices();
		ParticleVoxelizer.beginLayer(entry.getKey());
	}

	@Inject(method = "prepare", at = @At(value = "INVOKE", target = FOR_EACH, shift = At.Shift.AFTER))
	private void voxelparticles$endLayer(
			CallbackInfoReturnable<?> cir,
			@Local Map.Entry<SingleQuadParticle.Layer, ?> entry, @Local BufferBuilder builder
	) {
		ParticleVoxelizer.endLayer();

		int written = ((BufferBuilderAccessor) builder).voxelparticles$getVertices() - voxelparticles$verticesBefore;
		voxelparticles$quads.put(entry.getKey(), written / 4);
	}

	// The draw range comes from the particle count, replace it with the quads actually written
	@ModifyExpressionValue(method = "prepare", at = @At(value = "INVOKE", target = COUNT))
	private int voxelparticles$actualQuadCount(int original, @Local Map.Entry<SingleQuadParticle.Layer, ?> entry) {
		Integer quads = voxelparticles$quads.get(entry.getKey());
		return quads != null ? quads : original;
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
