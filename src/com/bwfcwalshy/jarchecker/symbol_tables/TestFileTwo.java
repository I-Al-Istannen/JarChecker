package com.bwfcwalshy.jarchecker.symbol_tables;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class for the plugin 
 */
public class TestFileTwo extends JavaPlugin {

	@Override
	public void onEnable() {
		getCommand("novel").setExecutor(this);
		getCommand("novel").setTabCompleter(this);
		try {
			Files.createDirectories(getDataFolder().toPath().resolve("texts"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You must be a player!");
			return true;
		}
				
		// limit the scope. Not needed anywhere else and it would probably confuse me.
		{
			String joined = Arrays.stream(args).sequential().collect(Collectors.joining(" "));
			joined = joined.replaceAll("\"(.+?)\"", "$1|SEP|");
			args = joined.split(Pattern.quote("|SEP|"));
		}
		
		if(args.length < 1) {
			sendUsage(sender);
			return true;
		}
		
		Player player = (Player) sender;		
		ItemStack bookItemStack = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) bookItemStack.getItemMeta();

		if(args.length >= 2) {
			meta.setTitle(color(args[1]).trim());
		}
		if(args.length >= 3) {
			meta.setAuthor(color(args[2]).trim());
		}
		
		
		Path filePath = null;
		try {
			filePath = getDataFolder().toPath().resolve("texts/" + args[0]).toAbsolutePath();
		} catch(InvalidPathException e) {
			sender.sendMessage(color("&6" + args[0] + "&c is no valid path!"));
			return true;
		}	
	
		try {
			if (!Files.exists(filePath) || Files.isDirectory(filePath) ||
					(Files.probeContentType(filePath) != null && !Files.probeContentType(filePath).contains("text/plain"))) {
				sender.sendMessage(color("&cThe file &6" + args[0] + " &cdoesn't exist or is not a text file!"));
				return true;
			}
			
			List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
					
			lines.stream().sequential()
				.flatMap(string -> {
					if(string.length() <= 255) {
						return Stream.of(string);
					}
					List<String> newOnes = new ArrayList<>();
					for(int i = 0; i < string.length(); i += 255) {
						int to = Math.min(string.length(), i + 255);
						// gracefully end a page. Not in the middle of a word.
						while(!Character.isWhitespace(string.charAt(to - 1)) && to < string.length()) {
							to--;
						}
						newOnes.add(string.substring(i, to));
						i = to - 255;
					}
					return newOnes.stream();
				});
						
			bookItemStack.setItemMeta(meta);

			player.getInventory().addItem(bookItemStack);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}
	
	/**
	 * @param input The input to color
	 * @return The colored input
	 */
	private String color(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}
	
	/**
	 * @param sender The sender to send the usage to
	 */
	private void sendUsage(CommandSender sender) {
		sender.sendMessage(color("&cUsage: /novel <&6filename&c> &8[title] [author]"));
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		try {
			if(!Files.exists(getDataFolder().toPath().resolve("texts")) || !Files.isDirectory(getDataFolder().toPath().resolve("texts"))) {
				return Collections.emptyList();
			}
			return Files.list(getDataFolder().toPath().resolve("texts")).map(path -> path.getFileName()).map(path -> path.toString()).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return Collections.emptyList();
	}
}

