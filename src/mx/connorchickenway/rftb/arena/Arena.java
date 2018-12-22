package mx.connorchickenway.rftb.arena;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import mx.connorchickenway.rftb.RFTB;
import mx.connorchickenway.rftb.arena.structure.ArenaState;
import mx.connorchickenway.rftb.arena.structure.Lang;
import mx.connorchickenway.rftb.arena.structure.PlayerScoreboard;
import mx.connorchickenway.rftb.arena.structure.Structure;
import mx.connorchickenway.rftb.arena.structure.sign.ArenaSign;

public class Arena extends Structure {
	
	private static ArenaManager arenaManager = plugin.getArenaManager();

	private HashMap<Player, PlayerScoreboard> scoreboards;
	private boolean invencible;
	
	public Arena(String name) {
		super(name);
		arenaManager.addArena(this);
		this.scoreboards = new HashMap<>();
		this.invencible = true;
	}

	public void join(Player player) {
		if(this.getSize() >= this.getMaxPlayers() && !player.hasPermission("rftb.joinfull")) {
			broadcast(Lang.ARENA_FULL.toString());
			return;
		}
		arenaManager.addPlayer(player, this);
		this.scoreboards.put(player, new PlayerScoreboard(player, this));
		this.broadcast(Lang.JOIN_ARENA.toString().replace("%player%", player.getName()).replace("%online_players%", String.valueOf(players.size())).replace("%max_players%", String.valueOf(this.getMaxPlayers())));
		this.getSign().updateLines();
		if(this.getSize() == this.getMinPlayers()) {
			this.startingArena();
		}
		this.preparePlayer(player);
		this.teleport(player, this.getLobbyLocation());
	}
	
	public void leave(Player player) {
		this.teleport(player, RFTB.getInstance().getMainLobby());
		this.clearInventory(player);
		this.removeEffects(player);
		scoreboards.get(player).unregister();
		scoreboards.remove(player);
		arenaManager.removePlayer(player);
		this.getSign().updateLines();
		this.broadcast(Lang.LEAVE_ARENA.toString().replace("%player%", player.getName()).replace("%online_players%", String.valueOf(players.size())).replace("%max_players%", String.valueOf(this.getMaxPlayers())));
		if(this.isState(ArenaState.GAME)) {
			if(this.isBeast(player)) {
				this.setBeast(null);
			}
			checkArena();
		}
	}

	private void startingArena() {
		this.setState(ArenaState.PRE_GAME);
		this.getSign().updateAll();
		new BukkitRunnable() {
			boolean beast = false;
			@Override
			public void run() {
				if(beast) {
					if(liberateBeast == 0) {
						broadcast(Lang.HAS_LIBERATE.toString());
						teleport(getBeast(), getRunnersLocation());
						distanceRunners(false);
						this.cancel();
						return;
					}
					if(getBeast() == null) {
						cancel();
						return;
					}
					if(liberateBeast % 10 == 0 || liberateBeast <= 5){
						broadcast(Lang.LIBERATE_BEAST.toString().replace("%count%", String.valueOf(liberateBeast)).replace("%seconds%", (count > 0 ? getSeconds()[1] : getSeconds()[0])));
					}
					liberateBeast--;
					return;
				} 
				if(count == 0) {
					setState(ArenaState.GAME);
					getSign().updateAll();
					prepareBeast(players.get(new Random().nextInt(players.size())));
					getRunners().forEach(player -> {
						teleport(player, getRunnersLocation());
						player.getInventory().clear();
					});
					runnableScoreboards();
					invencible = false;
					beast = true;
					return;
				}
				if(getSize() < getMinPlayers()) {
					setState(ArenaState.LOBBY);
					getSign().updateAll();
					broadcast(Lang.NECESSARY_PLAYERS.toString());
					updateScoreboards();
					this.cancel();
					return;
				}
				if(count % 10 == 0 || count <= 5){
					broadcast(Lang.START_GAME.toString().replace("%count%", String.valueOf(count)).replace("%seconds%", (count > 0 ? getSeconds()[1] : getSeconds()[0])));
				}
				updateScoreboards();
				count--;
			}
		}.runTaskTimerAsynchronously(plugin, 20L, 20L);
	}
	
	private String[] getSeconds() {
		return Lang.SECONDS.toString().split(";");
	}
	
	private void updateScoreboards() {
		for(Player runners : this.getPlayers()) {
			PlayerScoreboard ps = this.scoreboards.get(runners);
			if(ps != null) {
				ps.updateSlots();
			}
		}
	}

	private void runnableScoreboards() {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if(isState(ArenaState.FINISH)){
					this.cancel();
					return;
				}
				if(timerFinish == 0) {
					setState(ArenaState.FINISH);
					drawnArena();
					return;
				}
				getRunners().forEach(player -> distanceBeast(player));
				updateScoreboards();				
				timerFinish--;
			}
		}.runTaskTimerAsynchronously(plugin, 20L, 20L);
	}
	
	private void prepareBeast(Player player) {
		this.setBeast(player);
		this.teleport(player, this.getBeastLocation());
		for(Player runners : this.getPlayers()) {
			PlayerScoreboard ps = this.scoreboards.get(runners);
			if(ps != null) {
				ps.addBeast(player);
			}
		}
		if(plugin.existsKit()) {
			plugin.getKitBeast().sendKitBeast(player);
		}
		plugin.sendItemTracker(player);
	}
	
	private void drawnArena() {
		this.broadcast(Lang.ARENA_DRAWN.toString());
		new BukkitRunnable() {
			
			@Override
			public void run() {
				arenaLeaveAll();
			}
		}.runTaskLater(plugin, 20*5);
	}
	
	private void finishArena(boolean winBeast) {
		this.invencible = true;
		this.updateScoreboards();
		this.setState(ArenaState.FINISH);
		this.getSign().updateAll();
		new BukkitRunnable() {
			int i = 5;
			@Override
			public void run() {
				if(i == 0) {
					arenaLeaveAll();
					this.cancel();
					return;
				}
				players.forEach(player -> spawnFireworks(player, Color.RED));
				i--;
			}
		}.runTaskTimer(plugin, 20L, 20L);
	}
	
	private void arenaLeaveAll() {
		Lists.newArrayList(players).forEach(player -> leave(player));
		players.clear();
		loadNewArena();
	}
	
	private void preparePlayer(Player player) {
		player.setHealth(20);
		player.setAllowFlight(false);
		player.setGameMode(GameMode.SURVIVAL);
		clearInventory(player);
		removeEffects(player);
		RFTB.getInstance().sendItemReturn(player);
		player.updateInventory();
	}
	
	private void removeEffects(Player player) {
		for(PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		player.setExp(0);
		player.setLevel(0);
	}
	
	private void clearInventory(Player player) {
		PlayerInventory pInventory = player.getInventory();
		pInventory.setArmorContents(null);
		pInventory.clear();
	}
	
	private void loadNewArena() {
		this.setState(ArenaState.LOBBY);
		this.count = plugin.getConfig().getInt("start_game" , 30);
		this.liberateBeast = plugin.getConfig().getInt("liberate_beast", 10);
		this.timerFinish = plugin.getConfig().getInt("time_game" , 300);
		this.getSign().updateAll();
		this.invencible = true;
	}
	
	public void checkArena() {
		if(getRunners().size() <= 0 && beast != null) {
			broadcast(Lang.BEAST_WIN.toString());
			finishArena(true);
		} 
		if(beast == null) {
			broadcast(Lang.RUNNERS_WIN.toString());
			finishArena(false);
		}
	}
	
	public void spawnFireworks(Player player, Color color) {
		Firework firework = (Firework)player.getWorld().spawn(player.getLocation(), Firework.class);
		FireworkMeta fm = firework.getFireworkMeta();
		fm.addEffect(FireworkEffect.builder().flicker(false).trail(false).with(FireworkEffect.Type.STAR).withColor(color).withFade(color).build());
		fm.setPower(1);
                firework.setFireworkMeta(fm);
                new BukkitRunnable() {
			
			@Override
			public void run() {
				firework.detonate();
			}
	
               }.runTaskLater(plugin, 10L);
	}
	
	public void distanceRunners(boolean message) {
		if(beast == null) {
			return;
		}
		double distance = 10000.0;
		Player nearest = null;
		for(Player runner : this.getRunners()) {
			double dis = this.beast.getLocation().distance(runner.getLocation());
			if(dis < distance) {
				distance = dis;
				nearest = runner;
			}
		}
		if(nearest != null) {
			beast.setCompassTarget(nearest.getLocation());
			if(message) {
				beast.sendMessage(Lang.COMPASS_TARGET.toString().replace("%player%", nearest.getName()));
			}
		}
	}
	
	public void distanceBeast(Player player) {
		int distance = 0;
		try { distance = (int)player.getLocation().distance(beast.getLocation()); } catch (Exception e) { return; }
		if(distance <= 50) {
			player.setLevel(distance);
			player.setExp(1F-(distance * 0.02F));
		}else {
			player.setLevel(0);
			player.setExp(0);
		}
	}
	
	public boolean isInvencible() {
		return invencible;
	}
	
	public ArenaSign getSign() {
		return plugin.getSignManager().getSign(this);
	}
	
	
}
