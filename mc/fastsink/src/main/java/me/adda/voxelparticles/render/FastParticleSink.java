package me.adda.voxelparticles.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.mixin.BufferBuilderAccessor;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteOrder;

final class FastParticleSink implements VertexSink {
	private static final boolean SUPPORTED;
	private static final int STRIDE = ParticleLayout.STRIDE;
	private static final int POS = ParticleLayout.POSITION;
	private static final int UV0 = ParticleLayout.UV0;
	private static final int COLOR = ParticleLayout.COLOR;
	private static final int UV2 = ParticleLayout.UV2;

	static {
		SUPPORTED = ParticleLayout.VALID && ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN && STRIDE == 28;
		if (!SUPPORTED) {
			VoxelParticles.LOG.info("Fast particle path disabled (unexpected vertex layout)");
		}
	}

	private BufferBuilderAccessor target;
	private long pointer;
	boolean bind(VertexConsumer consumer) {
		if (!SUPPORTED || !(consumer instanceof BufferBuilder)) {
			return false;
		}

		BufferBuilderAccessor accessor = (BufferBuilderAccessor) consumer;

		if (accessor.voxelparticles$getFormat() != DefaultVertexFormat.PARTICLE
			|| !accessor.voxelparticles$isBuilding()
			|| accessor.voxelparticles$getElementsToFill() != 0) {
			return false;
		}

		this.target = accessor;
		return true;
	}

	@Override
	public void begin(int vertexCount) {
		long start = target.voxelparticles$getBuffer().reserve(vertexCount * STRIDE);
		int vertices = target.voxelparticles$getVertices() + vertexCount;

		if (vertices >= 16777215) {
			throw new IllegalStateException("Trying to write too many vertices (>16777215) into BufferBuilder");
		}

		target.voxelparticles$setVertices(vertices);
		target.voxelparticles$setVertexPointer(start + (long) (vertexCount - 1) * STRIDE);
		this.pointer = start;
	}

	@Override
	public void vertex(float x, float y, float z, float u, float v, int argb, int light) {
		long p = pointer;

		MemoryUtil.memPutFloat(p + POS, x);
		MemoryUtil.memPutFloat(p + POS + 4, y);
		MemoryUtil.memPutFloat(p + POS + 8, z);
		MemoryUtil.memPutFloat(p + UV0, u);
		MemoryUtil.memPutFloat(p + UV0 + 4, v);
		MemoryUtil.memPutInt(p + COLOR, (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16));
		MemoryUtil.memPutInt(p + UV2, light);

		pointer = p + STRIDE;
	}
}
