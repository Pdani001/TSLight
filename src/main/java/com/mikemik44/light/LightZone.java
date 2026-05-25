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
import org.bukkit.util.Vector;

import com.google.gson.Gson;
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
		String textToSave = on + "=" + timeOn + "=" + timeOff + "=" + new Gson().toJson(defLightBlockLevel) + "="
				+ new Gson().toJson(lightBlockLocations);
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

					if (((sel.getBlockData() instanceof Lightable
									|| sel.getType().isBlock() && sel.getType().equals(Material.LIGHT)))
							&& !sel.getType().name().toLowerCase().contains("candle")) {
						if (!(sel.getType().equals(Material.REDSTONE) || sel.getType().equals(Material.REDSTONE_TORCH)
								|| sel.getType().equals(Material.TORCH))) {
							Location l1 = sel.getLocation();
							Integer[] toint = new Integer[] { l1.getBlockX(), l1.getBlockY(), l1.getBlockZ() };
							lightBlockLocations.add(toint);
							defLightBlockLevel.add(sel.getLightLevel());
						}
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
		
		for (int k=this.lightBlockLocations.size()-1;k>=0;k--) {
			Integer[] v = this.lightBlockLocations.get(k);
			Block sel = w.getBlockAt(v[0], v[1], v[2]);
			if (!((sel.getBlockData() instanceof Lightable
							|| sel.getType().isBlock() && sel.getType().equals(Material.LIGHT))
					&& !sel.getType().name().toLowerCase().contains("candle"))) {
				int ind = this.lightBlockLocations.indexOf(v);
				this.lightBlockLocations.remove(ind);
				this.defLightBlockLevel.remove(ind);
			} else if ((sel.getType().equals(Material.REDSTONE) || sel.getType().equals(Material.REDSTONE_TORCH)
					|| sel.getType().equals(Material.TORCH))) {
				int ind = this.lightBlockLocations.indexOf(v);
				this.lightBlockLocations.remove(ind);
				this.defLightBlockLevel.remove(ind);
			}
		}
		save();
	}

	public Byte getLightLevel(int counter) {
		return defLightBlockLevel.get(counter);
	}

	public void removeLightLevel(int k) {
		defLightBlockLevel.remove(k);
	}

}
