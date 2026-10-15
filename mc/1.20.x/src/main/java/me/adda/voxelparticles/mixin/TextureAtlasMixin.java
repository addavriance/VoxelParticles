package me.adda.voxelparticles.mixin;

import me.adda.voxelparticles.render.ParticleVoxelizer;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {
	@Inject(method = "upload", at = @At("TAIL"))
	private void voxelparticles$onUpload(SpriteLoader.Preparations preparations, CallbackInfo ci) {
		ParticleVoxelizer.invalidate(((TextureAtlas) (Object) this).location());
	}
}
