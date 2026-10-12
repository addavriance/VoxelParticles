package me.adda.voxelparticles.gui;

import com.mojang.serialization.Codec;
import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.config.VoxelConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public final class VoxelConfigScreen extends OptionsSubScreen {
	private static final String KEY = "voxelparticles.config.";
	private static final int DISTANCE_STEP = 8;

	public VoxelConfigScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable(KEY + "title"));
	}

	@Override
	public void removed() {
		super.removed();
		VoxelParticles.saveConfig();
	}

	@Override
	protected void addOptions() {
		VoxelConfig cfg = VoxelParticles.config();

		list.addBig(toggle("enabled", cfg.enabled, v -> cfg.enabled = v));
		list.addBig(motion(cfg));

		list.addSmall(
			slider("motion_speed", 0, 30, Math.round(cfg.motionSpeed * 10), v -> times(v / 10F), v -> cfg.motionSpeed = v / 10F),
			slider("depth", 1, 30, Math.round(cfg.depth * 10), v -> times(v / 10F), v -> cfg.depth = v / 10F),
			slider("shading", 0, 100, Math.round(cfg.shading * 100), v -> Component.literal(v + "%"), v -> cfg.shading = v / 100F),
			toggle("tumble_debris", cfg.tumbleDebris, v -> cfg.tumbleDebris = v),
			toggle("block_particles", cfg.blockParticles, v -> cfg.blockParticles = v),
			toggle("item_particles", cfg.itemParticles, v -> cfg.itemParticles = v),
			slider("lod_distance", 0, 32, Math.round(cfg.lodDistance / DISTANCE_STEP),
				v -> v == 0 ? Component.translatable(KEY + "unlimited") : Component.translatable(KEY + "blocks", v * DISTANCE_STEP),
				v -> cfg.lodDistance = v * DISTANCE_STEP),
			slider("max_resolution", 4, 32, cfg.maxResolution,
				v -> Component.translatable(KEY + "pixels", v), v -> cfg.maxResolution = v)
		);
	}

	private static OptionInstance<VoxelConfig.Motion> motion(VoxelConfig cfg) {
		return new OptionInstance<>(KEY + "motion",
			value -> Tooltip.create(Component.translatable(KEY + "motion." + id(value) + ".tooltip")),
			(caption, value) -> Component.translatable(KEY + "motion." + id(value)),
			new OptionInstance.Enum<>(Arrays.asList(VoxelConfig.Motion.values()),
				Codec.STRING.xmap(VoxelConfig.Motion::valueOf, VoxelConfig.Motion::name)),
			cfg.motion,
			v -> changed(() -> cfg.motion = v));
	}

	private static OptionInstance<Boolean> toggle(String key, boolean value, Consumer<Boolean> setter) {
		return OptionInstance.createBoolean(KEY + key, tooltip(key), value, v -> changed(() -> setter.accept(v)));
	}

	private static OptionInstance<Integer> slider(String key, int min, int max, int value,
												  IntFunction<Component> label, Consumer<Integer> setter) {
		return new OptionInstance<>(KEY + key, tooltip(key),
			(caption, v) -> Options.genericValueLabel(caption, label.apply(v)),
			new OptionInstance.IntRange(min, max, false), value,
			v -> changed(() -> setter.accept(v)));
	}

	private static void changed(Runnable apply) {
		apply.run();
		VoxelParticles.onConfigChanged();
	}

	private static <T> OptionInstance.TooltipSupplier<T> tooltip(String key) {
		return OptionInstance.cachedConstantTooltip(Component.translatable(KEY + key + ".tooltip"));
	}

	private static Component times(float value) {
		return Component.literal(String.format(Locale.ROOT, "%.1fx", value));
	}

	private static String id(VoxelConfig.Motion motion) {
		return motion.name().toLowerCase(Locale.ROOT);
	}
}
