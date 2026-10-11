package com.commandwheel;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class CommandWheelClient implements ClientModInitializer {
	public static KeyMapping openKey;
	private static boolean promptPending;

	@Override
	public void onInitializeClient() {
		WheelConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("commandwheel", "main"));
		// Default key is R. It can be changed in Options > Controls, or in the box shown when you join.
		openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.commandwheel.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, category));

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			WheelConfig cfg = WheelConfig.get();

			if (!cfg.keyPromptDone || cfg.showKeyPromptOnEveryJoin) {
				promptPending = true;
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}

			// Wait until the loading screen is gone before showing the box.
			if (promptPending && client.gui.screen() == null) {
				promptPending = false;
				client.gui.setScreen(new KeyPromptScreen());
				return;
			}

			while (openKey.consumeClick()) {
				if (client.gui.screen() == null) {
					client.gui.setScreen(new WheelScreen());
				}
			}
		});
	}
}
