package com.llfrealms.LLFTime.util;

import org.bukkit.command.*;

import com.llfrealms.LLFTime.util.Utilities;
import com.llfrealms.LLFTime.LLFTime;

public class LLFTimeCommands  implements CommandExecutor 
{
	private LLFTime plugin;
	public LLFTimeCommands(LLFTime plugin) {
		this.plugin = plugin;
	}
 
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		if(cmd.getName().equalsIgnoreCase("tllfsave") && sender.hasPermission("tllf.save"))
	    {
			plugin.saveConfig();
        	sender.sendMessage("Config saved");
        	return true;
	    }
		if(cmd.getName().equalsIgnoreCase("tllfload")  && sender.hasPermission("tllf.load"))
	    {
			plugin.reloadConfig();
        	sender.sendMessage("Config reloaded");
        	return true;
	    }
		if(cmd.getName().equalsIgnoreCase("tllfadd")  && sender.hasPermission("tllf.add"))
	    {
			// /<command> {rewardName} {time} {commands}
			String reward = args[0], otime = args[1];
			Integer time = Integer.parseInt(otime);
			boolean rewardExists = false;
			for(int i = 0; i < plugin.name.size(); i++)
			{
				if(plugin.name.get(i).equalsIgnoreCase(reward))
				{
					rewardExists = true;
				}
			}
			if(rewardExists)
			{
				Utilities.sendMessage(sender, reward + " already exists.");
				return true;
			}
			else
			{
				String commands = Utilities.getFinalArg(args, 2);
				plugin.time.add(time);
				plugin.name.add(reward);
				plugin.getConfig().set("rSetup.time", time);
				plugin.getConfig().set("rSetup.name", reward);
				plugin.getConfig().createSection("rewards."+reward);
				plugin.getConfig().createSection("rewards."+reward+".requirements");
				plugin.getConfig().createSection("rewards."+reward+".commands");
				plugin.getConfig().set("rewards."+reward+".requirements", time);
				plugin.getConfig().set("rewards."+reward+".commands", commands);
				return true;
				
			}
	    }
		return false;
	}
}
