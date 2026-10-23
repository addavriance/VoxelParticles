package me.adda.voxelparticles.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

// Before 1.20.2 Screen.render leaves the background to the subclass
final class ScreenBackground {
	private ScreenBackground() {
	}

	static void draw(Screen screen, GuiGraphics graphics) {
		screen.renderBackground(graphics);
	}
}
