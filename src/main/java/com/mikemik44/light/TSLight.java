package com.mikemik44.light;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
import org.bukkit.block.data.Lightable;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikemik44.light.filemanager.FileHandler;

public class TSLight extends JavaPlugin implements Listener {

	private static String pluginDir = "plugins/TSLight";
	private int usedLightZoneIDS = 0;
	private final ArrayList<LightZone> allLightZones = new ArrayList<>();
	private static Gson gson;

	public static String getPluginDir() {
		return pluginDir;
	}

	public static Gson getGson() {
		return gson;
	}

	private final Map<String, Long> missingWorldWarning = new HashMap<>();

	@SuppressWarnings({"ResultOfMethodCallIgnored", "deprecation", "DataFlowIssue"})
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
				if(w == null) {
					long time = System.currentTimeMillis();
					long last = missingWorldWarning.getOrDefault(lz.getWorldName(), 0L);
					if(last - time < 1_800_000L)
						continue;
					missingWorldWarning.put(lz.getWorldName(), time);
					getLogger().warning("World '%s' not found! (LZ#%s)".formatted(lz.getWorldName(), lz.getId()));
					continue;
				}
                missingWorldWarning.remove(lz.getWorldName());
				if (!w.getPlayers().isEmpty()) {
					int timeOn = lz.timeOn;
					int timeOff = lz.timeOff;
					long time = Times.getWorldTime(w);
					runLightUpdate(lz, w, (time >= timeOff && time <= timeOn));
				}

			}

		}, 1, 1);
	}

	private void runLightUpdate(final LightZone lz, final World w, final boolean isOnMode) {
		for (int k = lz.getLightBlocks().size() - 1; k >= 0; k--) {
			LightBlock v = lz.getLightBlocks().get(k);
			Block b = w.getBlockAt(v.x, v.y, v.z);
			if (validType(b)) {
				byte dp = (byte) (isOnMode ? 0 : 15);
				setLightLevel(b, switch (lz.mode) {
                    case ON -> isOnMode ? lz.getLightLevel(k) : dp;
                    case OFF -> isOnMode ? dp : lz.getLightLevel(k);
                    case BOTH -> dp;
                });
			} else {
				lz.removeLight(k);
			}
		}
	}

	private boolean validType(final Block b) {
        return (b.getBlockData() instanceof Lightable || b.getBlockData() instanceof Light)
                && !(b.getType().equals(Material.REDSTONE) || b.getType().equals(Material.REDSTONE_TORCH)
                || b.getType().equals(Material.TORCH) || b.getType().name().toLowerCase().contains("candle"));
    }

	private void setLightLevel(final Block block, final byte level) {
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

	@SuppressWarnings("DataFlowIssue")
    private void loadLightZones() {
		String dirPath = TSLight.pluginDir + "/lightZone/";
		File f = new File(dirPath);
		usedLightZoneIDS = 0;
		if (f.isDirectory()) {
			for (File file : Objects.requireNonNull(f.listFiles())) {
				String name = file.getName();
				String fileName = name.replace(".txt", "");
				String[] fileData = fileName.split("=");
				String fileInformation = FileHandler.readFromFile(dirPath + "/" + name);
				String[] fileContent = fileInformation.split("=");
				usedLightZoneIDS = Math.max(usedLightZoneIDS - 1, Integer.parseInt(fileData[1].trim())) + 1;
				if(fileContent.length == 5)
					allLightZones.add(new LightZone(fileData[1].trim(), fileData[0].trim(),
							Integer.parseInt(fileContent[0].trim()), Integer.parseInt(fileContent[1].trim()),
							Integer.parseInt(fileContent[2].trim()),
							gson.fromJson(fileContent[3].trim(), new TypeToken<ArrayList<Byte>>() {
							}.getType()), gson.fromJson(fileContent[4].trim(),
									new TypeToken<ArrayList<Integer[]>>() {
									}.getType())));
				else
					allLightZones.add(new LightZone(fileData[1].trim(), fileData[0].trim(),
							Integer.parseInt(fileContent[0].trim()), Integer.parseInt(fileContent[1].trim()),
							Integer.parseInt(fileContent[2].trim()),
							gson.fromJson(fileContent[3].trim(), new TypeToken<ArrayList<LightBlock>>() {
							}.getType())));
			}
		}
	}

	public int getNextID() {
		return usedLightZoneIDS++;
	}

	public void addLightZone(LightZone lz) {
		allLightZones.add(lz);
	}

}
