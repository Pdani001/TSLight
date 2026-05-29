package com.mikemik44.light;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum LightMode {
    ON,
    OFF,
    BOTH;

    private final static Map<String, LightMode> modeMap = new HashMap<>(values().length);
    private final static Map<Integer, LightMode> intMap = new HashMap<>(values().length);
    static {
        for(LightMode mode : values()){
            modeMap.put(mode.name(), mode);
            intMap.put(mode.toInt(), mode);
        }
    }

    @Nullable
    public static LightMode fromString(String s){
        return modeMap.get(s.toUpperCase());
    }

    @Nullable
    public static LightMode fromInt(Integer n) {
        return intMap.get(n);
    }

    public int toInt() {
        return this.ordinal() + 1;
    }
}
