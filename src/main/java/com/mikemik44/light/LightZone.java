package com.mikemik44.light;

import java.io.File;
import java.util.ArrayList;
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
	private final String world;
	private final ArrayList<Integer[]> lightBlockLocations;
	private ArrayList<Byte> defLightBlockLevel = new ArrayList<>();
	public final String fileName;
	public final Integer on, timeOn, timeOff;

	public ArrayList<Integer[]> getLightBlocks() {
		return lightBlockLocations;
	}

	public String getWorldName() {
		return world;
	}

	public void save() {
		String textToSave = on + "=" + timeOn + "=" + timeOff + "=" + TsLight.getGson().toJson(defLightBlockLevel) + "="
				+ TsLight.getGson().toJson(lightBlockLocations);
		FileHandler.writeToFile(fileName, textToSave);
	}
	
	public LightZone(World w, String id, int on, int onSwitch, int offSwitch, Vector min, Vector max) {
		File check = new File(TsLight.getPluginDir() + "/lightZone");
		if (!check.isDirectory()) {
			check.mkdirs();
		}
		this.fileName = TsLight.getPluginDir() + "/lightZone/" + w.getName() + "=" + id + ".txt";
		this.on = on;
		this.timeOn = onSwitch;
		this.timeOff = offSwitch;
		lightBlockLocations = new ArrayList<>();
		world = w.getName();
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
						Integer[] blockPosition = new Integer[] { location.getBlockX(), location.getBlockY(), location.getBlockZ() };
						lightBlockLocations.add(blockPosition);
						defLightBlockLevel.add(sel.getLightLevel());
					}
				}
			}
		}
		save();
	}

	public LightZone(String id, String world, int on, int timeOn, int timeOff, ArrayList<Byte> lightLevels,
			ArrayList<Integer[]> lightBlockLocations) {
		this.fileName = TsLight.getPluginDir() + "/lightZone/" + world + "=" + id + ".txt";
		this.lightBlockLocations = lightBlockLocations;
		this.defLightBlockLevel = lightLevels;
		this.timeOn = timeOn;
		this.timeOff = timeOff;
		this.world = world;
		this.on = on;
		World w = Objects.requireNonNull(Bukkit.getServer().getWorld(world), "world not found");
		
		for (int i=this.lightBlockLocations.size()-1;i>=0;i--) {
			Integer[] pos = this.lightBlockLocations.get(i);
			Block sel = w.getBlockAt(pos[0], pos[1], pos[2]);
			if ((!(sel.getBlockData() instanceof Lightable
							|| sel.getBlockData() instanceof Light))
			|| (sel.getType().equals(Material.REDSTONE) || sel.getType().equals(Material.REDSTONE_TORCH)
					|| sel.getType().equals(Material.TORCH) || sel.getType().name().toLowerCase().contains("candle"))) {
				int lightIndex = this.lightBlockLocations.indexOf(pos);
				this.lightBlockLocations.remove(lightIndex);
				this.defLightBlockLevel.remove(lightIndex);
			}
		}
		save();
	}

	public Byte getLightLevel(int counter) {
		return defLightBlockLevel.get(counter);
	}

	public void removeLightLevel(int index) {
		defLightBlockLevel.remove(index);
	}

}
