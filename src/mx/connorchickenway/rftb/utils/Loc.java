package mx.connorchickenway.rftb.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Loc {
	
	private String world;
	
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	
	private String[] values;
	
	public Loc(Location location) {
		this(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}
	
	public Loc(String world, double x, double y, double z) {
		this(world, x, y, z, 0.0f, 0.0f);
	}
	
	public Loc(String world, double x, double y, double z, float yaw, float pitch) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.values = new String[] {x + "", y + "", z + "", yaw + "", pitch + "", world};
	}

	public boolean teleport(Player player, TeleportCause cause) {
		Location location = this.toLocation();
		if(location == null) {
			player.sendMessage(this.getIssue());
			return false;
		}		
		return player.teleport(location, cause);
	}
	
	public boolean existsLocation() {
		return (this.toLocation() != null);
	}
	
	public Location toLocation() {
		World world = Bukkit.getWorld(this.world);
		if(world == null) {
			return null;
		}
		return new Location(world, x, y, z, yaw, pitch);
	}
	
	public String serializableLoc(boolean pitchYaw) {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < values.length; i++) {
			if(i == 3 || i == 4) {
				if(pitchYaw) {
					str.append(values[i] + ";");
				}
				continue;
			}
			str.append(values[i] + (i == values.length ? "" : ";"));
		}
		return str.toString();
	}
	
	public String getIssue() {
		return "§cThe world " + world + " to which you were to be teleported has not been charged - Report to staff this issue";
	}
	
	public String getNameWorld() {
		return world;
	}
	
	public World getWorld() {
		return Bukkit.getWorld(world);
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public float getYaw() {
		return yaw;
	}
	
	public float getPitch() {
		return pitch;
	}
	
}
