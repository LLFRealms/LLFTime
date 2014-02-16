package com.llfrealms.LLFTime.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.llfrealms.LLFTime.LLFTime;

public class LLFTimeListeners implements Listener {
	
	private LLFTime plugin;
	
	public LLFTimeListeners(LLFTime plugin)
	{
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}
	@EventHandler // EventPriority.NORMAL by default
    public void onLogin(PlayerLoginEvent event) 
    {
        Player player = event.getPlayer();
        String play = player.toString();
	    play = play.replaceAll("CraftPlayer\\{name=", "");
	    play = play.replaceAll("\\}", "");
	    ResultSet rs = plugin.query("SELECT user FROM "+plugin.pluginname+"_users WHERE user = \'" + play + "\'");
	    try {
	    	rs.first();
			if(rs.getString("user").equalsIgnoreCase(play))
			{
			}
		} catch (SQLException e) {
		    String sql = "INSERT INTO "+plugin.pluginname+"_users VALUES(\'" + play + "\')";
		    String sql2 = "INSERT INTO "+plugin.pluginname+"_rewarded VALUES(\'" + play + "\', \'Default\')";
			plugin.addRecord(sql2);
		    plugin.addRecord(sql);
		}
	    plugin.ontimeCheck2(play);
    }
}
