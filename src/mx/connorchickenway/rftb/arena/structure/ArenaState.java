package mx.connorchickenway.rftb.arena.structure;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import mx.connorchickenway.rftb.RFTB;

public enum ArenaState {
	
	LOBBY("lobby"), PRE_GAME("pre-game"), GAME("game") , FINISH("finish");
	
	private String path;
	private FileConfiguration config;
	
	private ArenaState(String path) {
		this.path = path;
		this.config = RFTB.getInstance().getConfig();
	}
	
	public String[] getAttached() {
		return config.getString("sign.attached."+path).split(":");
	}
	
	public String getStatus() {
		return ChatColor.translateAlternateColorCodes('&', config.getString("sign.status."+path));
	}
	
	public String getPath() {
		return path;
	}
	
}
