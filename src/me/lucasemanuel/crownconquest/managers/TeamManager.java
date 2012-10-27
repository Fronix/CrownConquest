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
			logger.debug("Loading teamspawns...");
			loadedTeamSpawns = (HashMap<String, SerializedLocation>) SLAPI.load("plugins/CrownConquest/teamspawnlocations.dat");
		}
		catch(Exception e) {
			logger.warning("Error while loading teamspawnlocations! Message: " + e.getMessage());
		}
		
		if(loadedTeamSpawns != null) {
			logger.debug("Registering teamspawns...");
			for(Entry<String, SerializedLocation> entry : loadedTeamSpawns.entrySet()) {
				setSpawn(entry.getValue().deserialize(), entry.getKey());
			}
		}
		
		/* 
		 *  --- Signs
		 */
		try {
			logger.debug("Loading teamsigns...");
			loadedSigns = (HashMap<SerializedLocation, String>) SLAPI.load("plugins/CrownConquest/teamsignlocations.dat");
		}
		catch(Exception e) {
			logger.warning("Error while loading teamsignlocations! Message: " + e.getMessage());
		}
		
		if(loadedSigns != null) {
			
			logger.debug("Registering teamsigns...");
			logger.debug("Amount of saved signs: " + loadedSigns.size());
			
			for(Entry<SerializedLocation, String> entry : loadedSigns.entrySet()) {
				logger.debug("Adding sign");
				registerTeamSign(((Sign)entry.getKey().deserialize().getBlock().getState()), entry.getValue());
			}
		}
	}

	public void setSpawn(Location location, String teamname) {
		teamspawnlocations.put(teamname, location);
		
		registerTeam(teamname);
	}
	
	private void registerTeam(String teamname) {
		logger.debug("Trying to register team: " + teamname);
		
		if(!playerlists.containsKey(teamname)) {
			playerlists.put(teamname, new HashSet<Player>());
			logger.debug("Team registered!");
		}
		else
			logger.debug("Team already registered!");
	}

	@SuppressWarnings("deprecation")
	public boolean addToTeam(Player player, String teamname) {
		
		logger.debug("Trying to add player " + player.getName() + " to team " + teamname);
		
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
		
		logger.debug("Registering teamsign for team " + teamname);
		
		teamsignlocations.put(sign.getLocation(), teamname);

		registerTeam(teamname);
		
		sign.setLine(0, "");
		sign.setLine(1, ChatColor.GOLD + " - Lag - ");
		sign.setLine(2, ChatColor.LIGHT_PURPLE + teamname);
		sign.setLine(3, "");
		
		sign.update();
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
				player.sendMessage(ChatColor.RED + "Finns ingen spawn f�r laget!");
			}
		}
		else {
			logger.debug("Tried to teleport player " + player.getName() + " to nonexisting team");
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
	
	public void saveData() {
		
		logger.debug("Saving data");
		
		HashMap<String, SerializedLocation> tempspawn = new HashMap<String, SerializedLocation>();
		HashMap<SerializedLocation, String> tempsign  = new HashMap<SerializedLocation, String>();
		
		logger.debug("Amount of spawns to save: " + teamspawnlocations.size());
		for(Entry<String, Location> entry : teamspawnlocations.entrySet()) {
			tempspawn.put(entry.getKey(), new SerializedLocation(entry.getValue()));
		}
		
		logger.debug("Amount of signs to save: " + teamsignlocations.size());
		for(Entry<Location, String> entry : teamsignlocations.entrySet()) {
			tempsign.put(new SerializedLocation(entry.getKey()), entry.getValue());
		}
		
		try {
			SLAPI.save(tempspawn, "plugins/CrownConquest/teamspawnlocations.dat");
			SLAPI.save(tempsign,  "plugins/CrownConquest/teamsignlocations.dat");
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

/**
 * TODO: Gör så att den skriver ut i nummer ordrning.
 * TODO: Kanske lägga till spelarnas namn på något snyggt sätt?
 */
	public void sendTeamInfo(CommandSender sender) {

		for(Entry<String, HashSet<Player>> entry : playerlists.entrySet()) {
			sender.sendMessage("Lag: " + ChatColor.GREEN + entry.getKey());
			
			for(Player player : entry.getValue()) {
				sender.sendMessage(" - " + ChatColor.LIGHT_PURPLE + player.getName());
			}
		}
	}
}