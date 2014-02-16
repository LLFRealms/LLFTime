package com.llfrealms.LLFTime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import me.edge209.OnTime.OnTimeAPI;
import me.edge209.OnTime.OnTimeAPI.data;

import com.llfrealms.LLFTime.util.LLFTimeListeners;
import com.llfrealms.LLFTime.util.Utilities;
import com.llfrealms.LLFTime.util.LLFTimeCommands;

public final class LLFTime extends JavaPlugin 
{

	public Statement stmt = null;
	public Connection connection = null;
	public ResultSet result = null;
	private String database, dbusername, dbpassword, host;
	private int port;
	public String prefix;
	public ArrayList<Integer> time = new ArrayList<Integer>();
	public ArrayList<String> name = new ArrayList<String>();
	public ConsoleCommandSender consoleMessage = Bukkit.getConsoleSender();
	public String pluginname = "LLFTime";
	
	public void onEnable()
    {
    	this.saveDefaultConfig();
    	this.getConfig();
    	new LLFTimeListeners(this);
        
    	Utilities.sendMessage(consoleMessage,"["+pluginname+"] &aWelcome Jack's second plugin! It's a little better!!");
        
        OnTimeRewardSetup();
        
        getCommand("tllfadd").setExecutor(new LLFTimeCommands(this));
        getCommand("tllfload").setExecutor(new LLFTimeCommands(this));
        getCommand("tllfsave").setExecutor(new LLFTimeCommands(this));
        
        prefix = getConfig().getString("MySQL.database.ontimeTable");
        host = getConfig().getString("MySQL.server.address");
        port = getConfig().getInt("MySQL.server.port");
        database = getConfig().getString("MySQL.database.database");
        dbusername = getConfig().getString("MySQL.database.username");
        dbpassword = getConfig().getString("MySQL.database.password");
       
        connect(); //connect to database
        tableCheck(); //check to make sure our table exists and if not creates it.
        
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				ontimeCheck();
			}
        }, 6000L, 6000L);
    }

    @Override
    public void onDisable() 
    {
        getLogger().info("Closing "+pluginname);
    }
    public void tableCheck()
    {
    	Utilities.sendMessage(consoleMessage, "Making sure our table exists");
    	String sql = "CREATE TABLE IF NOT EXISTS "+pluginname+"_rewarded" +
    				 "(user varchar(255),"+
    				 "reward varchar(255))";
    	String sql2 = "CREATE TABLE IF NOT EXISTS "+pluginname+"_users" +
				 "(user varchar(255))";
    	try {
    		stmt = connection.createStatement();
    		stmt.executeUpdate(sql);
    		stmt.executeUpdate(sql2);
		} catch (SQLException ex) {
            // handle any errors
        	getLogger().info("SQLException: " + ex.getMessage());
        	getLogger().info("SQLState: " + ex.getSQLState());
        	getLogger().info("VendorError: " + ex.getErrorCode());
        }
    	if (stmt != null) 
	    {
	        try {
	        	stmt.close();
	        } catch (SQLException sqlEx) { } // ignore

	        stmt = null;
	    }
    }
    public ResultSet query(String query)
	{
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
		} catch (SQLException ex) {
            // handle any errors
        	getLogger().info("SQLException: " + ex.getMessage());
        	getLogger().info("SQLState: " + ex.getSQLState());
        	getLogger().info("VendorError: " + ex.getErrorCode());
        }
		return rs;
	}
    public void OnTimeRewardSetup()
	{
		List<Integer> otime = getConfig().getIntegerList("rSetup.time");
		List<String> reward = getConfig().getStringList("rSetup.name");
		for(int s : otime)
		{
			time.add(s);
		}
		for(String s : reward)
		{
			name.add(s);
		}
	}
    public void addRecord(String sql)
    {
    	try {
			stmt = connection.createStatement();
			 stmt.executeUpdate(sql);
		} catch (SQLException ex) {
            // handle any errors
			getLogger().info("SQLException: " + ex.getMessage());
			getLogger().info("SQLState: " + ex.getSQLState());
			getLogger().info("VendorError: " + ex.getErrorCode());
        }
    	if (stmt != null) 
	    {
	        try {
	        	stmt.close();
	        } catch (SQLException sqlEx) { } // ignore

	        stmt = null;
	    }
    }
    public void connect() {
        String connectionString = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try {
            getLogger().info("Attempting connection to MySQL...");

            // Force driver to load if not yet loaded
            Class.forName("com.mysql.jdbc.Driver");
            Properties connectionProperties = new Properties();
            connectionProperties.put("user", dbusername);
            connectionProperties.put("password", dbpassword);
            connectionProperties.put("autoReconnect", "false");
            connectionProperties.put("maxReconnects", "0");
            connection = DriverManager.getConnection(connectionString, connectionProperties);
            Utilities.sendMessage(consoleMessage,"["+pluginname+"] &aConnection to MySQL was a success!");
        }
        catch (SQLException ex) {
            connection = null;
            Utilities.sendMessage(consoleMessage, "&4[SEVERE] Connection to MySQL failed!");
            getLogger().info("SQLException: " + ex.getMessage());
        	getLogger().info("SQLState: " + ex.getSQLState());
        	getLogger().info("VendorError: " + ex.getErrorCode());
        }
        catch (ClassNotFoundException ex) {
            connection = null;
            getLogger().severe("MySQL database driver not found!");
        }
    }
    public void ontimeCheck()
    {
    	for(Player player: this.getServer().getOnlinePlayers()) 
    	{
    		 String play = player.toString();
    		 play = play.replaceAll("CraftPlayer\\{name=", "");
    		 play = play.replaceAll("\\}", "");
    		 ArrayList<Boolean> check = new ArrayList<Boolean>();
    		 for(int i = 0; i < time.size(); i++)
    		    {
    		    	check.add(false);
    		    }
    		 for(int i = 0; i < time.size(); i++)
    		 {
    			 String reward = name.get(i);
    			 long otime = getConfig().getLong("rewards."+reward+".time");
    			 otime = otime * 60 * 1000;
    			 ResultSet rwCheck = query("SELECT reward FROM "+pluginname+"_rewarded WHERE user =\'" + play + "\'");
    			 try {
 					while(rwCheck.next())
 					  {
 						  String rewardCheck = rwCheck.getString("reward");
 						  if(rewardCheck.equalsIgnoreCase(reward))
 						  {
 							  check.set(i, true);
 						  }
 					  }
 				} catch (SQLException e) {}//ignore
    			if(!check.get(i))
    			{
    				long totaltime = OnTimeAPI.getPlayerTimeData(play, data.TOTALPLAY);
    				if(totaltime >= otime)
    				{
    					String command = getConfig().getString("rewards." + reward + ".commands");
  					  command = command.replaceAll("\\{player\\}", play);
  					  String[] command2 = command.split("/");
  					  for(int c = 1; c < command2.length; c++)
  					  {
  						  Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command2[c]);
  					  }
  					String sql = "INSERT INTO "+pluginname+"_rewarded " +
					  		"VALUES(\'"+play+"\', \'"+reward+"\')";
  					addRecord(sql);
    				}
    				
    				
    			}
    		 }
    		 
    	}
    }
    public void ontimeCheck2(String play)
    {
    		Utilities.sendMessage(consoleMessage, "&4Checking OnTime Rewards on login of " + play+"!");
    		 ArrayList<Boolean> check = new ArrayList<Boolean>();
    		 for(int i = 0; i < time.size(); i++)
    		    {
    		    	check.add(false);
    		    }
    		 for(int i = 0; i < time.size(); i++)
    		 {
    			 String reward = name.get(i);
    			 long otime = getConfig().getLong("rewards."+reward+".time");
    			 otime = otime * 60 * 1000;
    			 ResultSet rwCheck = query("SELECT reward FROM "+pluginname+"_rewarded WHERE user =\'" + play + "\'");
    			 try {
 					while(rwCheck.next())
 					  {
 						  String rewardCheck = rwCheck.getString("reward");
 						  if(rewardCheck.equalsIgnoreCase(reward))
 						  {
 							  check.set(i, true);
 						  }
 					  }
 				} catch (SQLException e) {}//ignore
    			if(!check.get(i))
    			{
    				long totaltime = OnTimeAPI.getPlayerTimeData(play, data.TOTALPLAY);
    				if(totaltime >= otime)
    				{
    					String command = getConfig().getString("rewards." + reward + ".commands");
  					  command = command.replaceAll("\\{player\\}", play);
  					  String[] command2 = command.split("/");
  					  for(int c = 1; c < command2.length; c++)
  					  {
  						  Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command2[c]);
  					  }
  					String sql = "INSERT INTO "+pluginname+"_rewarded " +
					  		"VALUES(\'"+play+"\', \'"+reward+"\')";
  					addRecord(sql);
    				}
    				
    				
    			}
    		 }
    		 
    	}
}
