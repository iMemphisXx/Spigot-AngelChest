package de.jeff_media.AngelChest;

import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class AngelChestCommandUtils {

	static boolean hasEnoughMoney(Player p, double money, Main main) {

		if(money <= 0) {
			return true;
		}

		Plugin v = main.getServer().getPluginManager().getPlugin("Vault");

		if(v == null) {
			return true;
		}

		RegisteredServiceProvider<Economy> rsp = main.getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null) return true;

		if(rsp.getProvider()==null) return true;

		Economy econ = rsp.getProvider();

		if(econ.getBalance(p)>=money) {
			econ.withdrawPlayer(p,"AngelChest Teleport",money);
			return true;
		} else {
			p.sendMessage(main.messages.MSG_NOT_ENOUGH_MONEY);
			return false;
		}

	}

	// Parses the first argument for the chest index in acinfo and returns a valid chest if it exists
	protected static AngelChest argIdx2AngelChest(Main plugin, Player p, String[] args) {
		// Get all AngelChests by this player
		ArrayList<AngelChest> angelChestsFromThisPlayer = Utils.getAllAngelChestsFromPlayer(p, plugin);

		if(angelChestsFromThisPlayer.size()==0) {
			p.sendMessage(plugin.messages.MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS);
			return null;
		}

		if(angelChestsFromThisPlayer.size() > 1 && args.length == 0) {
			p.sendMessage("Please specify which AngelChest you would like select");
			sendListOfAngelChests(plugin, p);
			return null;
		}

		int chestIdx = 0;

		if(args.length > 0) {
			chestIdx = Integer.parseInt(args[0]) - 1;
		}

		if(chestIdx >= angelChestsFromThisPlayer.size() || chestIdx < 0) {
			p.sendMessage("Invalid AngelChest!");
			return null;
		}

		return angelChestsFromThisPlayer.get(chestIdx);
	}

	protected static void teleportPlayerToChest(Main plugin, Player p, AngelChest ac) {
		if(!p.hasPermission("angelchest.tp")) {
			p.sendMessage(plugin.getCommand("aclist").getPermissionMessage());
			return;
		}

		if(!ac.owner.equals(p.getUniqueId())) {
			p.sendMessage(ChatColor.RED+"You do not own this AngelChest.");
			return;
		}
		
		double price = plugin.getConfig().getDouble("price-teleport");
		if(price>0 && !hasEnoughMoney(p,price,plugin)) {
			return;
		}

		Location loc = ac.block.getLocation();
		List<Block> possibleSpawnPoints = Utils.getPossibleChestLocations(loc, plugin.getConfig().getInt("max-radius"), plugin);
		Utils.sortBlocksByDistance(loc.getBlock(), possibleSpawnPoints);
		
		Location teleportLocation = loc;
		
		if(possibleSpawnPoints.size()>0) {
			teleportLocation = possibleSpawnPoints.get(0).getLocation();
		}
		
		teleportLocation.setDirection(loc.toVector().subtract(teleportLocation.toVector()));
		teleportLocation.add(0.5,0,0.5);
		
		p.teleport(teleportLocation, TeleportCause.PLUGIN);
	}
	
	protected static void unlockSingleChest(Main plugin, Player p, AngelChest ac) {
//		if(!p.hasPermission("angelchest.tp")) {
//			p.sendMessage(plugin.getCommand("aclist").getPermissionMessage());
//			return;
//		}

		if(!ac.owner.equals(p.getUniqueId())) {
			p.sendMessage(ChatColor.RED+"You do not own this AngelChest.");
			return;
		}
		if(!ac.isProtected) {
			p.sendMessage(ChatColor.RED+"This AngelChest is already unlocked.");
			return;
		}
		
		ac.unlock();
		p.sendMessage(plugin.messages.MSG_UNLOCKED_ONE_ANGELCHEST);
	}

	protected static void sendListOfAngelChests(Main plugin, Player p) {
		// Get all AngelChests by this player
		ArrayList<AngelChest> angelChestsFromThisPlayer = Utils.getAllAngelChestsFromPlayer(p, plugin);
		
		if(angelChestsFromThisPlayer.size()==0) {
			p.sendMessage(plugin.messages.MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS);
			return;
		}
		
		int chestIndex = 1;
		Block b;

		for(AngelChest angelChest : angelChestsFromThisPlayer) {
			int remaining = angelChest.secondsLeft;
			int sec = remaining % 60;
			int min = (remaining / 60) % 60;
			int hour = (remaining / 60) / 60;

			b = angelChest.block;
			String tpCommand=null;
			String fetchCommand=null;
			String unlockCommand=null;
			if(p.hasPermission("angelchest.tp")) {
				tpCommand="/actp " + chestIndex;
			}
			if(p.hasPermission("angelchest.fetch")) {
				fetchCommand="/acfetch " + chestIndex;
			}
			if(angelChest.isProtected) {
				unlockCommand="/acunlock " + chestIndex;
			}
			
			String text = String.format("[%d] %02d:%02d:%02d §aX:§f %d §aY:§f %d §aZ:§f %d | %s ",
				chestIndex, hour, min, sec, b.getX(), b.getY(), b.getZ(), b.getWorld().getName()
			);
			p.spigot().sendMessage(LinkUtils.getLinks(p, text, tpCommand, unlockCommand, fetchCommand, plugin));
			chestIndex++;
		}
	}


	protected static void unlockAllChests(Main plugin, Player p) {
		ArrayList<AngelChest> angelChestsFromThisPlayer = Utils.getAllAngelChestsFromPlayer(p, plugin);

		int chestsUnlocked = 0;
			
		for(AngelChest angelChest : angelChestsFromThisPlayer) {
			if(angelChest.isProtected) {
				angelChest.unlock();
				chestsUnlocked++;
			}
		}
		
		if(chestsUnlocked == 0) {
			p.sendMessage(plugin.messages.MSG_ALL_YOUR_ANGELCHESTS_WERE_ALREADY_UNLOCKED);
			return;
		}
		
		else if(chestsUnlocked == 1) {
			p.sendMessage(plugin.messages.MSG_UNLOCKED_ONE_ANGELCHEST);
			return;
		}
		
		else {
			p.sendMessage(String.format(plugin.messages.MSG_UNLOCKED_MORE_ANGELCHESTS,chestsUnlocked));
			return;
		}
	}
}
