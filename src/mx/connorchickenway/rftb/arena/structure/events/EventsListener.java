package mx.connorchickenway.rftb.arena.structure.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mx.connorchickenway.rftb.RFTB;
import mx.connorchickenway.rftb.arena.Arena;
import mx.connorchickenway.rftb.arena.ArenaManager;
import mx.connorchickenway.rftb.arena.structure.ArenaState;
import mx.connorchickenway.rftb.arena.structure.Lang;
import mx.connorchickenway.rftb.arena.structure.sign.ArenaSign;

public class EventsListener implements Listener{
	
	private RFTB plugin;
	private ArenaManager arenaManager;
	
	public EventsListener(RFTB plugin) {
		this.plugin = plugin;
		this.arenaManager = plugin.getArenaManager();
	}
	
	@EventHandler
	public void onSpawn(CreatureSpawnEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Arena arena = arenaManager.getArena(player);
		if(arena != null) {
			String str = "";
			Player killer = player.getKiller();
			if(killer instanceof Player) {
				boolean beast = true;
				if(!killer.equals(arena.getBeast())) {
					arena.setBeast(null);
					beast = false;
				}
				str = beast ? Lang.BEAST_KILL.toString() : Lang.RUNNERS_KILL.toString();
			} else {
				str = player.equals(arena.getBeast()) ? Lang.BEAST_DEATH.toString() : Lang.RUNNER_DEATH.toString(); 
			}
			arena.broadcast(str.replace("%player%", player.getName()));
			arena.leave(player);
			plugin.getNMS().sendRespawn(player);
		}
		event.setDeathMessage(null);
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(plugin.getMainLobby().toLocation());
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			ItemStack item = event.getItem();
			if(item != null) {
				if(item.getType() != Material.COMPASS) {
					return;
				}
				Player player = event.getPlayer();
				Arena arena = arenaManager.getArena(player);
				if(arena != null) {
					if(!arena.isBeast(player)) {
						return;
					}
					if(!arena.isState(ArenaState.GAME)) {
						return;
					}
					arena.distanceRunners(true);
				}
			}
		}
	}
		
	@EventHandler
	public void onFood(FoodLevelChangeEvent event) {
		event.setFoodLevel(20);
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		if(this.hasTrue("no_drop")) {
			Player player = event.getPlayer();
			if(arenaManager.isInGame(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInventory(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			BlockState state = event.getClickedBlock().getState();
			if(!(state instanceof Chest)) {
				return;
			}
			Arena arena = arenaManager.getArena(player);
			if(arena != null) {
				if(this.hasTrue("new_inventory_chest")) {
					event.setCancelled(true);
					Inventory chestInventory = ((Chest) state).getInventory();
					Inventory newInventory = Bukkit.createInventory(null, chestInventory.getSize());
					newInventory.setContents(chestInventory.getContents());
					player.openInventory(newInventory);
				}
			}
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if(arenaManager.isInGame(player)) {
			event.setCancelled(true);
			return;
		}
		if(!player.hasPermission("rftb.admin")) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if(arenaManager.isInGame(player)) {
			event.setCancelled(true);
			return;
		}
		if(!player.hasPermission("rftb.admin")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity(), damager = event.getDamager();
		if(!(entity instanceof Player && damager instanceof Player)) {
			return;
		}
		
		Arena arena = arenaManager.getArena((Player)entity);
		if(arena != null) {
			if(!arena.isState(ArenaState.GAME)) {
				event.setCancelled(true);
				return;
			}
			if(arena.hasArena((Player)entity) && arena.isBeast((Player)damager) || entity.equals(arena.getBeast())) {
				return;
			}
			event.setCancelled(true);
			return;
		} 
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if(!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		if(!arenaManager.isInGame(player)) {
			event.setCancelled(true);
			return;
		}
		Arena arena = arenaManager.getArena(player);
		if(arena != null) {
			DamageCause cause = event.getCause();
			if(arena.isInvencible()) {
				if(cause == DamageCause.VOID) {
					arena.teleport(player, (arena.isState(ArenaState.FINISH) ? arena.getRunnersLocation() : arena.getLobbyLocation()));
				}
				event.setCancelled(true);
				return;
			}
		
			if(cause == DamageCause.VOID) {
				if(arena.isBeast(player)) {
					arena.teleport(player, arena.getRunnersLocation());
					event.setCancelled(true);
					return;
				}
			}
			
			if(cause == DamageCause.FALL) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Arena arena = arenaManager.getArena(player);
		if(arena != null) {
			arena.leave(player);
			this.getSign(arena).updateAll();
		}
	}

	public ArenaSign getSign(Arena arena) {
		return plugin.getSignManager().getSign(arena);
	}
	
	private boolean hasTrue(String path) {
		return plugin.getConfig().getBoolean(path, true);
	}
	
}
