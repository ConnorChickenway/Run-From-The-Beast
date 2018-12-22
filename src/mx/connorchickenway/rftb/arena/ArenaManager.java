package mx.connorchickenway.rftb.arena;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import mx.connorchickenway.rftb.RFTB;
import mx.connorchickenway.rftb.arena.builder.ArenaBuilder;
import mx.connorchickenway.rftb.utils.ConnorUtils;


public class ArenaManager {

	private RFTB plugin;
	
	private HashMap<String, Arena> arenas;
	private HashMap<Player, Arena> playerArena;
	private List<ArenaBuilder> builders;
	
	public ArenaManager() {
		this.arenas = new HashMap<>();
		this.playerArena = new HashMap<>();
		this.builders = new ArrayList<>();
		this.plugin = RFTB.getInstance();
	}
	
	public void addArena(Arena arena) {
		String name = arena.getName().toLowerCase();
		if(!arenas.containsKey(name)) {
			arenas.put(name, arena);
		}
	}
	
	public void removeArena(String name) {
		String str = name.toLowerCase();
		if(arenas.containsKey(str)) {
			plugin.getSignManager().removeSign(str);
			arenas.remove(str);
		}
	}
	
	public Arena getArena(String name) {
		return arenas.get(name.toLowerCase());
	}
	
	public Arena getArena(Player player) {
		return playerArena.get(player);
	}
	
	public boolean isInGame(Player player) {
		return playerArena.containsKey(player);
	}
	
	public boolean existsArena(String name) {
		return arenas.containsKey(name.toLowerCase());
	}
	
	public void addPlayer(Player player, Arena arena) {
		if(!playerArena.containsKey(player)) {
			playerArena.put(player, arena);
			arena.getPlayers().add(player);
		}
	}
	
	public void removePlayer(Player player) {
		if(playerArena.containsKey(player)) {
			this.getArena(player).getPlayers().remove(player);
			playerArena.remove(player);
		}
	}
	
	public ArenaBuilder getArenaBuilder(String name) {
		for(ArenaBuilder builder : builders) {
			if(builder.getName().equalsIgnoreCase(name)) {
				return builder;
			}
		}
		return null;
	}
	
	public boolean existsArenaBuilder(String name) {
		for(ArenaBuilder builder : builders) {
			if(builder.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	public void addArenaBuilder(String name) {
		builders.add(new ArenaBuilder(name));
	}
	
	public void removeArenaBuilder(ArenaBuilder builder){
		builders.remove(builder);
	}
	
	public Collection<Arena> getArenasList(){
		return arenas.values();
	}
	
	public void loadArenas() {
		File[] files = new File(plugin.getDataFolder().getAbsolutePath() + "/arenas").listFiles();
		if(files != null) {
			for(File file : files) {
				if(file.isDirectory()) {
					continue;
				}
				if(file.isFile()) {
					String[] split = file.getName().split("[.]");
					if(split[1].equals("yml")) {
						Arena arena = this.getArena(split[0]);
						if(arena != null) {
							continue;
						}
						YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
						ArenaBuilder builder = new ArenaBuilder(split[0]);
						builder.setMinPlayers(config.getInt("minPlayers"));
						builder.setMaxPlayers(config.getInt("maxPlayers"));
						builder.setLobbyLocation(ConnorUtils.getStringFromLoc("locations.lobby", config));
						builder.setBeastLocation(ConnorUtils.getStringFromLoc("locations.beast", config));
						builder.setRunnersLocation(ConnorUtils.getStringFromLoc("locations.runners", config));
						builder.createArena(false);
						continue;
					}
				}
			}
		}
		int i = 0;
		StringBuilder builder = new StringBuilder();
		for(Arena arena : this.getArenasList()) {
			i++;
			builder.append(arena.getName() + (i < this.getArenasList().size() ? ";" : ""));
		}
		ConnorUtils.logMessage("[Arenas] " + builder.toString());
	}
	
}
