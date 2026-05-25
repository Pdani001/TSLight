package com.mikemik44.light;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
import org.bukkit.block.data.Lightable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikemik44.light.filemanager.FileHandler;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;

public class TsLight extends JavaPlugin implements org.bukkit.event.Listener {

	private static String pluginDir = "plugins/TSLight";
	private int usedLightZoneIDS = 0;
	private final ArrayList<LightZone> allLightZones = new ArrayList<>();
	public static HashMap<UUID, ArrayList<String>> inputData = new HashMap<>();
	private static Gson gson;

	public static String getPluginDir() {
		return pluginDir;
	}

	public static Gson getGson() {
		return gson;
	}

	@Override
	public void onEnable() {
		GsonBuilder builder = new GsonBuilder();
		gson = builder.create();
		pluginDir = getDataFolder().getPath();
		File checkDir = new File(pluginDir);
		if (!checkDir.isDirectory()) {
			checkDir.mkdirs();
		}
		loadLightZones();
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("tslight").setExecutor(new TSLightCommand(this));
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
			if (allLightZones.isEmpty()) {
				return;
			}
			for (int ij = allLightZones.size() - 1; ij >= 0; ij--) {
				LightZone lz = allLightZones.get(ij);
				if (lz.getLightBlocks().isEmpty()) {
					allLightZones.remove(lz);
					File f1 = new File(lz.fileName);
					try {
						if (f1.exists()) {
							f1.delete();
						}
					} catch (Exception ignored) {

					}
				}
				World w = getServer().getWorld(lz.getWorldName());
				if (!w.getPlayers().isEmpty()) {
					int timeOn = lz.timeOn;
					int timeOff = lz.timeOff;
					long time = Times.getWorldTime(w);
					if (timeOn > timeOff) {
						if (time >= timeOff && time <= timeOn) {
							for (int k = lz.getLightBlocks().size() - 1; k >= 0; k--) {
								Integer[] v = lz.getLightBlocks().get(k);
								Block b = w.getBlockAt(v[0], v[1], v[2]);
								if (validType(b)) {
									byte dp = 0;
									if (lz.on == 1) { // Is it on mode
										setLightLevel(b, lz.getLightLevel(k));
									} else if (lz.on == 2) { // is it off mode
										setLightLevel(b, dp);
									} else if (lz.on == 3) {
										setLightLevel(b, dp);
									}
								} else {
									lz.getLightBlocks().remove(k);
									lz.removeLightLevel(k);
									lz.save();
								}
							}
						} else {
							for (int k = lz.getLightBlocks().size() - 1; k >= 0; k--) {
								Integer[] v = lz.getLightBlocks().get(k);
								Block b = w.getBlockAt(v[0], v[1], v[2]);
								if (validType(b)) {
									byte dp = 15;
									if (lz.on == 1) { // Is it on mode
										setLightLevel(b, dp);
									} else if (lz.on == 2) { // is it off mode
										setLightLevel(b, lz.getLightLevel(k));
									} else if (lz.on == 3) {
										setLightLevel(b, dp);
									}
								} else {
									lz.getLightBlocks().remove(k);
									lz.removeLightLevel(k);
									lz.save();
								}
							}
						}
					} else {
						if (time >= timeOff && time <= timeOn) {
							for (int k = lz.getLightBlocks().size() - 1; k >= 0; k--) {
								Integer[] v = lz.getLightBlocks().get(k);
								Block b = w.getBlockAt(v[0], v[1], v[2]);
								if (validType(b)) {
									byte dp = 0;
									if (lz.on == 1) { // Is it on mode
										setLightLevel(b, lz.getLightLevel(k));
									} else if (lz.on == 2) { // is it off mode
										setLightLevel(b, dp);
									} else if (lz.on == 3) {
										setLightLevel(b, dp);
									}
								} else {
									lz.getLightBlocks().remove(k);
									lz.removeLightLevel(k);
									lz.save();
								}
							}
						} else {
							for (int k = lz.getLightBlocks().size() - 1; k >= 0; k--) {
								Integer[] v = lz.getLightBlocks().get(k);
								Block b = w.getBlockAt(v[0], v[1], v[2]);
								if (validType(b)) {
									byte dp = 15;
									if (lz.on == 1) { // Is it on mode
										setLightLevel(b, dp);
									} else if (lz.on == 2) { // is it off mode
										setLightLevel(b, lz.getLightLevel(k));
									} else if (lz.on == 3) {
										setLightLevel(b, dp);
									}
								} else {
									lz.getLightBlocks().remove(k);
									lz.removeLightLevel(k);
									lz.save();
								}
							}
						}
					}

				}

			}

		}, 1, 1);
	}

	public boolean validType(Block b) {
		if (b.getBlockData() instanceof Lightable || b.getBlockData() instanceof Light) {
			if (!(b.getType().equals(Material.REDSTONE) || b.getType().equals(Material.REDSTONE_TORCH)
					|| b.getType().equals(Material.TORCH) || b.getType().name().toLowerCase().contains("candle"))) {
				return true;
			}
		}
		return false;
	}

	public void setLightLevel(Block block, byte level) {
		if (block.getLightLevel() == level) {
			return;
		}
		Bukkit.getScheduler().runTask(this, () -> {
			if (block.getBlockData() instanceof Light light) {
				light.setLevel(level);
				block.setBlockData(light, false);
			} else if (block.getBlockData() instanceof Lightable lightable) {
				lightable.setLit(level != 0);
				block.setBlockData(lightable, false);
			}
		});
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		super.onDisable();
	}

	public void loadLightZones() {
		String dirPath = TsLight.pluginDir + "/lightZone/";
		File f = new File(dirPath);
		usedLightZoneIDS = 0;
		if (f.isDirectory()) {
			for (File file : Objects.requireNonNull(f.listFiles())) {
				String name = file.getName();
				String fileName = name.replace(".txt", "");
				String[] fileData = fileName.split("=");
				String fileInformation = FileHandler.readFromFile(dirPath + "/" + name);
				String[] fileSep = fileInformation.split("=");
				usedLightZoneIDS = Math.max(usedLightZoneIDS - 1, Integer.parseInt(fileData[1].trim())) + 1;
				allLightZones.add(new LightZone(fileData[1].trim(), fileData[0].trim(),
						Integer.parseInt(fileSep[0].trim()), Integer.parseInt(fileSep[1].trim()),
						Integer.parseInt(fileSep[2].trim()),
                        new Gson().fromJson(fileSep[3].trim(), new TypeToken<ArrayList<Byte>>() {
                        }.getType()), new Gson().fromJson(fileSep[4].trim(),
								new TypeToken<ArrayList<Integer[]>>() {
								}.getType())));
			}
		}
	}

	// TODO: move plugin functionality from the chat to the dedicated command
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerChat(AsyncPlayerChatEvent event) {

		if (inputData.containsKey(event.getPlayer().getUniqueId())) {
			event.setCancelled(true); // Prevents the message from being seen
			String msg = event.getMessage();
			for (int i = 0; i < 9; i++) {
				msg = msg.replace("§" + i, "");
			}
			msg = msg.replace("§a", "");
			msg = msg.replace("§b", "");
			msg = msg.replace("§c", "");
			msg = msg.replace("§d", "");
			msg = msg.replace("§e", "");
			msg = msg.replace("§f", "");
			msg = msg.replace("§r", "");
			getLogger().info(msg);

			String[] sp1 = msg.split(": ");
			if (sp1.length == 1) {
				msg = sp1[0];
			} else {
				msg = "";
				for (int i = 1; i < sp1.length; i++) {
					if (i != 1) {
						msg += " ";
					}
					msg = msg + sp1[i].trim();
				}
				msg = msg.trim();
			}
			org.bukkit.entity.Player p = event.getPlayer();
			ArrayList<String> answers = inputData.get(p.getUniqueId());
			int step = answers.size();

			if (step == 0) {
				if (msg.equalsIgnoreCase("on")) {
					answers.add(msg);
					p.sendMessage("Ok so what time do you want it to turn on at!");
				} else if (msg.equalsIgnoreCase("off")) {
					answers.add(msg);
					p.sendMessage("Ok so what time do you want it to turn off at!");
				} else if (msg.equalsIgnoreCase("both")) {
					answers.add(msg);
					p.sendMessage("Ok so what time do you want it to turn on at!");
				} else if (msg.equalsIgnoreCase("exit")) {
					inputData.remove(p.getUniqueId());
				} else {
					p.sendMessage("Invalid option valid options are exit, on, off, or both!");
				}
			} else if (step == 1) {
				if (msg.matches("[0-9]+")) {
					answers.add(msg);
					if (answers.get(0).equalsIgnoreCase("on") || answers.get(0).equalsIgnoreCase("off")) {
						p.sendMessage("Ok when do you want to have it return to default light value!");
					} else {
						p.sendMessage("Ok now when do you want the light to turn off at!");
					}
				} else if (msg.equalsIgnoreCase("exit")) {
					inputData.remove(p.getUniqueId());
				} else {
					p.sendMessage("Invalid time " + msg + "! Either enter Valid time or write exit!");
				}
			} else if (step == 2) {
				if (msg.matches("[0-9]+")) {
					answers.add(msg);
					p.sendMessage(
							"Now Please Either Enter Coords of min and max or use world edit to make a selection! When you are done write finished, or write the coords ex. -1 -1 -1 1 1 1!");
				} else if (msg.equalsIgnoreCase("exit")) {
					inputData.remove(p.getUniqueId());
				} else {
					p.sendMessage("Invalid time " + msg + "! Either enter Valid time or write exit!");
				}
			} else if (step == 3) {
				if (msg.equalsIgnoreCase("finished")) {
					Player wep = BukkitAdapter.adapt(p);
					LocalSession w = WorldEdit.getInstance().getSessionManager().get(wep);
					if (w != null) {
						try {
							Region r = w.getSelection(w.getSelectionWorld());
							int timeOn = -1, timeOff = -1;
							if (answers.get(0).equalsIgnoreCase("on") || answers.get(0).equalsIgnoreCase("both")) {
								timeOn = Integer.parseInt(answers.get(1));
								timeOff = Integer.parseInt(answers.get(2));
							} else {
								timeOff = Integer.parseInt(answers.get(1));
								timeOn = Integer.parseInt(answers.get(2));
							}
							LightZone lz = new LightZone(getServer().getWorld(w.getSelectionWorld().getName()),
									usedLightZoneIDS++ + "", findAnswer(answers.get(0)), timeOn, timeOff,
									toVector(r.getMinimumPoint().toVector3()),
									toVector(r.getMaximumPoint().toVector3()));
							allLightZones.add(lz);
							p.sendMessage("added " + lz.getLightBlocks().size() + " amount of lights!");
							inputData.remove(p.getUniqueId());
						} catch (IncompleteRegionException e) {
							p.sendMessage(
									"You must make a world edit selection! To use the finish, or you can use exit to exit, or provide the coords itself such as -1 -1 -1 1 1 1!");
						}
					}
				} else if (msg.equals("exit")) {
					inputData.remove(p.getUniqueId());
				} else {
					String[] dp = msg.split(" ");
					if (dp.length == 6) {
						int num1x = toNumber(dp[0]);
						int num1y = toNumber(dp[1]);
						int num1z = toNumber(dp[2]);
						int num2x = toNumber(dp[3]);
						int num2y = toNumber(dp[4]);
						int num2z = toNumber(dp[5]);
						if (num1x == -Integer.MAX_VALUE) {
							p.sendMessage("Invalid number coord " + dp[0]
									+ "! Expected Either finished, exit, or coord like -1 -1 -1 1 1 1!");
						}
						if (num1y == -Integer.MAX_VALUE) {
							p.sendMessage("Invalid number coord " + dp[1]
									+ "! Expected Either finished, exit, or coord like -1 -1 -1 1 1 1!");
						}
						if (num1z == -Integer.MAX_VALUE) {
							p.sendMessage("Invalid number coord " + dp[2]
									+ "! Expected Either finished, exit, or coord like -1 -1 -1 1 1 1!");
						}
						if (num2x == -Integer.MAX_VALUE) {
							p.sendMessage("Invalid number coord " + dp[3]
									+ "! Expected Either finished, exit, or coord like -1 -1 -1 1 1 1!");
						}
						if (num2y == -Integer.MAX_VALUE) {
							p.sendMessage("Invalid number coord " + dp[4]
									+ "! Expected Either finished, exit, or coord like -1 -1 -1 1 1 1!");
						}
						if (num2z == -Integer.MAX_VALUE) {
							p.sendMessage("Invalid number coord " + dp[5]
									+ "! Expected Either finished, exit, or coord like -1 -1 -1 1 1 1!");
						}
						int timeOn = -1, timeOff = -1;
						if (answers.get(0).equalsIgnoreCase("on") || answers.get(0).equalsIgnoreCase("both")) {
							timeOn = Integer.parseInt(answers.get(1));
							timeOff = Integer.parseInt(answers.get(2));
						} else {
							timeOff = Integer.parseInt(answers.get(1));
							timeOn = Integer.parseInt(answers.get(2));
						}
						LightZone lz = new LightZone(p.getWorld(), usedLightZoneIDS++ + "", findAnswer(answers.get(0)),
								timeOn, timeOff, new Vector(num1x, num1y, num1z), new Vector(num2x, num2y, num2z));
						allLightZones.add(lz);
						p.sendMessage("added " + lz.getLightBlocks().size() + " amount of lights!");
						inputData.remove(p.getUniqueId());
					} else {
						p.sendMessage("Only valid inputs are finished, exit, or the coords such as -1 -1 -1 1 1 1!");
					}
				}
			}
		}
	}

	public int toNumber(String num) {
		if (num.matches("-?[0-9]+")) {
			return Integer.parseInt(num);
		} else {
			return -Integer.MAX_VALUE;
		}
	}

	private Vector toVector(Vector3 loc) {
		return new Vector(loc.x(), loc.y(), loc.z());
	}

	private int findAnswer(String string) {
		if (string.equalsIgnoreCase("on")) {
			return 1;
		} else if (string.equalsIgnoreCase("off")) {
			return 2;
		} else if (string.equalsIgnoreCase("both")) {
			return 3;
		}
		return -1;
	}

}
