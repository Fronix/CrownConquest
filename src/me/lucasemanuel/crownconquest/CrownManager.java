/**
 *  Name: CrownManager.java
 *  Date: 16:19:00 - 21 okt 2012
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

import java.util.HashSet;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.lucasemanuel.crownconquest.utils.ConsoleLogger;
import me.lucasemanuel.crownconquest.utils.SLAPI;
import me.lucasemanuel.crownconquest.utils.SerializedLocation;

public class CrownManager {
	
	private Main plugin;
	private ConsoleLogger logger;
	
	private Random random;
	
	private HashSet<Location> crownspawns;
	
	public CrownManager(Main instance) {
		plugin = instance;
		logger = new ConsoleLogger(instance, "CrownManager");
		
		random = new Random();
		
		loadSpawns();
		
		logger.debug("Initiated");
	}

	@SuppressWarnings("unchecked")
	private void loadSpawns() {
		
		crownspawns = new HashSet<Location>();
		
		HashSet<SerializedLocation> loadedSpawns = null;
		
		try {
			loadedSpawns = (HashSet<SerializedLocation>) SLAPI.load("plugins/CrownConquest/crownspawns.dat");
		}
		catch(Exception e) {
			logger.warning("Error while loading saved spawns! Message: " + e.getMessage());
		}
		
		if(loadedSpawns != null) {
			for(SerializedLocation location : loadedSpawns) {
				crownspawns.add(location.deserialize());
			}
		}
		
	}

	public void setSpawn(Location location) {
		crownspawns.add(location);
		
		saveSpawnlist();
	}

	private void saveSpawnlist() {
		
		HashSet<SerializedLocation> templist = new HashSet<SerializedLocation>();
		
		for(Location location : crownspawns) {
			templist.add(new SerializedLocation(location));
		}
		
		try {
			SLAPI.save(templist, "plugins/CrownConquest/crownspawns.dat");
		}
		catch(Exception e) {
			logger.severe("Error while saving crownspawns! Message: " + e.getMessage());
		}
	}

	public void spawnCrown() {
		
		Location location = (Location) crownspawns.toArray()[random.nextInt(crownspawns.size())];
		
		location.getWorld().dropItem(location, new ItemStack(Material.GOLD_HELMET));
		location.getWorld().createExplosion(location, 0.0F);
		
		plugin.getServer().broadcastMessage(ChatColor.GOLD + "En ny krona har spawnats!");
	}
}
