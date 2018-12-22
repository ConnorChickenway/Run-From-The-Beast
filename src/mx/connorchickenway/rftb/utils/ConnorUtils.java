package mx.connorchickenway.rftb.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import mx.connorchickenway.rftb.arena.Arena;

public class ConnorUtils {
	
	/**#PLAYER_LOCATION#**/
	public static String getLocationString(Loc loc, boolean pitchYaw) {
		return loc.serializableLoc(pitchYaw);
	}
	public static String getLocationString(Location location, boolean pitchYaw) {
		return getLocationString(new Loc(location), pitchYaw);
	}

	public static Loc getStringFromLoc(Arena arena, String path) {
		if(arena == null) {
			return null;
		}
		return getStringFromLoc(path, arena.getConfig().getFileConfiguration());
	}
	
	public static Loc getStringFromLoc(String path, FileConfiguration config) {
		String[] split = config.getString(path).split(";");
			
		String world = split[split.length-1];	
		double x = Double.parseDouble(split[0]);
		double y = Double.parseDouble(split[1]);
		double z = Double.parseDouble(split[2]);
		float yaw = 0.0F;
		float pitch = 0.0F;
		try {yaw = Float.parseFloat(split[3]);pitch = Float.parseFloat(split[4]);}catch (NumberFormatException e) {}
		
		return new Loc(world, x, y, z, yaw, pitch);
	}
	
	public static boolean inventoryEmpty(ItemStack[] contents) {
		for(int i = 0; i < contents.length; i++) {
			if(contents[i] != null) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean armorEmpty(ItemStack[] armor) {
		for(int i = 0; i < armor.length; i++) {
			if(armor[i].getType() != Material.AIR) {
				return false;
			}
		}
		return true;
	}
	
	public static void logMessage(String string) {
		System.out.println("[RFTB] " + string);
	}
}
