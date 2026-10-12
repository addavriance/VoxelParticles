package me.adda.voxelparticles.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

final class ParticleLayout {
	static final boolean VALID;
	static final int STRIDE, POSITION, UV0, COLOR, UV2;

	static {
		var format = DefaultVertexFormat.PARTICLE;
		VALID = format.contains(VertexFormatElement.POSITION) && format.contains(VertexFormatElement.UV0)
			&& format.contains(VertexFormatElement.COLOR) && format.contains(VertexFormatElement.UV2);
		STRIDE = format.getVertexSize();
		POSITION = VALID ? format.getOffset(VertexFormatElement.POSITION) : 0;
		UV0 = VALID ? format.getOffset(VertexFormatElement.UV0) : 0;
		COLOR = VALID ? format.getOffset(VertexFormatElement.COLOR) : 0;
		UV2 = VALID ? format.getOffset(VertexFormatElement.UV2) : 0;
	}

	private ParticleLayout() {
	}
}
