package me.adda.voxelparticles.mixin;

import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureSheetParticle.class)
public interface TextureSheetParticleAccessor {
	@Accessor("sprite")
	TextureAtlasSprite voxelparticles$getSprite();
}
