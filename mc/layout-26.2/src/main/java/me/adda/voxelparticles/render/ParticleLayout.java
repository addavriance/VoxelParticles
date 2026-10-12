package me.adda.voxelparticles.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

final class ParticleLayout {
	static final boolean VALID;
	static final int STRIDE, POSITION, UV0, COLOR, UV2;

	static {
		var format = DefaultVertexFormat.PARTICLE;
		var pos = format.getElement("Position");
		var uv0 = format.getElement("UV0");
		var color = format.getElement("Color");
		var uv2 = format.getElement("UV2");

		VALID = pos != null && uv0 != null && color != null && uv2 != null;
		STRIDE = format.getVertexSize();
		POSITION = VALID ? pos.offset() : 0;
		UV0 = VALID ? uv0.offset() : 0;
		COLOR = VALID ? color.offset() : 0;
		UV2 = VALID ? uv2.offset() : 0;
	}

	private ParticleLayout() {
	}
}
