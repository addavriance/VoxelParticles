package me.adda.voxelparticles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.config.VoxelConfig;
import me.adda.voxelparticles.motion.MotionBridge;
import me.adda.voxelparticles.render.ParticleVoxelizer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SingleQuadParticle.class, priority = 900)
public abstract class SingleQuadParticleMixin extends Particle {
	private static final String RENDER_ROTATED = "Lnet/minecraft/client/particle/SingleQuadParticle;renderRotatedQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;Lorg/joml/Quaternionf;F)V";

	@Unique
	private float voxelparticles$landedAt = -1F;

	protected SingleQuadParticleMixin(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
	}

	@Shadow
	protected abstract void renderRotatedQuad(VertexConsumer consumer, Quaternionf rotation, float x, float y, float z, float partialTick);

	@Shadow
	public abstract float getQuadSize(float partialTick);

	@Shadow
	protected abstract float getU0();

	@Shadow
	protected abstract float getU1();

	@Shadow
	protected abstract float getV0();

	@Shadow
	protected abstract float getV1();

	@Unique
	private @Nullable TextureAtlasSprite voxelparticles$sprite() {
		Object self = this;
		return self instanceof TextureSheetParticle sheet ? ((TextureSheetParticleAccessor) sheet).voxelparticles$getSprite() : null;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = RENDER_ROTATED))
	private void voxelparticles$applyMotion(
			SingleQuadParticle self, VertexConsumer consumer, Camera camera,
			Quaternionf rotation, float partialTick, Operation<Void> original
	) {
		VoxelConfig cfg = VoxelParticles.config();
		TextureAtlasSprite sprite = voxelparticles$sprite();
		if (
				cfg.enabled && cfg.motion != VoxelConfig.Motion.BILLBOARD &&
				ParticleVoxelizer.allowsMotion(sprite) &&
				self.getFacingCameraMode() == SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ // Particles with their own orientation (flat ripples, vertical streaks) keep it
		) {
			int seed = System.identityHashCode(this) * 0x9E3779B9;
			float time = this.age + partialTick;

			Object particle = this;
			if (cfg.tumbleDebris && (particle instanceof TerrainParticle || particle instanceof BreakingItemParticle)) {
				if (this.onGround && this.voxelparticles$landedAt < 0) {
					this.voxelparticles$landedAt = time;
				}

				float settled = MotionBridge.applyDebris(rotation, seed, time, this.voxelparticles$landedAt);
				if (settled > 0) {
					// Crumbs use a quarter of their sprite; lift them by half a voxel so they rest on the ground
					int res = Math.max(1, Math.min(sprite.contents().width() / 4, cfg.maxResolution));
					float lift = settled * getQuadSize(partialTick) * cfg.depth / res;
					Vec3 cam = camera.getPosition();

					this.renderRotatedQuad(consumer, rotation,
						(float) (Mth.lerp(partialTick, this.xo, this.x) - cam.x()),
						(float) (Mth.lerp(partialTick, this.yo, this.y) - cam.y()) + lift,
						(float) (Mth.lerp(partialTick, this.zo, this.z) - cam.z()),
						partialTick);

					return;
				}
			} else {
				MotionBridge.apply(rotation, seed, time);
			}
		}
		original.call(self, consumer, camera, rotation, partialTick);
	}

	@Inject(method = "renderRotatedQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Quaternionf;FFFF)V", at = @At("HEAD"), cancellable = true)
	private void voxelparticles$renderVoxels(
			VertexConsumer consumer, Quaternionf rotation, float x, float y, float z,
			float partialTick, CallbackInfo ci
	) {
		Object particle = this;
		ParticleVoxelizer.Kind kind =
				particle instanceof TerrainParticle ? ParticleVoxelizer.Kind.BLOCK :
				particle instanceof BreakingItemParticle ? ParticleVoxelizer.Kind.ITEM :
				ParticleVoxelizer.Kind.PARTICLE;

		int color = voxelparticles$channel(this.alpha) << 24 | voxelparticles$channel(this.rCol) << 16
			| voxelparticles$channel(this.gCol) << 8 | voxelparticles$channel(this.bCol);

		if (ParticleVoxelizer.render(consumer, voxelparticles$sprite(), kind, x, y, z,
			rotation.x, rotation.y, rotation.z, rotation.w, getQuadSize(partialTick),
			getU0(), getU1(), getV0(), getV1(), color, getLightColor(partialTick))) {
			ci.cancel();
		}
	}

	@Unique
	private static int voxelparticles$channel(float value) {
		return Mth.clamp((int) (value * 255.0F), 0, 255);
	}
}
