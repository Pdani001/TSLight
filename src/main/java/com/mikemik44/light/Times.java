package com.mikemik44.light;

import org.bukkit.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum Times {

	DAY(0L),
	DUSK(12_000L),
	NIGHT(13_000L),
	MIDNIGHT(17_500L),
	DAWN(23_000L),
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

	public static Long convertFromTimeToDay(String timeOfDay) {
		Times times = timesNamesMap.get(timeOfDay.toUpperCase());
		if(times != null)
			return times.getTime();
		return -1L;
	}

	public static String convertToTimeOfDay(Long time) {
		Times current = DAY;
		for(Times times : values())
			if(time >= times.getTime()) current = times;
			else break;
		return current.toString();
	}

	public static Long getWorldTime(World w) {
		return w.getTime();
	}

	@Override
	public String toString() {
		return niceName.get(this);
	}
}
