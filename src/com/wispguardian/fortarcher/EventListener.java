package com.wispguardian.fortarcher;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {

	@EventHandler
	public void onEntityDamage (EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			Challenge c = Challenge.getChallenge(player);
			if(c != null && event.getCause() == DamageCause.VOID) {
				event.setCancelled(true); // cancel event to avoid damage
				int base = 0;
				if(c.getOpp() == player) base = 1;
				player.setInvulnerable(true);
				player.teleport(c.getBases()[base]);
				player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());

				// reset flag
				ItemStack gold = new ItemStack(Material.GOLD_BLOCK);
				if(player.getInventory().contains(gold)) {
					player.getInventory().remove(gold);
					c.getFlags()[base==0?1:0].getBlock().setType(Material.GOLD_BLOCK);
				}
				
				// take away invulnerability after 2 seconds
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.instance, new Runnable(){
					@Override
					public void run(){
						player.setInvulnerable(false);
					}
				}, 2*20L);
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Challenge c = Challenge.getChallenge(player);
		if(c != null) {
			Player host = c.getHost();
			Player opp = c.getOpp();
			c.addPoint((player == host)?opp:host);
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		Challenge c = Challenge.getChallenge(player);
		if(c != null) {
			int base = 0;
			if(player == c.getOpp()) base = 1;
			event.setRespawnLocation(c.getBases()[base]);
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if(Challenge.inGame(player)) {
			Material block = event.getBlock().getType();
			if(!Arrays.stream(Utils.breakable).anyMatch(block::equals)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Challenge c = Challenge.getChallenge(player);
		if(c != null) {
			Block block = event.getBlock();
			if(block.getType() == Material.GOLD_BLOCK) event.setCancelled(true);
			else {
				event.getItemInHand().setAmount(64);
				c.addBlock(event.getBlock());
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(Challenge.inGame(player)) {
			Challenge c = Challenge.getChallenge(player);
			Block block = event.getClickedBlock();
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK &&
					block.getType() == Material.GOLD_BLOCK &&
					event.getClickedBlock().getType() == Material.GOLD_BLOCK) {
				if((player == c.getHost() && block.getLocation().equals(c.getFlags()[1])) ||
						(player == c.getOpp() && block.getLocation().equals(c.getFlags()[0]))) {
					// take flag
					block.setType(Material.AIR);
					player.getInventory().addItem(new ItemStack(Material.GOLD_BLOCK, 1));
					player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 100f, 1f);
				}else if((player == c.getHost() && block.getLocation().equals(c.getFlags()[0])) ||
						(player == c.getOpp() && block.getLocation().equals(c.getFlags()[1]))) {
					// capture flag
					ItemStack gold = new ItemStack(Material.GOLD_BLOCK);
					if(player.getInventory().contains(gold)) {
						player.getInventory().remove(gold);
						player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 100f, 1f);
						Challenge.getChallenge(player).addPoint(player);
						// reset map
					}
				}
			}
		}
	}

}
