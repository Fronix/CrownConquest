/**
 *  Name: PlayerListener.java
 *  Date: 14:36:09 - 9 okt 2012
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

package me.lucasemanuel.crownconquest.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.lucasemanuel.crownconquest.Main;
import me.lucasemanuel.crownconquest.utils.ConsoleLogger;

public class PlayerListener implements Listener {
	
	private Main plugin;
	private ConsoleLogger logger;
	
	public PlayerListener(Main instance) {
		plugin = instance;
		logger = new ConsoleLogger(instance, "PlayerListener");
		
		logger.debug("Initiated!");
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		
		event.setRespawnLocation(event.getRespawnLocation().getWorld().getSpawnLocation());
		
		plugin.getTeamManager().removePlayer(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		
		plugin.getTeamManager().removePlayer(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onEntityDamage(EntityDamageEvent event) {
		
		if(event.getEntity() instanceof Player 
				&& !plugin.getStatus()) {
			
			event.setCancelled(true);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		Player player = event.getPlayer();
		Block  block  = event.getClickedBlock();
		
		if(block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN_POST) && !plugin.getStatus()) {
			
			String teamname = plugin.getTeamManager().getTeamNameFromSign(block.getLocation());
			
			if(teamname != null) {
				if(plugin.getTeamManager().addToTeam(player, teamname)) {
					plugin.getTeamManager().teleportToTeamSpawn(player);
					player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN));
					player.updateInventory();
				}
			}
			else if(player.hasPermission("crownconquest.signs.registerteamsign")){
				
				Sign sign = (Sign) block.getState();
				
				if(sign.getLine(0).equalsIgnoreCase("[lag]") && sign.getLine(1) != "" && sign.getLine(1) != null) {
					
					teamname = sign.getLine(1);
					plugin.getTeamManager().registerTeamSign(sign, teamname);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerPickup(PlayerPickupItemEvent event) {
		
		final Player player = event.getPlayer();
		final Item   item   = event.getItem();
		
		if(item.getItemStack().getType().equals(Material.GOLD_HELMET)) {
			
			if(player.getInventory().getHelmet() != null
					&& player.getInventory().getHelmet().getType().equals(Material.GOLD_HELMET)) {
				event.setCancelled(true);
				return;
			}
			
			player.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 72000000, 3));
			
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					player.getInventory().remove(Material.GOLD_HELMET);
				}
			}, 1L);
			
			plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.WHITE + " har plockat upp kronan!");
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerInventoryClick(InventoryClickEvent event) {
		
		if(event.getSlotType().equals(SlotType.ARMOR)) {
			
			if(event.getCurrentItem().getType().equals(Material.GOLD_HELMET) || event.getCurrentItem().getType().equals(Material.PUMPKIN)) {
				event.setCancelled(true);
			}
		}
	}
}
