package com.commandwheel;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

/** The box shown when you join: asks which key should open the wheel. */
public class KeyPromptScreen extends Screen {
	private final Screen parent;
	private boolean changed;
	private Button doneButton;

	public KeyPromptScreen() {
		this(null);
	}

	public KeyPromptScreen(Screen parent) {
		super(Component.literal("Command Wheel"));
		this.parent = parent;
	}

	private static String currentKeyName() {
		return KeyMappingHelper.getBoundKeyOf(CommandWheelClient.openKey).getDisplayName().getString();
	}

	@Override
	protected void init() {
		doneButton = this.addRenderableWidget(Button.builder(doneLabel(), button -> this.onClose())
				.pos(this.width / 2 - 100, this.height / 2 + 30)
				.size(200, 20)
				.build());
	}

	private Component doneLabel() {
		return Component.literal(changed ? "Done" : "Keep " + currentKeyName());
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = event.key();

		if (key == GLFW.GLFW_KEY_ESCAPE) {
			this.onClose();
			return true;
		}

		if (key != GLFW.GLFW_KEY_UNKNOWN) {
			CommandWheelClient.openKey.setKey(InputConstants.Type.KEYSYM.getOrCreate(key));
			KeyMapping.resetMapping();
			this.minecraft.options.save();
			changed = true;
			doneButton.setMessage(doneLabel());
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		WheelConfig cfg = WheelConfig.get();
		cfg.keyPromptDone = true;
		cfg.save();
		this.minecraft.gui.setScreen(parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		int cx = this.width / 2;
		int y = this.height / 2 - 60;

		centered(graphics, "Command Wheel", cx, y, 0xFFFFFFFF);
		centered(graphics, "Which key do you want to open the wheel with?", cx, y + 18, 0xFFFFFFFF);
		centered(graphics, "Press any key now to use it.", cx, y + 32, 0xFFAAAAAA);
		centered(graphics, "Current key: " + currentKeyName(), cx, y + 50, 0xFFFFFF55);
		centered(graphics, "You can change it later in Options > Controls,", cx, this.height / 2 + 62, 0xFF888888);
		centered(graphics, "or with the \"Open key\" button in the wheel's Edit screen.", cx, this.height / 2 + 74, 0xFF888888);
	}

	private void centered(GuiGraphicsExtractor graphics, String text, int cx, int y, int color) {
		graphics.text(this.font, text, cx - this.font.width(text) / 2, y, color, true);
	}
}
