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

import me.lucasemanuel.crownconquest.listeners.BlockListener;
import me.lucasemanuel.crownconquest.listeners.PlayerListener;
import me.lucasemanuel.crownconquest.managers.ChestManager;
import me.lucasemanuel.crownconquest.managers.CommandManager;
import me.lucasemanuel.crownconquest.managers.CrownManager;
import me.lucasemanuel.crownconquest.managers.LocationManager;
import me.lucasemanuel.crownconquest.managers.TeamManager;
import me.lucasemanuel.crownconquest.utils.*;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	private ConsoleLogger logger;
	
	private TeamManager     teammanager;
	private CrownManager    crownmanager;
	private ChestManager    chestmanager;
	private LocationManager locationmanager;
	
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
		
		teammanager     = new TeamManager(this);
		crownmanager    = new CrownManager(this);
		chestmanager    = new ChestManager(this);
		locationmanager = new LocationManager(this);
		
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
		this.getCommand("teams").setExecutor(commands);
		this.getCommand("setspectatorspawn").setExecutor(commands);
		
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
		}, 24000L);
	}
	
	public boolean getStatus() {
		return status;
	}
	
	public LocationManager getLocationManager() {
		return locationmanager;
	}
	
	public ChestManager getChestManager() {
		return chestmanager;
	}

	public TeamManager getTeamManager() {
		return teammanager;
	}

	public CrownManager getCrownManager() {
		return crownmanager;
	}
}
