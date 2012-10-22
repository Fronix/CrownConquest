/**
 *  Name: CommandManager.java
 *  Date: 19:55:50 - 12 okt 2012
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

import me.lucasemanuel.crownconquest.Main;
import me.lucasemanuel.crownconquest.utils.ConsoleLogger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {
	
	private Main plugin;
	private ConsoleLogger logger;
	
	public CommandManager(Main instance) {
		plugin = instance;
		logger = new ConsoleLogger(instance, "CommandExecutor");
		
		logger.debug("Initiated");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String command = cmd.getName().toLowerCase();
		
		switch(command) {
			
			case "start":
				plugin.start();
				plugin.getServer().broadcastMessage(ChatColor.GOLD + "Spelet startar om " + ChatColor.LIGHT_PURPLE + "2 minuter!");
				return true;
				
			case "setcrownspawn":
				if(!isPlayer(sender)) return true;
				
				plugin.getCrownManager().setSpawn(((Player)sender).getLocation());
				sender.sendMessage(ChatColor.GREEN + "Plats sparad!");
				
				return true;
				
			case "setteamspawn":
				if(!isPlayer(sender)) return true;
				if(args.length != 1) return false;
				
				plugin.getTeamManager().setSpawn(((Player)sender).getLocation(), args[0]);
				sender.sendMessage(ChatColor.GREEN + "Lagplats sparad med ID: " + ChatColor.LIGHT_PURPLE + args[0]);
				
				return true;
				
			case "spawncrown":
				plugin.getCrownManager().spawnCrown();
				return true;
		}
		
		return false;
	}
	
	private boolean isPlayer(CommandSender sender) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Du m�ste vara spelare f�r detta kommando!");
			return false;
		}
		return true;
	}
}
