package me.adda.voxelparticles.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

final class ScreenNav {
	private ScreenNav() {
	}

	static void open(Screen screen) {
		Minecraft.getInstance().setScreenAndShow(screen);
	}
}
