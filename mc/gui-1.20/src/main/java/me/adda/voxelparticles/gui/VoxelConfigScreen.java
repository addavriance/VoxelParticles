package me.adda.voxelparticles.gui;

import com.mojang.serialization.Codec;
import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.config.VoxelConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public final class VoxelConfigScreen extends OptionsSubScreen {
	private static final String KEY = "voxelparticles.config.";
	private static final int DISTANCE_STEP = 8;
	private static final int BUTTON_WIDTH = 100;

	private @Nullable VoxelConfig.Category selected;
	private OptionsList list;

	public VoxelConfigScreen(Screen parent) {
		this(parent, null);
	}

	private VoxelConfigScreen(Screen parent, @Nullable VoxelConfig.Category selected) {
		super(parent, Minecraft.getInstance().options, Component.translatable(KEY + "title"));
		this.selected = selected;
	}

	@Override
	public void removed() {
		super.removed();
		VoxelParticles.saveConfig();
	}

	@Override
	protected void init() {
		list = new OptionsList(minecraft, width, height, 32, height - 32);
		addWidget(list);

		VoxelConfig cfg = VoxelParticles.config();
		if (selected == null) {
			addGeneral(cfg);
		} else {
			addCategory(cfg.settings(selected));
		}

		int row = height - 27;
		int gap = 4;
		int x = width / 2 - (BUTTON_WIDTH * 4 + gap * 3) / 2;
		addRenderableWidget(categoryButton(null, x, row));
		for (VoxelConfig.Category category : VoxelConfig.Category.values()) {
			x += BUTTON_WIDTH + gap;
			addRenderableWidget(categoryButton(category, x, row));
		}

		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
			.bounds(width / 2 - 100, height - 52, 200, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
		list.render(graphics, mouseX, mouseY, partialTick);
	}

	private void addSmall(OptionInstance<?>... options) {
		list.addSmall(options);
	}

	private Button categoryButton(@Nullable VoxelConfig.Category category, int x, int y) {
		Component label = Component.translatable(KEY + "tab." + (category == null ? "general" : id(category)));
		return Button.builder(label, b -> ScreenNav.open(new VoxelConfigScreen(lastScreen, category)))
			.bounds(x, y, BUTTON_WIDTH, 20).build();
	}

	private void addGeneral(VoxelConfig cfg) {
		list.addBig(toggle("enabled", cfg.enabled, v -> cfg.enabled = v));
		addSmall(
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
		addSmall(
			slider("motion_speed", 0, 30, Math.round(settings.motionSpeed * 10), v -> times(v / 10F), v -> settings.motionSpeed = v / 10F),
			slider("depth", 1, 30, Math.round(settings.depth * 10), v -> times(v / 10F), v -> settings.depth = v / 10F),
			slider("shading", 0, 100, Math.round(settings.shading * 100), v -> Component.literal(v + "%"), v -> settings.shading = v / 100F)
		);
		if (selected != VoxelConfig.Category.PARTICLE) {
			addSmall(toggle("tumble_debris", settings.tumbleDebris, v -> settings.tumbleDebris = v));
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
			new OptionInstance.IntRange(min, max), value,
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
