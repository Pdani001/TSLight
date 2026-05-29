package com.mikemik44.light;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class TSLightCommand implements TabExecutor {
	private final TSLight plugin;
    private final HashMap<UUID, SetupData> setupData = new HashMap<>();

	public TSLightCommand(TSLight plugin) {
		this.plugin = plugin;
	}

	@Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!sender.hasPermission("tslight.use") || !(sender instanceof Player player)){
            return true;
        }
        if(args.length == 0){
            player.sendMessage("Invalid number of args provided!");
            return false;
        }
        if(!setupData.containsKey(player.getUniqueId())){
            if(!args[0].equalsIgnoreCase("setup"))
                return true;
            setupData.put(player.getUniqueId(), new SetupData());
            player.sendMessage("- Entering LightZone setup mode -");
            player.sendMessage("Step 1: Please enter either \"on\", \"off\", or \"both\" as the first argument to this command!");
            player.sendMessage("\"on\" means it will be fully turned on when the given on time has been reached but will revert to normal state when the off time is reached.");
            player.sendMessage("\"off\" means it will turn off when the off time is reached, but will return to normal state when the on time is reached!");
            player.sendMessage("Finally \"both\" means it will be fully on while its in the on time, but will be fully off when in the off time! (For example if on time is 6000 and off time is 12000 anything between these is turned on, outside of these is turned off)");
            player.sendMessage("- To cancel this setup at any time, please type \"exit\" as the first argument -");
            return true;
        }
        if(args[0].equalsIgnoreCase("exit")){
            player.sendMessage("- Exiting LightZone setup mode -");
            player.sendMessage("All changes have been discarded!");
            setupData.remove(player.getUniqueId());
            return true;
        }
        SetupData data = setupData.get(player.getUniqueId());
        switch (data.currentStep){
            case 0 -> {
                LightMode mode = LightMode.fromString(args[0]);
                if(mode != null){
                    data.mode = mode;
                    data.currentStep++;
                    switch (mode) {
                        case ON, BOTH -> player.sendMessage("Step 2: What time do you want the lights to turn ON?");
                        case OFF -> player.sendMessage("Step 2: What time do you want the lights to turn OFF?");
                    }
                    player.sendMessage("Valid values include %s and any number between 0 and 24000".formatted(String.join(", ", Arrays.stream(Times.values()).map(Enum::name).toList())));
                } else {
                    player.sendMessage("Invalid mode (%s) provided!".formatted(args[0]));
                }
            }
            case 1 -> {
                int time = -1;
                if(args[0].matches("[0-9]+")){
                    time = (int) Times.normalize(Integer.parseInt(args[0]));
                } else {
                    time = (int) Times.convertFromTimeToDay(args[0]);
                }
                if(time < 0)
                    player.sendMessage("Invalid time (%s) provided!".formatted(args[0]));
                else {
                    data.currentStep++;
                    switch (data.mode){
                        case ON -> {
                            data.timeOn = time;
                            player.sendMessage("Step 3: When do you want to have it return to default light value?");
                        }
                        case OFF -> {
                            data.timeOff = time;
                            player.sendMessage("Step 3: When do you want to have it return to default light value?");
                        }
                        case BOTH -> {
                            data.timeOn = time;
                            player.sendMessage("Step 3: When do you want the light to turn off?");
                        }
                    }
                }
            }
            case 2 -> {
                int time = -1;
                if(args[0].matches("[0-9]+")){
                    time = (int) Times.normalize(Integer.parseInt(args[0]));
                } else {
                    time = (int) Times.convertFromTimeToDay(args[0]);
                }
                if(time < 0)
                    player.sendMessage("Invalid time (%s) provided!".formatted(args[0]));
                else {
                    data.currentStep++;
                    switch (data.mode){
                        case ON, BOTH -> data.timeOff = time;
                        case OFF -> data.timeOn = time;
                    }
                    player.sendMessage("Step 4: Enter min and max coordinates or use world edit to make a selection!");
                }
            }
            case 3 -> {
                if(args[0].equalsIgnoreCase("finished")){
                    com.sk89q.worldedit.entity.Player wep = BukkitAdapter.adapt(player);
                    LocalSession w = WorldEdit.getInstance().getSessionManager().get(wep);
                    try {
                        if(w == null){
                            throw new IncompleteRegionException();
                        }
                        Region r = w.getSelection(w.getSelectionWorld());
                        finishSetup(player, data, toVector(r.getMinimumPoint().toVector3()), toVector(r.getMaximumPoint().toVector3()));
                    } catch (IncompleteRegionException e) {
                        player.sendMessage("You must make a world edit selection to use this option, or provide two valid coordinates!");
                        break;
                    }
                }
                if(!args[0].equalsIgnoreCase("finished") && args.length < 6) {
                    player.sendMessage("Invalid number of arguments! Expected 6, only got %d".formatted(args.length));
                    break;
                }
                List<Integer> coords = Arrays.stream(args).map(this::toNumber).toList();
                if (coords.contains(null)) {
                    player.sendMessage("Invalid coordinates (%s) entered!".formatted(String.join(" ", args)));
                    break;
                }
                finishSetup(player, data,
                        new Vector(coords.get(0), coords.get(1), coords.get(2)),
                        new Vector(coords.get(3), coords.get(4), coords.get(5)));
            }
        }
        return true;
    }

    private Vector toVector(Vector3 loc) {
        return new Vector(loc.x(), loc.y(), loc.z());
    }

    private Integer toNumber(String num) {
        if (num.matches("-?[0-9]+")) {
            return Integer.parseInt(num);
        } else {
            return null;
        }
    }

    private void finishSetup(Player player, SetupData data, Vector min, Vector max) {
        LightZone lz = new LightZone(player.getWorld(),
                String.valueOf(plugin.getNextID()), data.mode.toInt(), data.timeOn, data.timeOff,
                min,
                max);
        setupData.remove(player.getUniqueId());
        plugin.addLightZone(lz);
        player.sendMessage("Added %d number of lights!".formatted(lz.getLightBlocks().size()));
        player.sendMessage("- Exiting LightZone setup mode -");
    }

	@Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if(!sender.hasPermission("tslight.use") || !(sender instanceof Player player)) {
            return List.of();
        }
        ArrayList<String> tabs = new ArrayList<>();
        if (setupData.containsKey(player.getUniqueId())){
            SetupData data = setupData.get(player.getUniqueId());
            switch (data.currentStep){
                case 0 -> tabs.addAll(Arrays.stream(LightMode.values()).map(Enum::name).toList());
                case 1, 2 -> {
                    tabs.add(String.valueOf(Times.getWorldTime(player.getWorld())));
                    tabs.addAll(Arrays.stream(Times.values()).map(Enum::name).toList());
                }
                case 3 -> {
                    if(args.length <= 1)
                        tabs.add("finished");
                }
            }
            if (args.length <= 1)
                tabs.add("exit");
        } else {
            tabs.add("setup");
        }
        return tabs;
    }
}
