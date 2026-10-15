package me.adda.voxelparticles.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.List;

final class ParticleLayout {
	static final boolean VALID;
	static final int STRIDE, POSITION, UV0, COLOR, UV2;

	static {
		VertexFormat format = DefaultVertexFormat.PARTICLE;
		List<String> names = format.getElementAttributeNames();
		int position = -1, uv0 = -1, color = -1, uv2 = -1, offset = 0;

		for (int i = 0; i < names.size(); i++) {
			switch (names.get(i)) {
				case "Position" -> position = offset;
				case "UV0" -> uv0 = offset;
				case "Color" -> color = offset;
				case "UV2" -> uv2 = offset;
				default -> {
				}
			}
			offset += format.getElements().get(i).getByteSize();
		}

		VALID = position >= 0 && uv0 >= 0 && color >= 0 && uv2 >= 0;
		STRIDE = format.getVertexSize();
		POSITION = Math.max(position, 0);
		UV0 = Math.max(uv0, 0);
		COLOR = Math.max(color, 0);
		UV2 = Math.max(uv2, 0);
	}

	private ParticleLayout() {
	}
}
