package com.popupmc.popupmoneyexchange;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class Emeralds implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Make sure this is a console
        if(!(sender instanceof Player)) {
            PopupMoneyExchange.plugin.getLogger().info("Only players may use this command.");
            return false;
        }

        // Make sure it has 2 arguments
        if(args.length != 2) {
            printUsageError(sender);
            return false;
        }

        // Gather information
        Player player = (Player) sender; // Player
        String dir = args[0]; // Direction to/from

        // Requested amount, fix to 2 decimal places
        int amountCoins;
        try {
            amountCoins = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            printUsageError(sender);
            return false;
        }

        // Get the amount in coins
        float amountMoney = amountCoins * PopupMoneyExchange.coinWorth;
        amountMoney = (float) Math.round(amountMoney * 100) / 100;

        // Send control to a direction
        if(dir.equals("buy"))
            return toCoins(player, amountCoins, amountMoney);
        else if(dir.equals("sell"))
            return fromCoins(player, amountCoins, amountMoney);

        // Error out if invalid direction
        printUsageError(sender);
        return false;
    }

    public void printUsageError(@NotNull CommandSender sender) {
        sender.sendMessage(new String[]{
                ChatColor.translateAlternateColorCodes('&', "&cInvalid usage..."),
                ChatColor.translateAlternateColorCodes('&', "&6Convert money to or from emeralds")
        });
    }

    public void printVaultError(@NotNull Player player, EconomyResponse r) {
        player.sendMessage(new String[]{
                ChatColor.translateAlternateColorCodes('&', "&cA vault error has occured: &6" + r.errorMessage),
                ChatColor.translateAlternateColorCodes('&', "&cAborting transaction..."),
        });
    }

    public boolean toCoins(Player player, int amountCoins, float amountMoney) {

        // Get current balance
        float curBal = (float)PopupMoneyExchange.econ.getBalance(player);

        // Make sure player has enough money to buy these coins
        if(curBal < amountMoney) {
            player.sendMessage(new String[]{
                    ChatColor.translateAlternateColorCodes('&',
                            "&cYou don't have enough money to buy &6" + amountCoins + " emeralds&c, costing &6❇" + amountMoney)
            });
            return false;
        }

        // How much space for these coins does the player have
        int freeSpace = invFreeSpace(player.getInventory(), PopupMoneyExchange.emeralds.getType(), PopupMoneyExchange.emeralds.asOne());

        if(freeSpace < amountCoins) {
            player.sendMessage(new String[]{
                    ChatColor.translateAlternateColorCodes('&',
                            "&cYou don't have enough room in your inventory for &6" + amountCoins + " emeralds.")
            });
            return false;
        }

        // Make a withdrawl
        EconomyResponse r = PopupMoneyExchange.econ.withdrawPlayer(player, amountMoney);

        if(!r.transactionSuccess()) {
            printVaultError(player, r);
            return false;
        }

        int stackAmount = 0;
        int tmpAmountCoins = amountCoins;
        while (tmpAmountCoins > 0) {
            // Figure out stack to give
            stackAmount = Math.min(tmpAmountCoins, PopupMoneyExchange.emeralds.getMaxStackSize());

            // Set stack size
            ItemStack coin = PopupMoneyExchange.emeralds.asOne();
            coin.setAmount(stackAmount);

            // Add stack to inventory
            player.getInventory().addItem(coin);

            // Subtract coin amount
            tmpAmountCoins -= stackAmount;
        }

        player.sendMessage(new String[]{
                ChatColor.translateAlternateColorCodes('&',
                        "&aYou've bought &e" + amountCoins + " emeralds&a costing &e❇" + amountMoney)
        });
        return true;
    }

    public boolean fromCoins(Player player, int amountCoins, float amountMoney) {

        // Make sure player has enough coins in inventory
        if(!player.getInventory().containsAtLeast(PopupMoneyExchange.emeralds.asOne(), amountCoins)) {
            player.sendMessage(new String[]{
                    ChatColor.translateAlternateColorCodes('&',
                            "&cYou don't have &6" + amountCoins + " emeralds&c to sell")
            });
            return false;
        }

        // Add money to players inventory
        EconomyResponse r = PopupMoneyExchange.econ.depositPlayer(player, amountMoney);

        if(!r.transactionSuccess()) {
            printVaultError(player, r);
            return false;
        }

        // Remove coins from inventory
        invRemoveSpace(player.getInventory(), PopupMoneyExchange.emeralds.asOne(), amountCoins);

        player.sendMessage(new String[]{
                ChatColor.translateAlternateColorCodes('&',
                        "&aYou've sold &e" + amountCoins + " emeralds&a for &e❇" + amountMoney)
        });
        return true;
    }

    public int invFreeSpace(PlayerInventory inv, Material m, ItemStack item) {
        int count = 0;
        for (int slot = 0; slot < 36; slot ++) {
            ItemStack is = inv.getItem(slot);
            if (is == null) {
                count += m.getMaxStackSize();
            }
            if (is != null) {
                if (is.isSimilar(item)){
                    count += (m.getMaxStackSize() - is.getAmount());
                }
            }
        }
        return count;
    }

    public static void invRemoveSpace(PlayerInventory inventory, ItemStack item, int amount) {
        if (amount <= 0) return;
        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            ItemStack is = inventory.getItem(slot);
            if (is == null) continue;
            if (is.isSimilar(item)) {
                int newAmount = is.getAmount() - amount;
                if (newAmount > 0) {
                    is.setAmount(newAmount);
                    break;
                } else {
                    inventory.clear(slot);
                    amount = -newAmount;
                    if (amount == 0) break;
                }
            }
        }
    }
}
