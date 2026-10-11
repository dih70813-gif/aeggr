package com.commandwheel;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.commandwheel.WheelConfig.Node;

/** Edit the name (and command) of one slice. */
public class ItemEditScreen extends Screen {
	private final Screen parent;
	private final Node node;
	private final Runnable onCancel;
	private EditBox nameBox;
	private EditBox commandBox;

	public ItemEditScreen(Screen parent, Node node, Runnable onCancel) {
		super(Component.literal(node.isFolder() ? "Folder" : "Command"));
		this.parent = parent;
		this.node = node;
		this.onCancel = onCancel;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int y = this.height / 2 - 50;

		nameBox = this.addRenderableWidget(new EditBox(this.font, cx - 130, y + 12, 260, 20, Component.literal("Name")));
		nameBox.setMaxLength(24);
		nameBox.setValue(node.name == null ? "" : node.name);

		if (!node.isFolder()) {
			commandBox = this.addRenderableWidget(new EditBox(this.font, cx - 130, y + 52, 260, 20, Component.literal("Command")));
			commandBox.setMaxLength(256);
			commandBox.setHint(Component.literal("for example: home base"));
			commandBox.setValue(node.command == null ? "" : node.command);
		}

		this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> {
					String name = nameBox.getValue().trim();
					node.name = name.isEmpty() ? "Unnamed" : name;

					if (!node.isFolder()) {
						String cmd = commandBox.getValue().trim();
						node.command = cmd.startsWith("/") ? cmd.substring(1) : cmd;
					}

					WheelConfig.get().save();
					this.minecraft.gui.setScreen(parent);
				})
				.pos(cx - 130, y + 90).size(127, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.onClose())
				.pos(cx + 3, y + 90).size(127, 20).build());

		this.setInitialFocus(nameBox);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		int cx = this.width / 2;
		int y = this.height / 2 - 50;

		graphics.text(this.font, this.title.getString(), cx - this.font.width(this.title.getString()) / 2, y - 14, 0xFFFFFFFF, true);
		graphics.text(this.font, "Name", cx - 130, y, 0xFFAAAAAA, true);

		if (!node.isFolder()) {
			graphics.text(this.font, "Command (without the /)", cx - 130, y + 40, 0xFFAAAAAA, true);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		onCancel.run();
		this.minecraft.gui.setScreen(parent);
	}
}
