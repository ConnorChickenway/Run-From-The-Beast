package mx.connorchickenway.rftb.arena.structure;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import mx.connorchickenway.rftb.RFTB;
import mx.connorchickenway.rftb.utils.ConnorUtils;
import mx.connorchickenway.rftb.utils.ItemBuilder;

public class KitBeast {

	private HashMap<Integer, ItemStack> contents;
	private ItemStack[] armor = new ItemStack[4];
	
	private KitBeast() {
		this.contents = new HashMap<>();
		this.armor = new ItemStack[4];
	}
	
	public void addArmor(int index, ItemStack itemStack) {
		this.armor[index] = itemStack;
	}
	
	public void add(int index, ItemStack itemStack) {
		contents.put(index, itemStack);
	}
	
	public void sendKitBeast(Player player) {
		PlayerInventory inventory = player.getInventory();
		if(contents.size() > 0) {
			inventory.clear();
			for(Entry<Integer, ItemStack> item : contents.entrySet()) {
				inventory.setItem(item.getKey(), item.getValue());
			}
		}
		inventory.setArmorContents(armor);
	}
		
	public static void serializableKit(Player player) throws IOException {
		File file = new File(RFTB.getInstance().getDataFolder() + "/kit_beast.json");
		
		if(!file.exists()) {
			file.createNewFile();
		}
		
		PlayerInventory pInventory = player.getInventory();
		ItemStack[] contents = pInventory.getContents() , armor = pInventory.getArmorContents(); 
		
		if(ConnorUtils.inventoryEmpty(contents) && ConnorUtils.armorEmpty(armor)) {
			player.sendMessage("§cYour inventory is empty!");
			return;
		}
		
		JsonArray array = new JsonArray();
		
		for(int i = 0; i < contents.length; i++) {
			ItemStack itemStack = contents[i];
			if(itemStack != null) {
				array.add(new ItemToJSON(itemStack, i).generateItemJSON());
			}
		}
	
		JsonObject object = new JsonObject();
		object.add("contents", array);
		
		array = new JsonArray();
		
		for(int i = 0; i < armor.length; i++) {
			array.add(new ItemToJSON(armor[i]).generateArmorJSON());
		}
		
		object.add("armor", array);
		
		try(FileWriter writer = new FileWriter(file)){
			writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(object.toString())));
			writer.flush();
			writer.close();
			player.sendMessage("§aKit beast saved successfully!");
		}catch (Exception e) {
			ConnorUtils.logMessage("Error when saving kit in " + file.getAbsolutePath());
		}
		
	}
	
	public static KitBeast deserializableKit() throws IOException {
		File file = new File(RFTB.getInstance().getDataFolder() + "/kit_beast.json");
		
		if(!file.exists()) {
			return null;
		}
		
		KitBeast kit = new KitBeast();
		
		JsonObject object = new JsonParser().parse(new FileReader(file)).getAsJsonObject();
		JsonArray array = object.get("contents").getAsJsonArray();
		
		for(int i = 0; i < array.size(); i++) {
			JsonObject obj = array.get(i).getAsJsonObject();
			ItemBuilder builder = new ItemBuilder(obj.get("material").getAsString(), obj.get("amount").getAsInt(), obj.get("durability").getAsByte());
			String name = obj.get("name").getAsString();
			if(!name.equals("null")) {
				builder.setName(name);
			}
			JsonArray enchants = obj.get("enchants").getAsJsonArray();
			if(enchants.size() > 0) {
				for(int n = 0; n < enchants.size(); n++) {
					String[] str = enchants.get(n).getAsString().split(":");
					builder.addEnchant(Enchantment.getByName(str[0]), Integer.valueOf(str[1]));
				}
			}
			kit.add(obj.get("index").getAsInt(), builder.toItemStack());
		}
		
		array = object.get("armor").getAsJsonArray();
		
		for(int i = 0; i < array.size(); i++) {
			JsonObject obj = array.get(i).getAsJsonObject();
			String type = obj.get("type").getAsString();
			if(type.equalsIgnoreCase("AIR")) {
				kit.addArmor(i, new ItemStack(Material.AIR));
				continue;
			}
			ItemBuilder builder = new ItemBuilder(Material.getMaterial(type));
			JsonArray enchants = obj.get("enchants").getAsJsonArray();
			if(enchants.size() > 0) {
				for(int e = 0; e < enchants.size(); e++) {
					String[] str = enchants.get(e).getAsString().split(":");
					builder.addEnchant(Enchantment.getByName(str[0]), Integer.valueOf(str[1]));
				}
			}
			kit.addArmor(i, builder.toItemStack());
		}
		
		return kit;
	}
	
}

class ItemToJSON {
	
	private ItemStack item;
	private int index;
	
	public ItemToJSON(ItemStack stack, int index) {
		this.item = stack;
		this.index = index;
	}
	
	public ItemToJSON(ItemStack stack) {
		this.item = stack;
	}
	
	public String getName() {
		return item.getItemMeta().getDisplayName();
	}
	
	public Material getMaterial() {
		return item.getType();
	}
	
	public int getAmount() {
		return item.getAmount();
	}
	
	public short getDurability() {
		return item.getDurability();
	}
	
	public Map<Enchantment, Integer> getAllEnchantments() {
		return item.getEnchantments();
	}
	
	public JsonObject generateArmorJSON() {
		JsonObject object = new JsonObject();
		object.addProperty("type", this.getMaterial().toString());
		JsonArray array = new JsonArray();
		this.getAllEnchantments().forEach((key, value) -> array.add(new JsonPrimitive(key.getName() + ":" + value)));
		object.add("enchants", array);
		return object;
	}
	
	public JsonObject generateItemJSON() {
		JsonObject object = new JsonObject();
		object.addProperty("name", this.getName() != null ? this.getName() : "null");
		object.addProperty("material", this.getMaterial().toString());
		object.addProperty("amount", this.getAmount());
		object.addProperty("durability", this.getDurability());
		object.addProperty("index", this.index);
		JsonArray array = new JsonArray();
		this.getAllEnchantments().forEach((key, value) -> array.add(new JsonPrimitive(key.getName() + ":" + value)));
		object.add("enchants", array);
		return object;
	}
	
}
