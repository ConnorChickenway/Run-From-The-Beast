package mx.connorchickenway.rftb.arena.structure.sign;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import mx.connorchickenway.rftb.RFTB;
import mx.connorchickenway.rftb.arena.Arena;
import mx.connorchickenway.rftb.utils.ConnorUtils;

public class ArenaSign {
	
	private Arena arena;

	private Sign sign;
	private Block back;
	
	public ArenaSign(Sign sign, Arena arena) {
		this.sign = sign;
		this.arena = arena;
		this.back = getAttached();
		this.updateAll();
	}
	
	public void joinArena(Player player) {
		this.arena.join(player);
		this.updateLines();
	}
	
	public void updateLines() {
		List<String> lines = RFTB.getInstance().getConfig().getStringList("sign.lines");
		for(int i = 0; i < 4; i++) {
			sign.setLine(i, ChatColor.translateAlternateColorCodes('&', lines.get(i).replaceAll("%status%", arena.getState().getStatus()).replaceAll("%map%", arena.getName())
					.replaceAll("%online_players%", arena.getSize() + "").replaceAll("%max_players%", arena.getMaxPlayers() + "")));
		}
		sign.update();
	}
	
	@SuppressWarnings("deprecation")
	public void updateAttached() {
		if(back == null) {
			return;
		}
		String[] id = arena.getState().getAttached();
		try {
			back.setTypeIdAndData(Integer.valueOf(id[0]), Byte.valueOf(id[1]), true);
		}catch (Exception e) {
			ConnorUtils.logMessage("Error in the config, path: sign.attached."+arena.getState().getPath());
			e.printStackTrace();
		}	
	}
	
	public void updateAll() {		
		this.updateLines();
		this.updateAttached();
	}
	
	private Block getAttached() {
		if (sign.getType() == Material.WALL_SIGN) {
			return sign.getBlock().getRelative(((org.bukkit.material.Sign)sign.getData()).getAttachedFace());
		}
		return null;
	}
	
	public Location getLocation() {
		return sign.getLocation().clone();
	}
	
	public Arena getArena() {
		return arena;
	}
	
	public Sign getSign() {
		return sign;
	}
	
	
	
}
