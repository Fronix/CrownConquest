/**
 *  Name: LocationManager.java
 *  Date: 21:26:55 - 23 okt 2012
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

import org.bukkit.Location;

import me.lucasemanuel.crownconquest.Main;
import me.lucasemanuel.crownconquest.utils.ConsoleLogger;
import me.lucasemanuel.crownconquest.utils.SLAPI;
import me.lucasemanuel.crownconquest.utils.SerializedLocation;

public class LocationManager {
	
	private Main plugin;
	private ConsoleLogger logger;
	
	private Location spectatorspawn;
	
	public LocationManager(Main instance) {
		plugin = instance;
		logger = new ConsoleLogger(instance, "LocationManager");
		
		loadSave();
		
		logger.debug("Initiated");
	}

	private void loadSave() {
		
		spectatorspawn = null;
		
		SerializedLocation savedloc = null;
		
		try {
			savedloc = (SerializedLocation) SLAPI.load(plugin.getDataFolder() + "spectatorspawn.dat");
		}
		catch(Exception e) {
			logger.warning("Error while loading spectatorspawn! Message: " + e.getMessage());
		}
		
		if(savedloc != null) {
			spectatorspawn = savedloc.deserialize();
		}
	}
	
	public void setSpectatorSpawn(Location location) {
		spectatorspawn = location;
		
		saveSpectatorSpawn();
	}
	
	public Location getSpectatorSpawn() {
		return spectatorspawn;
	}

	private void saveSpectatorSpawn() {

		if(spectatorspawn != null) {
			
			SerializedLocation temploc = new SerializedLocation(spectatorspawn);
			
			try {
				SLAPI.save(temploc, plugin.getDataFolder() + "spectatorspawn.dat");
			}
			catch(Exception e) {
				logger.severe("Could not save spectatorspawn! Message: " + e.getMessage());
			}
		}
	}
}