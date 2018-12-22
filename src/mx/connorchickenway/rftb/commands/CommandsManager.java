package mx.connorchickenway.rftb.commands;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mx.connorchickenway.rftb.RFTB;
import mx.connorchickenway.rftb.arena.Arena;
import mx.connorchickenway.rftb.arena.ArenaManager;
import mx.connorchickenway.rftb.arena.builder.ArenaBuilder;
import mx.connorchickenway.rftb.arena.structure.KitBeast;
import mx.connorchickenway.rftb.utils.ConnorUtils;
import mx.connorchickenway.rftb.utils.Loc;

public class CommandsManager implements CommandExecutor{

	private RFTB plugin;
	private ArenaManager arenaManager;
	
	public CommandsManager(RFTB plugin) {
		this.plugin = plugin;
		this.arenaManager = plugin.getArenaManager();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		if(args.length < 1) {
			player.sendMessage("§aPlugin RFTB created by ConnorChickenway");
			return false;
		}
		
		Arena arena;
		ArenaBuilder builder;
		
		String key = args[0];
		
		switch (key.toLowerCase()) {
		
		case "leave":
			arena = arenaManager.getArena(player);
			if(arena != null) {
				arena.leave(player);
			} else {
				player.sendMessage("§cYou're not in a game!");
			}
			return true;
		
		case "setup":
			
			if(!player.hasPermission("rftb.admin")) {
				player.sendMessage("§cYou don't have permission - Plugin created by ConnorChickenway");
				break;
			}
			
			if(args.length <= 1) {
				break;
			}
			
			if(args[1].equalsIgnoreCase("arena")) {
				if(args.length == 3) {
					if(args[2].equalsIgnoreCase("saveInventoryBeast")) {
						try {
							KitBeast.serializableKit(player);
							return true;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return false;
				}

				if(args.length == 4) {
					builder = arenaManager.getArenaBuilder(args[3]);
					if(args[2].equalsIgnoreCase("createArenaBuilder")) {
						arena  = arenaManager.getArena(args[3]);
						if(arena != null) {
							player.sendMessage("§cYou can't create a arena builder because there is already a arena with that name!");
							return false;
						}
						if(builder != null) {
							player.sendMessage("§cThe arena builder " + args[3] + " already exists!");
						} else {
							arenaManager.addArenaBuilder(args[3]);
							player.sendMessage("§aThe arena builder " + args[3] + " created successfully!");
						}
						return true;
					} else if(args[2].equalsIgnoreCase("createArena")) {
						if(builder != null) {
							if(builder.createArena(true)) {
								player.sendMessage("§aThe arena " + args[3] + " created successfully!");
							}
						} else {
							player.sendMessage("§cThe arena builder " + args[3] + " not exists!");
						}
						return true;
					} else if(args[2].equalsIgnoreCase("lobby")) {
						if(builder != null) {
							builder.setLobbyLocation(new Loc(player.getLocation()));
							player.sendMessage("§aSet lobby location in the arena builder "+ args[3]);
						} else { 
							player.sendMessage("§cThe arena builder " + args[3] + " not exists!");
						}
						return true;
					} else if(args[2].equalsIgnoreCase("beast")) {
						if(builder != null) {
							builder.setBeastLocation((new Loc(player.getLocation())));
							player.sendMessage("§aSet beast location in the arena builder "+ args[3]);
						} else {
							player.sendMessage("§cThe arena builder " + args[3] + " not exists!");
						}
						return true;
					} else if(args[2].equalsIgnoreCase("runners")) {
						if(builder != null) {
							builder.setRunnersLocation((new Loc(player.getLocation())));
							player.sendMessage("§aSet runners location in the arena builder "+ args[3]);
						} else {
							player.sendMessage("§cThe arena builder " + args[3] + " not exists!");
						}
						return true;
					} 
				} else {
					if(args.length == 5) {
						builder = arenaManager.getArenaBuilder(args[3]);
						if(args[2].equalsIgnoreCase("setMinPlayers")) {
							
							if(builder != null) {
								try {
									int i = Integer.valueOf(args[4]);
									if(i < 2) {
										player.sendMessage("§cThe number must be greater than or equal to 2");
										return false;
									}
									int maxPlayers = builder.getMaxPlayers();
									if(maxPlayers > 0 && i > maxPlayers) {
										player.sendMessage("§cThe number you entered is higher than maxplayers!");
										return false;
									}
									builder.setMinPlayers(i);
									player.sendMessage("§aSet minplayers in the arena builder "+ args[3]);
									return true;
								}catch (NumberFormatException e) {
									player.sendMessage("§cYou have to use numbers!");
								}
							}
						} else if(args[2].equalsIgnoreCase("setMaxPlayers")) { 
							if(builder != null) {
								try {
									int i = Integer.valueOf(args[4]);
									int minPlayers = builder.getMinPlayers();
									if(minPlayers > 0 && i < minPlayers) {
										player.sendMessage("§cThe number you entered is less than the minplayers!");
										return false;
									}
									builder.setMaxPlayers(i);
									player.sendMessage("§aSet maxplayers in the arena builder "+ args[3]);
									return true;
								}catch (NumberFormatException e) {
									player.sendMessage("§cYou have to use numbers!");
								}
							}
						}
					} 
				}
			
			} else if(args[1].equalsIgnoreCase("setmainlobby")) {
				plugin.setMainLobby(new Loc(player.getLocation()));
				plugin.getConfig().set("spawn", ConnorUtils.getLocationString(player.getLocation(), true));
				plugin.saveConfig();
				sender.sendMessage("§aSet main lobby successfully!");
				return true;
			} 
					
			break;
				
		default:
			break;
		}
		
		return false;
	}	
}

