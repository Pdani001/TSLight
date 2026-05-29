package com.mikemik44.light;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Light;
import org.bukkit.util.Vector;

import com.mikemik44.light.filemanager.FileHandler;

public class LightZone {
	private final String id;
	private final String world;
	private final List<LightBlock> lightBlocks = new ArrayList<>();
	private final ArrayList<Integer[]> lightBlockLocations;
	private final ArrayList<Byte> defLightBlockLevel;
	public final String fileName;
	public final Integer timeOn, timeOff;
	public final LightMode mode;

	public List<LightBlock> getLightBlocks() {
		return lightBlocks;
	}

	public String getWorldName() {
		return world;
	}

	private void save() {
		String textToSave = mode.toInt() + "=" + timeOn + "=" + timeOff + "=" + TSLight.getGson().toJson(lightBlocks);
		FileHandler.writeToFile(fileName, textToSave);
	}
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
    public LightZone(World w, String id, int mode, int onSwitch, int offSwitch, Vector min, Vector max) {
		File check = new File(TSLight.getPluginDir() + "/lightZone");
		if (!check.isDirectory()) {
			check.mkdirs();
		}
		this.id = id;
		this.fileName = TSLight.getPluginDir() + "/lightZone/" + w.getName() + "=" + id + ".txt";
		this.mode = LightMode.fromInt(mode);
		this.timeOn = onSwitch;
		this.timeOff = offSwitch;
		this.lightBlockLocations = null;
		this.defLightBlockLevel = null;
		this.world = w.getName();
		Vector m = Vector.getMinimum(min, max);
		max = Vector.getMaximum(min, max);
		min = m;
		for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
			for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
				for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
					Block sel = w.getBlockAt(x, y, z);

					if ((sel.getBlockData() instanceof Lightable
									|| sel.getBlockData() instanceof Light)
					&& !(sel.getType().equals(Material.REDSTONE) || sel.getType().equals(Material.REDSTONE_TORCH)
							|| sel.getType().equals(Material.TORCH) || sel.getType().name().toLowerCase().contains("candle"))) {
						Location location = sel.getLocation();
						lightBlocks.add(new LightBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ(), sel.getLightLevel()));
					}
				}
			}
		}
		save();
	}

	public LightZone(String id, String world, int mode, int timeOn, int timeOff, ArrayList<Byte> lightLevels,
			ArrayList<Integer[]> lightBlockLocations) {
		this.id = id;
		this.fileName = TSLight.getPluginDir() + "/lightZone/" + world + "=" + id + ".txt";
		this.lightBlockLocations = lightBlockLocations;
		this.defLightBlockLevel = lightLevels;
		this.timeOn = timeOn;
		this.timeOff = timeOff;
		this.world = world;
		this.mode = LightMode.fromInt(mode);
		World w = Objects.requireNonNull(Bukkit.getServer().getWorld(world), "world not found");
		
		for (int i=this.lightBlockLocations.size()-1;i>=0;i--) {
			Integer[] pos = this.lightBlockLocations.get(i);
			Block sel = w.getBlockAt(pos[0], pos[1], pos[2]);
			if ((!(sel.getBlockData() instanceof Lightable
							|| sel.getBlockData() instanceof Light))
			|| (sel.getType().equals(Material.REDSTONE) || sel.getType().equals(Material.REDSTONE_TORCH)
					|| sel.getType().equals(Material.TORCH) || sel.getType().name().toLowerCase().contains("candle"))) {
				this.lightBlockLocations.remove(i);
				this.defLightBlockLevel.remove(i);
			} else {
				this.lightBlocks.addFirst(new LightBlock(pos[0], pos[1], pos[2], defLightBlockLevel.get(i)));
			}
		}
		save();
	}

	public LightZone(String id, String world, int mode, int timeOn, int timeOff, ArrayList<LightBlock> lightBlocks) {
		this.id = id;
		this.fileName = TSLight.getPluginDir() + "/lightZone/" + world + "=" + id + ".txt";
		this.lightBlocks.addAll(lightBlocks);
		this.lightBlockLocations = null;
		this.defLightBlockLevel = null;
		this.timeOn = timeOn;
		this.timeOff = timeOff;
		this.world = world;
		this.mode = LightMode.fromInt(mode);
		World w = Objects.requireNonNull(Bukkit.getServer().getWorld(world), "world not found");

		for (int i=this.lightBlocks.size()-1;i>=0;i--) {
			LightBlock pos = lightBlocks.get(i);
			Block sel = w.getBlockAt(pos.x, pos.y, pos.z);
			if ((!(sel.getBlockData() instanceof Lightable
					|| sel.getBlockData() instanceof Light))
					|| (sel.getType().equals(Material.REDSTONE) || sel.getType().equals(Material.REDSTONE_TORCH)
					|| sel.getType().equals(Material.TORCH) || sel.getType().name().toLowerCase().contains("candle"))) {
				this.lightBlocks.remove(i);
			}
		}
		save();
	}

	public Byte getLightLevel(int counter) {
		return this.lightBlocks.get(counter).level;
	}

	public void removeLight(int index) {
		if(this.lightBlockLocations != null && this.defLightBlockLevel != null) {
			this.defLightBlockLevel.remove(index);
			this.lightBlockLocations.remove(index);
		}
		this.lightBlocks.remove(index);
		save();
	}

    public String getId() {
        return id;
    }
}
