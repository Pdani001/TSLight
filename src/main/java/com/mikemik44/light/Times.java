package com.mikemik44.light;

import org.bukkit.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum Times {

	DAWN(23_000L),
	DAY(0L),
	DUSK(12_000L),
	NIGHT(13_000L),
	MIDNIGHT(17_500L),
	;
	private final long time;
	Times(long time){
		this.time = time;
	}
	public static final Map<String, Times> timesNamesMap = new HashMap<>(values().length);
	public static final Map<Times, String> niceName = new HashMap<>(values().length);
	static {
		Arrays.stream(values()).forEach(times -> timesNamesMap.put(times.name(),times));
		Arrays.stream(values()).forEach(times -> niceName.put(times, times.name().charAt(0) + times.name().substring(1).toLowerCase()));
	}

	public long getTime() {
		return time;
	}

	public static long convertFromTimeToDay(String timeOfDay) {
		Times times = timesNamesMap.get(timeOfDay.toUpperCase());
		if(times != null)
			return times.getTime();
		return -1L;
	}

	public static long getWorldTime(World w) {
		return w.getTime();
	}

	public static long normalize(long time){
		return (time % 24000 + 24000) % 24000;
	}

	@Override
	public String toString() {
		return niceName.get(this);
	}
}
