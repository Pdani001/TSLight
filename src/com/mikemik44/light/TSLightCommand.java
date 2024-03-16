package com.mikemik44.light;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class TSLightCommand implements TabExecutor {
	private final TsLight plugin;

	public TSLightCommand(TsLight plugin) {
		this.plugin = plugin;
	}

	@Override
    public boolean onCommand(CommandSender sender, Command command,  String label,  String[] args) {
        if(!sender.hasPermission("tslight.use")){
            return true;
        }
        TsLight.inputData.put(((Player)sender).getUniqueId(), new ArrayList<>());
        sender.sendMessage("Please type either \"on\", \"off\", or \"both\" in chat! \"on\" means it will be fully turned on when on time has reached but will revert to normal state when off time reached. \"off\" means will turn off if off time reached, but will return to normal state when on time is reached! Finally both means it will be fully on when on the on time, but will be fully off when on off time!");
        return true;
    }

	@Override
    public List<String> onTabComplete( CommandSender sender,  Command command,  String alias, String[] args) {
        ArrayList<String> tabs = new ArrayList<>();
        if(!sender.hasPermission("tslight.use")) {
            return tabs;
        }
       
       
        return tabs;
    }
}
