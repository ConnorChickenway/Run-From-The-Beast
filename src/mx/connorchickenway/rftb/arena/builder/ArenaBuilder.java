package mx.connorchickenway.rftb.arena.builder;

import mx.connorchickenway.rftb.arena.Arena;
import mx.connorchickenway.rftb.utils.Config;
import mx.connorchickenway.rftb.utils.ConnorUtils;
import mx.connorchickenway.rftb.utils.Loc;

public class ArenaBuilder {

	private String name;
	private Loc lobby, runners, beast;
	private int minPlayers, maxPlayers;
	
	public ArenaBuilder(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}
	
	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}
	
	public void setLobbyLocation(Loc lobby) {
		this.lobby = lobby;
	}
	
	public void setRunnersLocation(Loc runners) {
		this.runners = runners;
	}
	
	public void setBeastLocation(Loc beast) {
		this.beast = beast;
	}
	
	public int getMinPlayers() {
		return minPlayers;
	}
	
	public int getMaxPlayers() {
		return maxPlayers;
	}
	
	public Loc getLobby() {
		return lobby;
	}
	
	public Loc getRunners() {
		return runners;
	}
	
	public Loc getBeast() {
		return beast;
	}
	
	public boolean isNoNull() {
		return name != null && minPlayers > 0 && maxPlayers > minPlayers && lobby != null && beast != null && runners != null;
	}
	
	private void saveConfig(Arena arena) {
		Config config = arena.getConfig();
		config.setObject("maxPlayers", maxPlayers);
		config.setObject("minPlayers", minPlayers);
		config.setObject("locations.lobby", ConnorUtils. getLocationString(lobby, true));
		config.setObject("locations.beast", ConnorUtils. getLocationString(beast, true));
		config.setObject("locations.runners", ConnorUtils. getLocationString(runners, true));
		config.saveConfig();
	}
	
	public boolean createArena(boolean config) {
		if(isNoNull()) {
			Arena arena = new Arena(name);
			arena.setMinPlayers(minPlayers);
			arena.setMaxPlayers(maxPlayers);
			arena.setLobbyLocation(lobby);
			arena.setBeastLocation(beast);
			arena.setRunnersLocation(runners);
			if(config) {
				saveConfig(arena);
			}
			return true;
		}
		return false;
	}
	
}
