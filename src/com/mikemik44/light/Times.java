package com.mikemik44.light;

import org.bukkit.World;

public class Times {

	public static String defaultTimes = "Dawn 23000, Day 0, Dusk 12000, Night 13000, Midnight 17500";

	public static Long convertFromTimeToDay(String timeOfDay) {
		if (timeOfDay.equalsIgnoreCase("Dawn")) {
			return 23000L;
		}
		if (timeOfDay.equalsIgnoreCase("Day")) {
			return 0L;
		}
		if (timeOfDay.equalsIgnoreCase("Dusk")) {
			return 12000L;
		}
		if (timeOfDay.equalsIgnoreCase("Night")) {
			return 13000L;
		}
		if (timeOfDay.equalsIgnoreCase("Midnight")) {
			return 17500L;
		}
		return -1L;
	}

	public static String convertToTimeOfDay(Long time) {
		if (time >= 23000) {
			return "Dawn";
		}
		if (time >= 0 && time <= 12000) {
			return "Day";
		}
		if (time <= 13000) {
			return "Dusk";
		}
		if (time <= 17500) {
			return "Night";
		}
		return "Midnight";
	}

	public static Long getTime(World w) {
		return w.getTime();
	}

}
