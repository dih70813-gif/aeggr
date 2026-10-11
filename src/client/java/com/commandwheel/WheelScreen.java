package com.commandwheel;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import com.commandwheel.WheelConfig.Node;

/** The radial menu. Hold the key and let go on a slice, or click a slice. */
public class WheelScreen extends Screen {
	private static final int OUTER = 125;
	private static final int INNER = 56;
	private static final int SLICE_A = 0xC0222236;
	private static final int SLICE_B = 0xC02C2C44;
	private static final int SLICE_HOVER = 0xE05A5AA8;

	private final List<Node> trail = new ArrayList<>();
	private final long openedAt = System.currentTimeMillis();
	private int hovered = -1;
	private Button editButton;
	private Button backButton;

	public WheelScreen() {
		super(Component.literal("Command Wheel"));
	}

	private List<Node> items() {
		return trail.isEmpty() ? WheelConfig.get().root : trail.get(trail.size() - 1).children;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int cy = this.height / 2;

		editButton = this.addRenderableWidget(Button.builder(Component.literal("Edit wheel"), button ->
						this.minecraft.gui.setScreen(new EditScreen(this, null)))
				.pos(cx - 34, cy + 2)
				.size(68, 16)
				.build());
		backButton = this.addRenderableWidget(Button.builder(Component.literal("< Back"), button -> {
					trail.remove(trail.size() - 1);
					updateBack();
				})
				.pos(cx - 34, cy + 20)
				.size(68, 16)
				.build());
		updateBack();
	}

	private void updateBack() {
		backButton.visible = !trail.isEmpty();
		backButton.active = !trail.isEmpty();
	}

	// ---- geometry ----

	private static int sectorOf(double dx, double dy, int n) {
		double angle = Math.atan2(dx, -dy); // 0 = straight up, clockwise
		if (angle < 0) {
			angle += Math.PI * 2;
		}

		return Math.min(n - 1, (int) (angle / (Math.PI * 2 / n)));
	}

	private int sliceColor(int index, int n) {
		if (index == hovered) {
			return SLICE_HOVER;
		}

		return index % 2 == 0 ? SLICE_A : SLICE_B;
	}

	// ---- rendering ----

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// No blur: we dim the world ourselves in extractRenderState.
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		List<Node> items = items();
		int n = items.size();
		int cx = this.width / 2;
		int cy = this.height / 2;

		double dx = mouseX - cx;
		double dy = mouseY - cy;
		double dist = Math.sqrt(dx * dx + dy * dy);
		hovered = (n > 0 && dist >= INNER && dist <= OUTER + 30) ? sectorOf(dx, dy, n) : -1;

		graphics.fill(0, 0, this.width, this.height, 0x60000000);

		// Slices, drawn row by row in runs of the same colour.
		for (int y = -OUTER; y <= OUTER; y++) {
			int xo = (int) Math.sqrt((double) OUTER * OUTER - (double) y * y);

			if (Math.abs(y) < INNER) {
				int xi = (int) Math.sqrt((double) INNER * INNER - (double) y * y);
				drawRow(graphics, cx, cy, y, -xo, -xi, n);
				drawRow(graphics, cx, cy, y, xi, xo, n);
			} else {
				drawRow(graphics, cx, cy, y, -xo, xo, n);
			}
		}

		// Dividers between slices.
		if (n > 1) {
			for (int i = 0; i < n; i++) {
				double a = i * Math.PI * 2 / n;

				for (int t = INNER; t <= OUTER; t++) {
					int px = cx + (int) Math.round(Math.sin(a) * t);
					int py = cy - (int) Math.round(Math.cos(a) * t);
					graphics.fill(px, py, px + 1, py + 1, 0xFF0E0E18);
				}
			}
		}

		// Labels.
		double labelRadius = (INNER + OUTER) / 2.0;

		for (int i = 0; i < n; i++) {
			Node node = items.get(i);
			double a = (i + 0.5) * Math.PI * 2 / n;
			int lx = cx + (int) Math.round(Math.sin(a) * labelRadius);
			int ly = cy - (int) Math.round(Math.cos(a) * labelRadius);

			label(graphics, node.name, lx, ly - 8, i == hovered ? 0xFFFFFFFF : 0xFFE0E0E0);
			label(graphics, node.isFolder() ? "Open >" : "/" + shorten(node.command, 14), lx, ly + 4, 0xFF9A9AB0);
		}

		// Centre text.
		String title;
		String sub;

		if (n == 0) {
			title = "Empty";
			sub = "Use Edit wheel";
		} else if (hovered >= 0) {
			Node node = items.get(hovered);
			title = node.name;
			sub = node.isFolder() ? "Click to open" : "Click to run";
		} else {
			title = trail.isEmpty() ? "Command Wheel" : trail.get(trail.size() - 1).name;
			sub = "Hover an action";
		}

		label(graphics, title, cx, cy - 30, 0xFFFFFFFF);
		label(graphics, sub, cx, cy - 18, 0xFF9A9AB0);

		String key = KeyMappingHelper.getBoundKeyOf(CommandWheelClient.openKey).getDisplayName().getString();
		label(graphics, "Hover to use  |  Release " + key + " or ESC to close  |  Click to select", cx, this.height - 16, 0xFF8A8A9A);

		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	private void drawRow(GuiGraphicsExtractor graphics, int cx, int cy, int y, int fromX, int toX, int n) {
		if (toX <= fromX) {
			return;
		}

		if (n == 0) {
			graphics.fill(cx + fromX, cy + y, cx + toX, cy + y + 1, SLICE_A);
			return;
		}

		int runStart = fromX;
		int runSector = sectorOf(fromX, y, n);

		for (int x = fromX + 1; x <= toX; x++) {
			int s = x < toX ? sectorOf(x, y, n) : -2;

			if (s != runSector) {
				graphics.fill(cx + runStart, cy + y, cx + x, cy + y + 1, sliceColor(runSector, n));
				runStart = x;
				runSector = s;
			}
		}
	}

	private void label(GuiGraphicsExtractor graphics, String text, int centerX, int y, int color) {
		graphics.text(this.font, text, centerX - this.font.width(text) / 2, y, color, true);
	}

	private static String shorten(String s, int max) {
		if (s == null) {
			return "";
		}

		return s.length() <= max ? s : s.substring(0, max - 1) + "...";
	}

	// ---- input ----

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}

		if (hovered >= 0) {
			activate(hovered);
			return true;
		}

		return false;
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		int bound = KeyMappingHelper.getBoundKeyOf(CommandWheelClient.openKey).getValue();

		if (event.key() == bound) {
			// A quick tap keeps the wheel open so you can click; a real hold selects on release.
			if (System.currentTimeMillis() - openedAt > 300) {
				if (hovered >= 0) {
					activate(hovered);
				} else {
					this.onClose();
				}
			}

			return true;
		}

		return super.keyReleased(event);
	}

	private void activate(int index) {
		List<Node> items = items();

		if (index < 0 || index >= items.size()) {
			return;
		}

		Node node = items.get(index);

		if (node.isFolder()) {
			trail.add(node);
			updateBack();
			return;
		}

		String cmd = node.command == null ? "" : node.command.trim();

		if (cmd.startsWith("/")) {
			cmd = cmd.substring(1);
		}

		this.onClose();

		if (!cmd.isEmpty() && this.minecraft.player != null) {
			this.minecraft.player.connection.sendCommand(cmd);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(null);
	}
}
