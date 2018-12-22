package mx.connorchickenway.rftb.arena.structure;

import org.bukkit.ChatColor;

import mx.connorchickenway.rftb.RFTB;
import mx.connorchickenway.rftb.utils.Config;

public enum Lang {

	JOIN_ARENA("join_arena" , "&a%player% &7joined the game (&a%players_size%&7/&a%max_players%&7)") , 
	LEAVE_ARENA("leave_arena" , "%player% leave the game (%players_size%/%max_players%)"),
	ARENA_FULL("arena_full" , "&cThe arena is full!"), 
	ARENA_IN_GAME("arena_in_game" , "&cThe arena is in game!"),
	ARENA_FINISH("arena_finish", "&cThe arena is finished!"),
	ARENA_DRAWN("arena_drawn", "&cThe game has been drawn!"),
	NECESSARY_PLAYERS("necessary_players" , "&aThe countdown has been cancelled, because the minimum number of players is lower!"),
	START_GAME("start_game.message" , "&eThe game starts in &c%count% &e%seconds%!"),
	SECONDS("seconds" , "second;seconds"),
	LIBERATE_BEAST("liberate_beast" , "&cThe beast will be released in &e%count% &c%seconds%!"),
	HAS_LIBERATE("has_liberate" , "&cThe beast has been liberated!"),
	BEAST_WIN("beast_win" , "&cThe beast has won!"),
	RUNNERS_WIN("runners_win" , "&aThe runners has won!"),
	BEAST_KILL("beast_kill" , "&c%player% &7was killed by &cThe Beast!"), 
	RUNNERS_KILL("runner_kill" , "&cThe Beast &7was killed by &c%player%!"),
	BEAST_DEATH("beast_death" , "&cThe beast &7has died!"),
	RUNNER_DEATH("runner_death", "&c%player% &7has died!"),
	COMPASS_TARGET("compass_target", "&7The compass is now aimed at the player &a%player%."),
	
	ROLES("scoreboard.game.roles" , "Beast;Runner");
	
	private String path;
	private String def;
	
	private Lang(String path, String def) {
		this.path = path;
		this.def = def;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getDef() {
		return def;
	}
	
	@Override
	public String toString() {
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path, def));
	}
	
	public static Config getConfig() {
		return RFTB.getInstance().getLang();
	}
	
}
