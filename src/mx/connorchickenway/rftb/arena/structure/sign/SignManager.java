package mx.connorchickenway.rftb.arena.structure.sign;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mx.connorchickenway.rftb.RFTB;
import mx.connorchickenway.rftb.arena.Arena;
import mx.connorchickenway.rftb.arena.ArenaManager;
import mx.connorchickenway.rftb.arena.structure.ArenaState;
import mx.connorchickenway.rftb.arena.structure.Lang;
import mx.connorchickenway.rftb.utils.ConnorUtils;
import mx.connorchickenway.rftb.utils.Loc;

public class SignManager implements Listener{

	private HashMap<Location, ArenaSign> signs = new HashMap<>();
	
	private RFTB plugin;
	private ArenaManager arenaManager;
	
	public SignManager(RFTB plugin) {
		this.plugin = plugin;
		this.arenaManager = plugin.getArenaManager();
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onChangeSign(SignChangeEvent event) {
		String[] lines = event.getLines();
		if(lines[0].equalsIgnoreCase("[RFTB]")) {
			
			Player player = event.getPlayer();
			Block block = event.getBlock();
			
			if(!player.hasPermission("rftb.admin")) {
				player.sendMessage("§cYou don't have permission to do that!"); block.breakNaturally();
				return;
			}
			if(lines[1].equals("")) {
				player.sendMessage("§eUsage: \n §7line 1 = [RFTB] \n §7line 2 = name of the arena"); block.breakNaturally();
				return;
			}
			Arena arena = arenaManager.getArena(lines[1]);
			if(arena == null) {
				player.sendMessage("§cThe arena " + lines[1] + " does not exist!"); block.breakNaturally();
				return;
			}
			if(this.existsSign(arena.getName())) {
				player.sendMessage("§cThe sign for this arena already exists!"); block.breakNaturally();
				return;
			}
			
			if(!plugin.getMainLobby().existsLocation()) {
				player.sendMessage("§cMainlobby location does not exist - set /rftb setup setmainlobby");
				return;
			}
			
			Bukkit.getScheduler().runTaskLater(plugin, () -> signs.put(block.getLocation(), new ArenaSign((Sign)block.getState(), arena)), 5);
			arena.getConfig().setObject("sign_location", ConnorUtils.getLocationString(block.getLocation() , false));
			arena.getConfig().saveConfig();
			player.sendMessage("The sign for the arena " + arena.getName() + " created successfully!");
		
		}
		 
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		BlockState state = event.getBlock().getState();
		if(!(state instanceof Sign)) {
			return;
		}
		ArenaSign as = this.getSign(state);
		if(as != null) {
			if(!player.hasPermission("rftb.admin")) {
				player.sendMessage("§cYou don't have permission to do that!");
				return;
			}
			signs.remove(state.getLocation());
			return;
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			BlockState state = event.getClickedBlock().getState();
			if(!(state instanceof Sign)) {
				return;
			}
			ArenaSign as = this.getSign(state);
			if(as != null) {
				Arena arena = as.getArena();
				if(arena.hasArena(event.getPlayer())) {
					return;
				}
				if(arena.isState(ArenaState.GAME) || arena.isState(ArenaState.FINISH)) {
					event.getPlayer().sendMessage(arena.isState(ArenaState.GAME) ? Lang.ARENA_IN_GAME.toString() : Lang.ARENA_FINISH.toString());
					return;
				}
				arena.join(event.getPlayer());
			}
		}
	}
	
	public void loadSigns() {
		for(Arena arena : plugin.getArenaManager().getArenasList()) {
			if(arena.getConfig().existsPath("sign_location")) {
				Loc loc = ConnorUtils.getStringFromLoc(arena, "sign_location");
				if(loc.existsLocation()) {
					Location location = loc.toLocation();
					BlockState state = location.getBlock().getState();
					if(state instanceof Sign) {
						signs.put(location, new ArenaSign((Sign)state, arena));
						continue;
					}
				}
			}
		}
	}
	
	public ArenaSign getSign(BlockState state) {
		return signs.get(state.getLocation());
	}
	
	public ArenaSign getSign(Arena arena) {
		for(ArenaSign as : signs.values()) {
			if(as.getArena().equals(arena)) {
				return as;
			}
		}
		return null;
	}
	
	public ArenaSign getSign(String name) {
		for(ArenaSign as : signs.values()) {
			if(as.getArena().getName().equalsIgnoreCase(name)) {
				return as;
			}
		}
		return null;
	}
	
	public boolean existsSign(String name) {
		for(ArenaSign as : signs.values()) {
			if(as.getArena().getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	public void removeSign(String name) {
		ArenaSign as = this.getSign(name);
		if(as != null) {
			signs.remove(as.getLocation());
		}
	}
	
}
