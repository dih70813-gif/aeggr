package com.commandwheel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/** The wheel layout and settings, stored as JSON in config/commandwheel.json. */
public final class WheelConfig {
	public static final int MAX_ITEMS = 8;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static WheelConfig instance;

	/** One slice of the wheel: either a command, or a folder with children. */
	public static final class Node {
		public String name = "New";
		/** Command without the leading slash. Null for folders. */
		public String command;
		/** Non-null means this node is a folder. */
		public List<Node> children;

		public boolean isFolder() {
			return children != null;
		}

		public static Node command(String name, String command) {
			Node n = new Node();
			n.name = name;
			n.command = command;
			return n;
		}

		public static Node folder(String name) {
			Node n = new Node();
			n.name = name;
			n.children = new ArrayList<>();
			return n;
		}
	}

	/** Set to true once the "which key?" box has been shown. */
	public boolean keyPromptDone = false;
	/** If true the "which key?" box is shown every time you join a world or server. */
	public boolean showKeyPromptOnEveryJoin = false;
	public List<Node> root = new ArrayList<>();

	public static WheelConfig get() {
		if (instance == null) {
			load();
		}

		return instance;
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("commandwheel.json");
	}

	public static void load() {
		Path file = file();

		if (Files.exists(file)) {
			try {
				WheelConfig loaded = GSON.fromJson(Files.readString(file), WheelConfig.class);

				if (loaded != null && loaded.root != null) {
					instance = loaded;
					return;
				}
			} catch (Exception e) {
				System.err.println("[Command Wheel] Could not read config, using defaults: " + e);
			}
		}

		instance = createDefault();
		instance.save();
	}

	public void save() {
		try {
			Files.createDirectories(file().getParent());
			Files.writeString(file(), GSON.toJson(this));
		} catch (IOException e) {
			System.err.println("[Command Wheel] Could not save config: " + e);
		}
	}

	private static WheelConfig createDefault() {
		WheelConfig cfg = new WheelConfig();

		Node homes = Node.folder("Homes");
		homes.children.add(Node.command("Home 1", "home home1"));
		homes.children.add(Node.command("Home 2", "home home2"));
		homes.children.add(Node.command("Home 3", "home home3"));

		cfg.root.add(homes);
		cfg.root.add(Node.command("Spawn", "spawn"));
		cfg.root.add(Node.command("RTP", "rtp"));
		return cfg;
	}
}
