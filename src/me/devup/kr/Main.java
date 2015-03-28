package me.devup.kr;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	
	private Economy economy = null;
	
	@Override
	public void onEnable() {
		if(!(setupEconomy())) {
			Bukkit.getLogger().severe("[" + this.getName() + "] Could not find Vault. Shutting down...");
			
			Bukkit.getPluginManager().disablePlugin(this);
		}
		
		saveDefaultConfig();
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        
        if (rsp == null)
            return false;
        
        economy = rsp.getProvider();
        
        return economy != null;
    }
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("kr-reload")) {
			reloadConfig();
			
			sender.sendMessage(ChatColor.GREEN + "Kill rewards successfully reloaded.");
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if(!(e.getEntity() instanceof Player))
			return;
		
		if(!(e.getEntity().getKiller() instanceof Player))
			return;
		
		Player killer = e.getEntity().getKiller();
		
		EconomyResponse r = economy.depositPlayer(killer, getConfig().getDouble("Player"));
		
        if(r.transactionSuccess()) {
            killer.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("MoneyReceived").replace("%balance%", economy.format(r.balance)).replace("%amount%", economy.format(r.amount))));
        } else {
            killer.sendMessage(String.format("An error occured: %s", r.errorMessage));
        }
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if(!(e.getEntity().getKiller() instanceof Player))
			return;
		
		Player killer = (Player) e.getEntity().getKiller();
		
		if(!getConfig().contains("Mobs." + e.getEntity().getType().toString()))
			return;
		
		EconomyResponse r = economy.depositPlayer(killer, getConfig().getDouble("Mobs." + e.getEntity().getType().toString()));
		
        if(r.transactionSuccess()) {
            killer.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("MoneyReceived").replace("%balance%", economy.format(r.balance)).replace("%amount%", economy.format(r.amount))));
        } else {
            killer.sendMessage(String.format("An error occured: %s", r.errorMessage));
        }
	}

}
