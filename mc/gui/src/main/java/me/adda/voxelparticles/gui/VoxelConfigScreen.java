package me.adda.voxelparticles.gui;

import com.mojang.serialization.Codec;
import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.config.VoxelConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public final class VoxelConfigScreen extends OptionsSubScreen {
	private static final String KEY = "voxelparticles.config.";
	private static final int DISTANCE_STEP = 8;

	private static final int TAB_WIDTH = 80;
	private static final int TAB_SPACING = 4;
	private static final int HEADER_HEIGHT = 56;

	private static @Nullable VoxelConfig.Category selected;

	public VoxelConfigScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable(KEY + "title"));
	}

	@Override
	public void removed() {
		super.removed();
		VoxelParticles.saveConfig();
	}

	@Override
	protected void addTitle() {
		layout.setHeaderHeight(HEADER_HEIGHT);

		LinearLayout header = layout.addToHeader(LinearLayout.vertical().spacing(8));
		header.defaultCellSetting().alignHorizontallyCenter();
		header.addChild(new StringWidget(title, font));

		LinearLayout tabs = header.addChild(LinearLayout.horizontal().spacing(TAB_SPACING));
		tabs.addChild(tab(null));

		for (VoxelConfig.Category category : VoxelConfig.Category.values()) {
			tabs.addChild(tab(category));
		}
	}

	@Override
	protected void addOptions() {
		VoxelConfig cfg = VoxelParticles.config();

		if (selected == null) {
			addGeneral(cfg);
		} else {
			addCategory(cfg.settings(selected));
		}
	}

	private Button tab(@Nullable VoxelConfig.Category category) {
		Button button = Button.builder(
			Component.translatable(KEY + "tab." + (category == null ? "general" : id(category))),
			b -> {
				selected = category;
				ScreenNav.open(new VoxelConfigScreen(lastScreen)); // The layout is built once per screen, so a tab switch opens a fresh one
			}
		).width(TAB_WIDTH).build();

		button.active = selected != category;
		return button;
	}

	private void addGeneral(VoxelConfig cfg) {
		list.addBig(toggle("enabled", cfg.enabled, v -> cfg.enabled = v));

		list.addSmall(
			slider("lod_distance", 0, 32, Math.round(cfg.lodDistance / DISTANCE_STEP),
				v -> v == 0 ? Component.translatable(KEY + "unlimited") : Component.translatable(KEY + "blocks", v * DISTANCE_STEP),
				v -> cfg.lodDistance = v * DISTANCE_STEP),
			slider("max_resolution", 4, 32, cfg.maxResolution,
				v -> Component.translatable(KEY + "pixels", v), v -> cfg.maxResolution = v)
		);
	}

	private void addCategory(VoxelConfig.Settings settings) {
		list.addBig(toggle("category_enabled", settings.enabled, v -> settings.enabled = v));
		list.addBig(motion(settings));

		list.addSmall(
			slider("motion_speed", 0, 30, Math.round(settings.motionSpeed * 10), v -> times(v / 10F), v -> settings.motionSpeed = v / 10F),
			slider("depth", 1, 30, Math.round(settings.depth * 10), v -> times(v / 10F), v -> settings.depth = v / 10F),
			slider("shading", 0, 100, Math.round(settings.shading * 100), v -> Component.literal(v + "%"), v -> settings.shading = v / 100F)
		);

		if (selected != VoxelConfig.Category.PARTICLE) {
			list.addSmall(toggle("tumble_debris", settings.tumbleDebris, v -> settings.tumbleDebris = v));
		}
	}

	private static OptionInstance<VoxelConfig.Motion> motion(VoxelConfig.Settings settings) {
		return new OptionInstance<>(KEY + "motion",
			value -> Tooltip.create(Component.translatable(KEY + "motion." + id(value) + ".tooltip")),
			(caption, value) -> Component.translatable(KEY + "motion." + id(value)),
			new OptionInstance.Enum<>(Arrays.asList(VoxelConfig.Motion.values()),
				Codec.STRING.xmap(VoxelConfig.Motion::valueOf, VoxelConfig.Motion::name)),
			settings.motion,
			v -> changed(() -> settings.motion = v));
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

	private static String id(Enum<?> value) {
		return value.name().toLowerCase(Locale.ROOT);
	}
}
