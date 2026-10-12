package me.adda.voxelparticles.mixin;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TextureAtlas.class)
public interface TextureAtlasAccessor {
	@Accessor("sprites")
	List<TextureAtlasSprite> voxelparticles$getSprites();

	@Accessor("width")
	int voxelparticles$getWidth();

	@Accessor("height")
	int voxelparticles$getHeight();
}
