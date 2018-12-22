package mx.connorchickenway.rftb.arena.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import mx.connorchickenway.rftb.RFTB;
import mx.connorchickenway.rftb.utils.Config;
import mx.connorchickenway.rftb.utils.Loc;

public class Structure {

	protected static RFTB plugin = RFTB.getInstance();
	
	private String arenaName;
	private int[] minMax;
	protected int count, liberateBeast, timerFinish;
	
	protected List<Player> players;
	protected Player beast;
	private Loc[] locs;
	
	private Config config;
	protected ArenaState state;
	
	public Structure(String arenaName) {
		this.arenaName = arenaName;
		this.locs = new Loc[3];
		this.minMax = new int[2];
		this.players = new ArrayList<>();
		this.state = ArenaState.LOBBY;
		this.config = new Config(plugin.getDataFolder().getAbsolutePath()+"/arenas/", arenaName);
		this.count = plugin.getConfig().getInt("start_game" , 30);
		this.liberateBeast = plugin.getConfig().getInt("liberate_beast", 10);
		this.timerFinish = plugin.getConfig().getInt("time_game" , 300);
	}
	
	public void teleport(Player player, Loc loc) {
		loc.teleport(player, TeleportCause.COMMAND);
	}
	
	public void broadcast(String message) {
		players.forEach(player -> player.sendMessage(message));
	}
	
	
	public boolean isState(ArenaState state) {
		return this.state == state;
	}
	
	public boolean isBeast(Player player) {
		return (beast != null && beast.equals(player));
	}
	
	public boolean hasArena(Player player) {
		return players.contains(player);
	}
	
	public Player getBeast() {
		return beast;
	}
	
	public List<Player> getRunners(){
		return players.stream().filter(player ->  beast == null || beast != null && !beast.equals(player)).collect(Collectors.toList());
	}
	
	public String getName() {
		return arenaName;
	}

	
	public int getCount() {
		return count;
	}
	
	public int getMinPlayers() {
		return minMax[0];
	}
	
	public int getMaxPlayers() {
		return minMax[1];
	}
	
	public int getSize() {
		return players.size();
	}
	
	public Loc getLobbyLocation() {
		return this.locs[0];
	}
	
	public Loc getBeastLocation() {
		return this.locs[1];
	}
	
	public Loc getRunnersLocation() {
		return this.locs[2];
	}
		
	public Config getConfig() {
		return config;
	}
	
	public ArenaState getState() {
		return state;
	}
	
	public List<Player> getPlayers(){
		return players;
	}
	
	public void setState(ArenaState state) {
		this.state = state;
	}
	
	public void setMinPlayers(int minPlayers) {
		this.minMax[0] = minPlayers;
	}
	
	public void setMaxPlayers(int maxPlayers){
		this.minMax[1] = maxPlayers;
	}
	
	public void setLobbyLocation(Loc lobby) {
		this.locs[0] = lobby;
	}
	
	public void setBeastLocation(Loc beast) {
		this.locs[1] = beast;
	}
	
	public void setRunnersLocation(Loc runners) {
		this.locs[2] = runners;
	}
	
	public void setBeast(Player player) {
		this.beast = player;
	}
	
	public void setName(String name) {
		this.arenaName = name;
	}
	
	
	
}
