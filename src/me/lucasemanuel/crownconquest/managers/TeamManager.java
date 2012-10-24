/**
 *  Name: TeamManager.java
 *  Date: 16:58:08 - 20 okt 2012
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

package me.lucasemanuel.crownconquest.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.lucasemanuel.crownconquest.Main;
import me.lucasemanuel.crownconquest.utils.ConsoleLogger;
import me.lucasemanuel.crownconquest.utils.SLAPI;
import me.lucasemanuel.crownconquest.utils.SerializedLocation;

public class TeamManager {
	
	private Main plugin;
	private ConsoleLogger logger;
	
	private HashMap<String, Location> teamspawnlocations;
	private HashMap<Location, String> teamsignlocations;
	
	private HashMap<String, HashSet<Player>> playerlists;
	
	public TeamManager(Main instance) {
		plugin = instance;
		logger = new ConsoleLogger(instance, "TeamManager");
		
		playerlists = new HashMap<String, HashSet<Player>>();
		
		loadTeamLocations();
		
		logger.debug("Initiated");
	}
	
	@SuppressWarnings("unchecked")
	private void loadTeamLocations() {
		
		teamspawnlocations = new HashMap<String, Location>();
		teamsignlocations  = new HashMap<Location, String>();
		
		HashMap<String, SerializedLocation> loadedTeamSpawns = null;
		HashMap<SerializedLocation, String> loadedSigns      = null;
		
		/*
		 *  --- Spawns
		 */
		try {
			loadedTeamSpawns = (HashMap<String, SerializedLocation>) SLAPI.load(plugin.getDataFolder() + "/teamspawnlocations.dat");
		}
		catch(Exception e) {
			logger.warning("Error while loading teamspawnlocations! Message: " + e.getMessage());
		}
		
		if(loadedTeamSpawns != null) {
			for(Entry<String, SerializedLocation> entry : loadedTeamSpawns.entrySet()) {
				teamspawnlocations.put(entry.getKey(), entry.getValue().deserialize());
			}
		}
		
		/* 
		 *  --- Signs
		 */
		try {
			loadedSigns = (HashMap<SerializedLocation, String>) SLAPI.load(plugin.getDataFolder() + "/teamsignlocations.dat");
		}
		catch(Exception e) {
			logger.warning("Error while loading teamsignlocations! Message: " + e.getMessage());
		}
		
		if(loadedSigns != null) {
			for(Entry<SerializedLocation, String> entry : loadedSigns.entrySet()) {
				registerTeamSign(((Sign)entry.getKey().deserialize().getBlock().getState()), entry.getValue());
			}
		}
	}

	public void setSpawn(Location location, String teamname) {
		teamspawnlocations.put(teamname, location);
		
		saveData();
	}

	@SuppressWarnings("deprecation")
	public boolean addToTeam(Player player, String teamname) {
		
		String playerteam = getTeamNameFromPlayer(player);
		
		if(playerlists.containsKey(teamname)) {
			if(playerteam == null) {
				
				HashSet<Player> playerlist = playerlists.get(teamname);
				
				playerlist.add(player);
				
				player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN));
				player.updateInventory();
				
				return true;
			}
			else {
				player.sendMessage(ChatColor.RED + "Du befinner dig redan i laget: " 
									+ ChatColor.LIGHT_PURPLE + playerteam);
			}
		}
		else {
			logger.warning("Tried to add player: " + player.getName() 
					+ " to nonexisting team! Teamname: " + teamname);
		}
		
		return false;
	}

	public void registerTeamSign(Sign sign, String teamname) {
		
		teamsignlocations.put(sign.getLocation(), teamname);
		playerlists.put(teamname, new HashSet<Player>());
		
		sign.setLine(0, "");
		sign.setLine(1, ChatColor.GOLD + " - Lag - ");
		sign.setLine(2, ChatColor.LIGHT_PURPLE + teamname);
		sign.setLine(3, "");
		
		sign.update();
		
		saveData();
	}

	public String getTeamNameFromSign(Location location) {
		if(teamsignlocations.containsKey(location)) 
			return teamsignlocations.get(location);
		else
			return null;
	}

	public void teleportToTeamSpawn(Player player) {
		
		String teamname = getTeamNameFromPlayer(player);
		
		if(teamname != null) {
			Location spawn = getTeamSpawn(teamname);
			
			if(spawn != null) {
				player.teleport(spawn);
			}
			else {
				player.sendMessage(ChatColor.RED + "Finns ingen spawn för laget!");
			}
		}
		else {
			player.sendMessage(ChatColor.RED + "Laget finns inte!");
		}
	}

	private Location getTeamSpawn(String teamname) {
		
		if(teamspawnlocations.containsKey(teamname)) return teamspawnlocations.get(teamname);
		
		return null;
	}

	public String getTeamNameFromPlayer(Player player) {
		
		for(Entry<String, HashSet<Player>> entry : playerlists.entrySet()) {
			for(Player tempplayer : entry.getValue()) {
				if(player == tempplayer) return entry.getKey();
			}
		}
		
		return null;
	}

	public void removePlayer(Player player) {
		
		String teamname = getTeamNameFromPlayer(player);
		
		if(teamname != null) {
			playerlists.get(teamname).remove(player);
		}
	}
	
	private void saveData() {
		
		HashMap<String, SerializedLocation> tempspawn = new HashMap<String, SerializedLocation>();
		HashMap<SerializedLocation, String> tempsign  = new HashMap<SerializedLocation, String>();
		
		for(Entry<String, Location> entry : teamspawnlocations.entrySet()) {
			tempspawn.put(entry.getKey(), new SerializedLocation(entry.getValue()));
		}
		
		for(Entry<Location, String> entry : teamsignlocations.entrySet()) {
			tempsign.put(new SerializedLocation(entry.getKey()), entry.getValue());
		}
		
		try {
			SLAPI.save(tempspawn, plugin.getDataFolder() + "/teamspawnlocations.dat");
			SLAPI.save(tempsign,  plugin.getDataFolder() + "/teamsignlocations.dat");
		}
		catch(Exception e) {
			logger.severe("Error while saving data! Message: " + e.getMessage());
		}
	}

	public void addToConfiguredTeam(Player player) {
		
		String teamname = plugin.getConfig().getString("team." + player.getName().toLowerCase());
		
		if(teamname != null) {
			addToTeam(player, teamname);
		}
	}

	public void sendTeamInfo(CommandSender sender) {

		for(Entry<String, HashSet<Player>> entry : playerlists.entrySet()) {
			sender.sendMessage("Lag: " + ChatColor.GREEN + entry.getKey());
			
			for(Player player : entry.getValue()) {
				sender.sendMessage(" - " + ChatColor.LIGHT_PURPLE + player.getName());
			}
		}
	}
}