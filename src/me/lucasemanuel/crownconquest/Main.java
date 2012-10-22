/**
 *  Name: Main.java
 *  Date: 21:21:59 - 8 okt 2012
 * 
 *  Author: LucasEmanuel @ bukkit forums
 *  
 *  
 *  Description:
 *  
 *  
 *  
 * 
 * 
 */

package me.lucasemanuel.crownconquest;

import me.lucasemanuel.crownconquest.utils.*;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	private ConsoleLogger logger;
	
	private TeamManager  teammanager;
	private CrownManager crownmanager;
	
	private boolean status = false;
	
	public void onEnable() {
		logger = new ConsoleLogger(this, "Main");
		
		/*
		 *  --- General
		 */
		logger.debug("Loading config...");

		Config.load(this);
		
		logger.debug("... done!");
		
		/*
		 *  --- Managers
		 */
		logger.debug("Initiating managers...");
		
		teammanager  = new TeamManager(this);
		crownmanager = new CrownManager(this);
		
		logger.debug("... done!");
		
		/*
		 *  --- Listeners
		 */
		logger.debug("Initating listeners...");

		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		this.getServer().getPluginManager().registerEvents(new BlockListener(this), this);
		
		logger.debug("... done!");
		
		/*
		 *  --- Commands
		 */
		logger.debug("Registering commands...");
		
		CommandManager commands = new CommandManager(this);
		
		this.getCommand("start").setExecutor(commands);
		this.getCommand("setcrownspawn").setExecutor(commands);
		this.getCommand("setteamspawn").setExecutor(commands);
		this.getCommand("spawncrown").setExecutor(commands);
		
		logger.debug("... done!");
		
		/*
		 *  --- Finished
		 */
		logger.debug("Startup sequence finished!");
	}
	
	private void activate() {
		status = true;
		this.getServer().broadcastMessage(ChatColor.GOLD + "Spelet har startat!");
	}

	public void start() {
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				activate();
			}
		}, 2400L);
	}
	
	public boolean getStatus() {
		return status;
	}

	public TeamManager getTeamManager() {
		return teammanager;
	}

	public CrownManager getCrownManager() {
		return crownmanager;
	}
}
