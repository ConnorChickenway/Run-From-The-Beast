package mx.connorchickenway.rftb.utils;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mx.connorchickenway.rftb.RFTB;

public class RespawnNMS {

	private final String minecraft_version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	
	private boolean is1_8_R1 = false;
	private Class<?> packet;
	
	public RespawnNMS() {
		this.packet = this.getClass("PacketPlayInClientCommand");
		if(minecraft_version.equals("v1_8_R1")) {
			this.is1_8_R1 = true;
		}
	}
	
	public void sendRespawn(Player player) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				preparePacket(player);
			}
		}.runTaskLater(RFTB.getInstance(), 2L);
	}
	
	private void preparePacket(Player player) {
		if(this.packet != null) {
			Class<?> perform = this.is1_8_R1 ? getClass("EnumClientCommand") : packet.getDeclaredClasses()[0];
			Constructor<?> constructor = null;
			Object packet = null;
			try {
				constructor = this.packet.getConstructor(perform);
				packet = constructor.newInstance(perform.getField("PERFORM_RESPAWN").get(null));
			}catch (Exception e) {
				e.printStackTrace();
			}
			if(packet != null) {
				sendPacket(player, packet);
			}
		}
	}
	
	private void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("a", this.packet).invoke(playerConnection, packet);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
	
	private Class<?> getClass(String name) {
		try {
			return Class.forName("net.minecraft.server." + minecraft_version + "." + name);
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
