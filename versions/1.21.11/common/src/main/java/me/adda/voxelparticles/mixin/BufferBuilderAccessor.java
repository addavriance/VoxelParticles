package me.adda.voxelparticles.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
	@Accessor("buffer")
	ByteBufferBuilder voxelparticles$getBuffer();

	@Accessor("format")
	VertexFormat voxelparticles$getFormat();

	@Accessor("vertices")
	int voxelparticles$getVertices();

	@Accessor("vertices")
	void voxelparticles$setVertices(int vertices);

	@Accessor("vertexPointer")
	void voxelparticles$setVertexPointer(long pointer);

	@Accessor("elementsToFill")
	int voxelparticles$getElementsToFill();

	@Accessor("building")
	boolean voxelparticles$isBuilding();
}
