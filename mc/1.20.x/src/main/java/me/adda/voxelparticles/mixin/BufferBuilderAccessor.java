package me.adda.voxelparticles.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
	@Accessor("buffer")
	ByteBuffer voxelparticles$getBuffer();

	@Accessor("format")
	VertexFormat voxelparticles$getFormat();

	@Accessor("vertices")
	int voxelparticles$getVertices();

	@Accessor("vertices")
	void voxelparticles$setVertices(int vertices);

	@Accessor("nextElementByte")
	int voxelparticles$getNextElementByte();

	@Accessor("nextElementByte")
	void voxelparticles$setNextElementByte(int nextElementByte);

	@Accessor("elementIndex")
	int voxelparticles$getElementIndex();

	@Accessor("building")
	boolean voxelparticles$isBuilding();

	@Invoker("ensureCapacity")
	void voxelparticles$ensureCapacity(int size);
}
