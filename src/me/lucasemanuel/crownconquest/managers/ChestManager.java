/**
 *  Name: ChestManager.java
 *  Date: 22:08:03 - 7 jul 2012
 * 
 *  Author: LucasEmanuel @ bukkit forums
 *  
 *  
 *  Description:
 *  
 *  Manages all chest related stuff.
 *  Like randomizing and logging etc.
 * 
 */

package me.lucasemanuel.crownconquest.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import me.lucasemanuel.crownconquest.Main;
import me.lucasemanuel.crownconquest.utils.ConsoleLogger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestManager {
	
	private Main plugin;
	private ConsoleLogger logger;
	private FileConfiguration itemConfig;
	
	private ArrayList<Location> randomizedchests;
	
	private RandomCollection<String> itemlist;
	private HashMap<String, RandomCollection<String>> enchantmentlists;
	private Map<String, String> enchable;
	
	private Random generator;
	
	public ChestManager(Main instance) {
		this.plugin = instance;
		this.logger = new ConsoleLogger(this.plugin, "ChestManager");
		
		this.randomizedchests = new ArrayList<Location>();
		this.loadItemList();
		
		this.generator = new Random(System.currentTimeMillis());
		
		this.logger.debug("Initiated");
	}
	
	public void randomizeChest(Chest chest) {
		
		if(!this.randomizedchests.contains(chest.getLocation())) {
			
			int spawnchance = itemConfig.getInt("blankChestChance-OneOutOf");
			
			if(spawnchance > generator.nextInt(spawnchance + 1)) {
			
				logger.debug("Randomizing inventory of a chest!");
				
				Inventory inventory = chest.getInventory();
				inventory.clear();
				
				int items = this.generator.nextInt(itemConfig.getInt("maxAmountOfItems")) + 1;
				
				for(int i = 0 ; i < items ; i++) {
					
					Enchantment enchantment = null;
					String itemname = this.itemlist.next();
					
					ItemStack item = new ItemStack(Material.getMaterial(itemname.toUpperCase()), 1);
					
					if(this.enchable.containsKey(itemname)) {
						
						String itemtype = this.enchable.get(itemname);
						
						// Get the specified enchantchance for the item
						double enchantchance = 0.0d;
						
						if(itemtype.equals("swords") || itemtype.equals("bow")) {
							enchantchance = this.itemConfig.getDouble("weapons." + itemname + ".enchantmentchance");
						} else if (itemtype.equals("armors")) {
							enchantchance = this.itemConfig.getDouble("armors." + itemname + ".enchantmentchance");
						}
						
						// Generate a random double and retrieve an enchantment if the generated value is less or equal to the enchantchance
						if(this.generator.nextDouble() <= enchantchance) {
							
							// Keep getting a new enchantment from the list until we find one that can enchant the specified item
							while(true) {
								enchantment = Enchantment.getByName(this.enchantmentlists.get(itemtype).next().toUpperCase());
								if(enchantment.canEnchantItem(item))
									break;
							}
						}
					}
					
					// If we have an enchantment, enchant the item
					if(enchantment != null) {
						
						// Generate a random level for the enchantment based on the items maxlevel + 1
						int level = this.generator.nextInt(enchantment.getMaxLevel() + 1 );
						
						// If the level is above the maxlevel, decrease the value by one
						if(level > enchantment.getMaxLevel())
							level--;
						// If the level is beneath or equal to zero increase the level by one
						else if (level <= 0)
							level++;
					
						item.addEnchantment(enchantment, level);
					}
					
					// Place the item in a random slot of the inventory, get a new slot if the previous one where occupied
					int place;
					while(true) {
						place = this.generator.nextInt(inventory.getSize());
						if(inventory.getItem(place) == null)
							break;
					}
					
					inventory.setItem(place, item);
				}
				
				chest.update();
			}
			else {
				logger.debug("Spawnchance less or equal to random number, skipping randomization...");
			}
			
			this.randomizedchests.add(chest.getLocation());
		}
	}
	
	public void addChestToLog(Location location) {
		if(!this.randomizedchests.contains(location))
			this.randomizedchests.add(location);
	}
	
	public void clearLogs(String worldname) {
		logger.debug("Clearing logs for world - " + worldname);
		
		Iterator<Location> locations = this.randomizedchests.iterator();
		while(locations.hasNext()) {
			
			Location location = locations.next();
			
			if(location.getWorld().getName().equals(worldname))
				locations.remove();
		}
		
		logger.debug("finished...");
	}
	
    private void loadItemList() {
		
		this.itemConfig = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "itemlist.yml"));
		
		logger.debug("Loading configuration file.");
		
		// Check the default settings
		this.checkDefaults();
		
		
		
		// --- Items
		this.itemlist = new RandomCollection<String>();
		this.enchable = new HashMap<String, String>();
		
		for(String string : itemConfig.getConfigurationSection("weapons").getKeys(false)) {
			this.itemlist.add(itemConfig.getDouble("weapons." + string + ".spawnchance"), string);
			
			if(string.equals("bow"))
				this.enchable.put(string, "bow");
			else
				this.enchable.put(string, "swords");
		}
		
		for(String string : itemConfig.getConfigurationSection("armors").getKeys(false)) {
			this.itemlist.add(itemConfig.getDouble("armors." + string + ".spawnchance"), string);
			this.enchable.put(string, "armors");
		}
		
		for(String string : itemConfig.getConfigurationSection("food").getKeys(false)) {
			this.itemlist.add(itemConfig.getDouble("food." + string), string);
		}
		
		for(String string : itemConfig.getConfigurationSection("items").getKeys(false)) {
			this.itemlist.add(itemConfig.getDouble("items." + string), string);
		}
		
		
		
		// --- Enchantments
		this.enchantmentlists = new HashMap<String, RandomCollection<String>>();
		
		RandomCollection<String> swords = new RandomCollection<String>();
		RandomCollection<String> bows   = new RandomCollection<String>();
		RandomCollection<String> armors = new RandomCollection<String>();
		
		for(String swordench : itemConfig.getConfigurationSection("enchantments.swords").getKeys(false)) {
			swords.add(itemConfig.getDouble("enchantments.swords." + swordench), swordench);
		}
		
		for(String bowench : itemConfig.getConfigurationSection("enchantments.bow").getKeys(false)) {
			bows.add(itemConfig.getDouble("enchantments.bow." + bowench), bowench);
		}
		
		for(String armorench : itemConfig.getConfigurationSection("enchantments.armors").getKeys(false)) {
			armors.add(itemConfig.getDouble("enchantments.armors." + armorench), armorench);
		}
		
		this.enchantmentlists.put("swords", swords);
		this.enchantmentlists.put("bow",    bows);
		this.enchantmentlists.put("armors", armors);
		
		
		
		logger.debug("Finished loading config");
	}

	private void checkDefaults() {
		boolean save = false;
		
		if(!itemConfig.contains("maxAmountOfItems")) {
			
			itemConfig.set("maxAmountOfItems", 3);
			
			save = true;
		}
		
		if(!itemConfig.contains("blankChestChance-OneOutOf")) {
			
			itemConfig.set("blankChestChance-OneOutOf", 5);
			
			save = true;
		}
		
		if(!itemConfig.contains("weapons")) {
			
			itemConfig.set("weapons.wood_sword.enchantmentchance", 0.3);
			itemConfig.set("weapons.wood_sword.spawnchance", 0.8);
			
			itemConfig.set("weapons.bow.enchantmentchance", 0.1);
			itemConfig.set("weapons.bow.spawnchance", 0.4);
			
			save = true;
		}
		
		if(!itemConfig.contains("armors")) {
			
			itemConfig.set("armors.diamond_helmet.enchantmentchance", 0.01);
			itemConfig.set("armors.diamond_helmet.spawnchance", 0.1);
			
			itemConfig.set("armors.diamond_chestplate.enchantmentchance", 0.01);
			itemConfig.set("armors.diamond_chestplate.spawnchance", 0.1);
			
			itemConfig.set("armors.leather_chestplate.enchantmentchance", 0.3);
			itemConfig.set("armors.leather_chestplate.spawnchance", 0.3);
			
			itemConfig.set("armors.iron_leggings.enchantmentchance", 0.2);
			itemConfig.set("armors.iron_leggings.spawnchance", 0.2);
			
			save = true;
		}
		
		if(!itemConfig.contains("enchantments")) {
			
			itemConfig.set("enchantments.swords.fire_aspect", 0.3);
			itemConfig.set("enchantments.swords.damage_all", 0.3);
			itemConfig.set("enchantments.swords.knockback", 0.3);
			itemConfig.set("enchantments.swords.damage_arthropods", 0.3);
			itemConfig.set("enchantments.swords.damage_undead", 0.3);
			
			itemConfig.set("enchantments.bow.arrow_damage", 0.3);
			itemConfig.set("enchantments.bow.arrow_fire", 0.3);
			itemConfig.set("enchantments.bow.arrow_infinite", 0.3);
			itemConfig.set("enchantments.bow.arrow_knockback", 0.3);
			
			itemConfig.set("enchantments.armors.protection_fall", 0.2);
			itemConfig.set("enchantments.armors.protection_projectile", 0.2);
			itemConfig.set("enchantments.armors.protection_fire", 0.2);
			
			save = true;
		}
		
		if(!itemConfig.contains("food")) {
			
			itemConfig.set("food.apple", 0.8);
			itemConfig.set("food.cooked_beef", 0.5);
			
			save = true;
		}
		
		if(!itemConfig.contains("items")) {
			
			itemConfig.set("items.flint_and_steel", 0.2);
			itemConfig.set("items.arrow", 0.4);
			
			save = true;
		}
		
		if(save) {
			try {
	            itemConfig.save(this.plugin.getDataFolder() + File.separator + "itemlist.yml");
	            // Some sort of bug makes it impossible to use the config.getList(), reloading it after saving seems to fix it.
	            this.itemConfig = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "itemlist.yml"));
	        } catch (IOException e) {
	        	this.logger.severe("Could not save the itemlist!");
	        }
		}
	}
}

/**
 * Below this line is not my work
 */

class RandomCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    public RandomCollection() {
        this(new Random());
    }

    public RandomCollection(Random random) {
        this.random = random;
    }

    public void add(double weight, E result) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, result);
    }

    public E next() {
        double value = random.nextDouble() * total;
        return map.ceilingEntry(value).getValue();
    }
}
