package com.commandwheel;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import com.commandwheel.WheelConfig.Node;

/** Edit one level of the wheel (the top level, or the inside of a folder). */
public class EditScreen extends Screen {
	private final Screen parent;
	/** The folder being edited, or null for the top level. */
	private final Node folder;

	public EditScreen(Screen parent, Node folder) {
		super(Component.literal(folder == null ? "Edit wheel" : "Edit folder: " + folder.name));
		this.parent = parent;
		this.folder = folder;
	}

	private List<Node> items() {
		return folder == null ? WheelConfig.get().root : folder.children;
	}

	@Override
	protected void init() {
		List<Node> items = items();
		int left = this.width / 2 - 135;
		int y = 34;

		for (int i = 0; i < items.size(); i++) {
			final int index = i;
			final Node node = items.get(i);
			int x = left;

			String label = node.isFolder() ? node.name + "  (folder) >" : node.name + "  /" + node.command;
			this.addRenderableWidget(Button.builder(Component.literal(label), button -> {
						if (node.isFolder()) {
							this.minecraft.gui.setScreen(new EditScreen(this, node));
						} else {
							this.minecraft.gui.setScreen(new ItemEditScreen(this, node, () -> { }));
						}
					})
					.pos(x, y)
					.size(node.isFolder() ? 150 : 170, 18)
					.build());
			x += node.isFolder() ? 153 : 173;

			if (node.isFolder()) {
				this.addRenderableWidget(Button.builder(Component.literal("Rename"), button ->
								this.minecraft.gui.setScreen(new ItemEditScreen(this, node, () -> { })))
						.pos(x, y)
						.size(40, 18)
						.build());
				x += 43;
			}

			Button up = this.addRenderableWidget(Button.builder(Component.literal("^"), button -> move(index, -1))
					.pos(x, y).size(18, 18).build());
			up.active = index > 0;
			Button down = this.addRenderableWidget(Button.builder(Component.literal("v"), button -> move(index, 1))
					.pos(x + 20, y).size(18, 18).build());
			down.active = index < items.size() - 1;
			this.addRenderableWidget(Button.builder(Component.literal("X"), button -> {
						items.remove(index);
						WheelConfig.get().save();
						this.rebuildWidgets();
					})
					.pos(x + 40, y).size(18, 18).build());

			y += 21;
		}

		int bottom = this.height - 54;
		boolean full = items.size() >= WheelConfig.MAX_ITEMS;

		Button addCommand = this.addRenderableWidget(Button.builder(Component.literal("+ Command"), button -> {
					Node created = Node.command("New command", "");
					items.add(created);
					this.minecraft.gui.setScreen(new ItemEditScreen(this, created, () -> {
						items.remove(created);
					}));
				})
				.pos(this.width / 2 - 135, bottom).size(133, 20).build());
		addCommand.active = !full;

		Button addFolder = this.addRenderableWidget(Button.builder(Component.literal("+ Folder"), button -> {
					Node created = Node.folder("New folder");
					items.add(created);
					this.minecraft.gui.setScreen(new ItemEditScreen(this, created, () -> {
						items.remove(created);
					}));
				})
				.pos(this.width / 2 + 2, bottom).size(133, 20).build());
		addFolder.active = !full;

		this.addRenderableWidget(Button.builder(Component.literal("Open key: " + keyName()), button ->
						this.minecraft.gui.setScreen(new KeyPromptScreen(this)))
				.pos(this.width / 2 - 135, bottom + 24).size(133, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
				.pos(this.width / 2 + 2, bottom + 24).size(133, 20).build());
	}

	private static String keyName() {
		return KeyMappingHelper.getBoundKeyOf(CommandWheelClient.openKey).getDisplayName().getString();
	}

	private void move(int index, int delta) {
		List<Node> items = items();
		int target = index + delta;

		if (target < 0 || target >= items.size()) {
			return;
		}

		Node tmp = items.get(index);
		items.set(index, items.get(target));
		items.set(target, tmp);
		WheelConfig.get().save();
		this.rebuildWidgets();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		String title = this.title.getString();
		graphics.text(this.font, title, this.width / 2 - this.font.width(title) / 2, 14, 0xFFFFFFFF, true);

		if (items().isEmpty()) {
			String hint = "Nothing here yet. Add a command or a folder below.";
			graphics.text(this.font, hint, this.width / 2 - this.font.width(hint) / 2, 50, 0xFF9A9AB0, true);
		}

		String limit = "Max " + WheelConfig.MAX_ITEMS + " slices per level.";
		graphics.text(this.font, limit, this.width / 2 - this.font.width(limit) / 2, this.height - 66, 0xFF888888, true);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		WheelConfig.get().save();
		this.minecraft.gui.setScreen(parent);
	}
}
