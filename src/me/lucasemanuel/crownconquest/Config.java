/**
 *  Name: Config.java
 *  Date: 14:33:03 - 9 okt 2012
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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
	public static void load(JavaPlugin plugin) {
		
		FileConfiguration config = plugin.getConfig();
		boolean save = false;
		
		if(!config.contains("debug")) {
			config.set("debug", false);
			save = true;
		}
		
		if(!config.contains("team")) {
			config.set("team.LucasEmanuel", "sture");
			save = true;
		}
		
		if(save) {
			plugin.saveConfig();
		}
	}
}
