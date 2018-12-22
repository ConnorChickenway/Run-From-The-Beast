package mx.connorchickenway.rftb;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import mx.connorchickenway.rftb.arena.ArenaManager;
import mx.connorchickenway.rftb.arena.structure.KitBeast;
import mx.connorchickenway.rftb.arena.structure.events.EventsListener;
import mx.connorchickenway.rftb.arena.structure.sign.SignManager;
import mx.connorchickenway.rftb.commands.CommandsManager;
import mx.connorchickenway.rftb.utils.Config;
import mx.connorchickenway.rftb.utils.ConnorUtils;
import mx.connorchickenway.rftb.utils.ItemBuilder;
import mx.connorchickenway.rftb.utils.Loc;
import mx.connorchickenway.rftb.utils.RespawnNMS;

public class RFTB extends JavaPlugin implements Listener {
	
	private ArenaManager arenaManager;
	private SignManager signManager;
	private Loc mainLobby;
	private Config lang;
	private ItemBuilder returnLobby, playerTracker;
	private KitBeast kitBeast;
	private RespawnNMS respawn;
	
	@Override
	public void onEnable() {
		instance = this;
		this.saveDefaultConfig();
		this.respawn = new RespawnNMS();
		this.arenaManager = new ArenaManager();
		this.signManager = new SignManager(this);
		this.loadArenasAndSigns();
		try { this.mainLobby = ConnorUtils.getStringFromLoc("spawn", getConfig());} catch(NullPointerException e) {}
		try { this.kitBeast = KitBeast.deserializableKit(); } catch(Exception e) { }
		this.lang = new Config(this.getDataFolder().getAbsolutePath(), "lang.yml");
		this.getCommand("rftb").setExecutor(new CommandsManager(this));
		this.registerEvents();
		this.registerItemReturn();
		this.registerItemTracker();
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);
		Player player = event.getPlayer();
		if(mainLobby != null) {
			if(mainLobby.existsLocation()) {
				mainLobby.teleport(player, TeleportCause.COMMAND);
				return;
			}
			player.sendMessage(mainLobby.getIssue());
		}else 
		if(player.hasPermission("rftb.admin")) {
			player.sendMessage("§cMain lobby not exits! - /rftb setup setmainlobby");
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack itemStack = event.getItem();
			if(itemStack != null) {
				if(itemStack.equals(returnLobby.toItemStack())) {
					Bukkit.dispatchCommand(event.getPlayer(), "rftb leave");
				}
			}
		}
	}
	
	@EventHandler
	public void onClickInventory(InventoryClickEvent event) {
		Inventory inventory = event.getClickedInventory();
		if(inventory == null) {
			return;
		}
		if(inventory.getType() == InventoryType.PLAYER) {
			ItemStack itemStack = event.getCurrentItem();
			if(itemStack != null) {
				if(itemStack.equals(returnLobby.toItemStack())) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	private void loadArenasAndSigns() {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				arenaManager.loadArenas();
				signManager.loadSigns();
				ConnorUtils.logMessage("Plugin created by ConnorChickenway!");
			}
		}.runTaskLater(this, 1);
	}
	
	private void registerItemReturn() {
		String path = "items.return_lobby.";
		returnLobby = new ItemBuilder(this.getConfig().getString(path + "material"), 1, Byte.valueOf(this.getConfig().getString(path + "durability")));
		String name = this.getConfig().getString(path + "name");
		if(name != null) {
			returnLobby.setName(name);
		}
	}
	
	private void registerItemTracker() {
		String path = "items.compass_target";
		playerTracker = new ItemBuilder(Material.COMPASS);
		String name = this.getConfig().getString(path + "name" , "&a&lPlayer Tracker &7(Right Click)");
		if(name != null) {
			playerTracker.setName(name);
		}
	}
	
	private void registerEvents() {
		this.getServer().getPluginManager().registerEvents(new EventsListener(this), this);
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void sendItemReturn(Player player) {
		player.getInventory().setItem(this.getConfig().getInt("items.return_lobby.position" , 8), returnLobby.toItemStack());
	}
	
	public void sendItemTracker(Player player) {
		player.getInventory().setItem(this.getConfig().getInt("items.compass.target" , 4), playerTracker.toItemStack());
	}
	
	public boolean existsKit() {
		return kitBeast != null;
	}
	
	public ArenaManager getArenaManager() {
		return arenaManager;
	}
	
	public SignManager getSignManager() {
		return signManager;
	}
	
	public Loc getMainLobby() {
		return mainLobby;
	}
	
	public Config getLang() {
		return lang;
	}
	
	public KitBeast getKitBeast() {
		return kitBeast;
	}
	
	public RespawnNMS getNMS() {
		return respawn;
	}
	
	public void setMainLobby(Loc location) {
		this.mainLobby = location;
	}
	
	
	/*INSTANCE*/
	private static RFTB instance;
	
	public static RFTB getInstance() {
		return instance;
	}
	
}
