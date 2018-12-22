package mx.connorchickenway.rftb.arena.structure;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import mx.connorchickenway.rftb.RFTB;
import mx.connorchickenway.rftb.arena.Arena;

public class PlayerScoreboard {
   
	private static RFTB plugin = RFTB.getInstance();
	
	private Player player;
    private Scoreboard scoreboard;
    private Objective sidebar;
    
    private Team beast;
    private Arena arena;
    
    public PlayerScoreboard(Player player, Arena arena) {
    	this.arena = arena;
    	this.player = player;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        sidebar = scoreboard.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        beast = scoreboard.registerNewTeam("beast");
        beast.setPrefix(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix_beast", "&c&lBEAST&7")));
        // Create Teams
        for(int i =1 ; i <= 15; i++) {
            Team team = scoreboard.registerNewTeam("SLOT_" + i);
            team.addEntry(genEntry(i));
        }
        setTitle(Lang.getConfig().getString("scoreboard.name"));
        player.setScoreboard(scoreboard);
        this.updateSlots();
    }

    public void updateSlots() {
    	ArenaState state = arena.getState();
    	if(state.equals(ArenaState.FINISH)) {
    		return;
    	}
    	List<String> list = new ArrayList<>();
    	for(String str : RFTB.getInstance().getLang().getStringList("scoreboard."+ state.getPath() + ".scores")) {
    		list.add(updateVariables(str));
    	}
    	this.setSlotsFromList(list);
    }
    
    private String updateVariables(String str) {
    	if(arena.isState(ArenaState.PRE_GAME)) {
    		str = str.replace("%count%", arena.getCount() + "");
    	}else if(arena.isState(ArenaState.GAME)){
    		str = str.replace("%alive_runners%", arena.getRunners().size() + "");
    		str = str.replace("%role%", getRole());
    		str = str.replace("%time_left%", convertIntToString(arena.timerFinish));
    	}
    	str = str.replace("%map%", arena.getName());
    	str = str.replace("%date%", new SimpleDateFormat(Lang.getConfig().getString("scoreboard.format_date" , "dd/MM/yy")).format(Calendar.getInstance().getTime()));
    	str = str.replace("%online_players%", arena.getSize() + "");
    	str = str.replace("%max_players%", arena.getMaxPlayers() + "");
    	return str;
    }
    
    private String getRole() {
    	String[] roles = Lang.ROLES.toString().split(";");
    	if(arena.beast != null && arena.beast.equals(player)) {
    		return roles[0];
    	}
    	return roles[1];
    }
    
    private String convertIntToString(int time) {
    	return String.format("%02d:%02d", time / 60, time % 60);
    }
    
    public void addBeast(Player player) {
    	beast.addEntry(player.getName());
    }
    
    public void setTitle(String title) {
        title = ChatColor.translateAlternateColorCodes('&', title);
        sidebar.setDisplayName(title.length() > 32 ? title.substring(0, 32) : title);
    }

    public void setSlot(int slot, String text) {
        Team team = scoreboard.getTeam("SLOT_" + slot);
        String entry = genEntry(slot);
        if(!scoreboard.getEntries().contains(entry)) {
            sidebar.getScore(entry).setScore(slot);
        }

        text = ChatColor.translateAlternateColorCodes('&', text);
        String pre = getFirstSplit(text);
        String suf = getFirstSplit(ChatColor.getLastColors(pre) + getSecondSplit(text));
        team.setPrefix(pre);
        team.setSuffix(suf);
    }

    public void removeSlot(int slot) {
        String entry = genEntry(slot);
        if(scoreboard.getEntries().contains(entry)) {
            scoreboard.resetScores(entry);
        }
    }

    public void setSlotsFromList(List<String> list) {
        while(list.size() > 15) {
            list.remove(list.size() -1);
        }
       
        int slot = list.size();

        if(slot<15) {
            for(int i=(slot +1); i<=15; i++) {
                removeSlot(i);
            }
        }

        for(String line : list) {
            setSlot(slot, line);
            slot--;
        }
    }
    
    public void unregister() {
    	sidebar.unregister();
    	beast.unregister();
    	player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private String genEntry(int slot) {
        return ChatColor.values()[slot].toString();
    }

    private String getFirstSplit(String s) {
        return s.length()>16 ? s.substring(0, 16) : s;
    }

    private String getSecondSplit(String s) {
        if(s.length()>32) {
            s = s.substring(0, 32);
        }
        return s.length()>16 ? s.substring(16) : "";
    }
   
}
