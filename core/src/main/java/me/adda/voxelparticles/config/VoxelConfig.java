package me.adda.voxelparticles.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import me.adda.voxelparticles.VoxelParticles;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class VoxelConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public enum Motion {
		BILLBOARD,
		SWAY,
		SPIN,
		TUMBLE
	}

	public enum Category {
		BLOCK,
		ITEM,
		PARTICLE
	}

	public static final class Settings {
		public boolean enabled = true;
		public Motion motion = Motion.SWAY;

		public float depth = 1.0F;

		public float shading = 1.0F;

		public float motionSpeed = 1.0F;

		public boolean tumbleDebris = true;

		void sanitize() {
			if (motion == null) motion = Motion.SWAY;

			depth = clamp(depth, 0.1F, 4F);
			shading = clamp(shading, 0F, 1F);
			motionSpeed = clamp(motionSpeed, 0F, 5F);
		}
	}

	public boolean enabled = true;

	public int maxResolution = 16;

	public float lodDistance = 64.0F;

	public Settings blocks = new Settings();
	public Settings items = new Settings();
	public Settings particles = new Settings();

	public List<String> excludedSprites = new ArrayList<>(List.of(
		"minecraft:sweep_*",
		"minecraft:sonic_boom_*",
		"minecraft:flash",
		"minecraft:explosion_*",
		"minecraft:shriek",
		"minecraft:vibration",
		"minecraft:missingno"
	));

	private transient List<Pattern> compiled;

	public boolean isExcluded(String spriteId) {
		if (compiled == null) {
			List<Pattern> list = new ArrayList<>();

			for (String glob : excludedSprites) {
				String regex = Pattern.quote(glob.toLowerCase(Locale.ROOT)).replace("*", "\\E.*\\Q");
				list.add(Pattern.compile(regex));
			}

			compiled = list;
		}

		String id = spriteId.toLowerCase(Locale.ROOT);

		for (Pattern p : compiled) {
			if (p.matcher(id).matches()) {
				return true;
			}
		}

		return false;
	}

	public Settings settings(Category category) {
		return switch (category) {
			case BLOCK -> blocks;
			case ITEM -> items;
			case PARTICLE -> particles;
		};
	}

	public String shapeKey() {
		return blocks.depth + "|" + items.depth + "|" + particles.depth + "|" + maxResolution + "|" + excludedSprites;
	}

	public void sanitize() {
		if (blocks == null) blocks = new Settings();
		if (items == null) items = new Settings();
		if (particles == null) particles = new Settings();

		blocks.sanitize();
		items.sanitize();
		particles.sanitize();

		maxResolution = Math.max(4, Math.min(64, maxResolution));
		lodDistance = Math.max(0F, lodDistance);

		if (excludedSprites == null) excludedSprites = new ArrayList<>();
		compiled = null;
	}

	private static float clamp(float v, float min, float max) {
		return Float.isNaN(v) ? min : Math.max(min, Math.min(max, v));
	}

	public static VoxelConfig load(Path file) {
		VoxelConfig config = null;

		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
				config = GSON.fromJson(reader, VoxelConfig.class);
			} catch (IOException | JsonParseException e) {
				VoxelParticles.LOG.warn("Failed to read {}, using defaults", file, e);
			}
		}

		if (config == null) {
			config = new VoxelConfig();
		}

		config.sanitize();
		config.save(file);

		return config;
	}

	public void save(Path file) {
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			VoxelParticles.LOG.warn("Failed to save {}", file, e);
		}
	}
}
