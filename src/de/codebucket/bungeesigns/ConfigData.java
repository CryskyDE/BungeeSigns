package de.codebucket.bungeesigns;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.codebucket.bungeesigns.utils.CustomVariable;
import de.codebucket.bungeesigns.utils.LocationSerialiser;
import de.codebucket.bungeesigns.utils.SignLayout;
import de.codebucket.bungeesigns.utils.ServerPing;
import de.codebucket.bungeesigns.utils.BungeeSign;

public class ConfigData 
{
	private BungeeSigns plugin;
	private boolean log;
	private FileConfiguration config;
	private FileConfiguration layout;
	private FileConfiguration sign;
	private List<ServerPing> servers = new ArrayList<>();
	private List<BungeeSign> signs = new ArrayList<>();
	private List<CustomVariable> variables = new ArrayList<>();
	private List<Block> blocks = new ArrayList<>();
	private Map<String, SignLayout> layouts = new HashMap<>();
	private long cooldown;
	private int pingTimeout;
	private int pingInterval;
	
	private File config_file = new File("plugins/BungeeSigns/config.yml");
	private File layout_file = new File("plugins/BungeeSigns/layout.yml");
	private File sign_file = new File("plugins/BungeeSigns/signs.yml");
	
	public enum ConfigType
	{
		SETTINGS,
		LAYOUTS,
		SIGNS;
	}
	
	public ConfigData(BungeeSigns plugin)
	{
		this.plugin = plugin;
	}
	
	public void loadConfig()
	{
		config = null;
		layout = null;
		sign = null;
		
		servers.clear();
		signs.clear();
		layouts.clear();
		
		if(config_file.exists())
		{
			config = YamlConfiguration.loadConfiguration(config_file);
		}
		else
		{
			plugin.saveResource("config.yml", false);
			config = YamlConfiguration.loadConfiguration(config_file);
		}
		
		if(layout_file.exists())
		{
			layout = YamlConfiguration.loadConfiguration(layout_file);
		}
		else
		{
			plugin.saveResource("layout.yml", false);
			layout = YamlConfiguration.loadConfiguration(layout_file);
		}
		
		if(sign_file.exists())
		{
			sign = YamlConfiguration.loadConfiguration(sign_file);
		}
		else
		{
			plugin.saveResource("signs.yml", false);
			sign = YamlConfiguration.loadConfiguration(sign_file);
		}
		
		loadSettings();
		loadServers();
		loadLayouts();
		loadSigns();
		loadVariables();
	}
	
	public void reloadConfig()
	{
		config = null;
		layout = null;
		sign = null;
		
		servers.clear();
		signs.clear();
		layouts.clear();
		variables.clear();
		
		loadConfig();
	}
	
	public void unloadConfig()
	{
		config = null;
		layout = null;
		sign = null;
		
		servers.clear();
		signs.clear();
		layouts.clear();
		variables.clear();
	}
	
	private void loadSettings()
	{
		this.log = this.config.getBoolean("logConsole");
		this.pingInterval = this.config.getInt("interval");
		this.pingTimeout = this.config.getInt("timeout");
		this.cooldown = (this.config.getInt("cooldown") * 1000);
	}
	
	private void loadServers()
	{
		ConfigurationSection srv = this.config.getConfigurationSection("servers");
		
		for(String server : srv.getKeys(false))
		{
			ConfigurationSection cs = srv.getConfigurationSection(server);
			String displayname = cs.getString("displayname");
			String[] address = cs.getString("address").split(":");
			String ip = address[0];
			String port = address[1];
			
			ServerPing serverping = new ServerPing(server, displayname, ip, Integer.valueOf(port), this.pingTimeout);
			serverping.resetPingDelay();
			this.servers.add(serverping);
		}
	}
	
	private void loadLayouts()
	{
		ConfigurationSection layouts = this.layout.getConfigurationSection("layouts");
		
		for(String layout : layouts.getKeys(false)) 
		{
		    ConfigurationSection cs = layouts.getConfigurationSection(layout);
		    String online = cs.getString("online");
		    String offline = cs.getString("offline");
		    List<String> lines = cs.getStringList("layout");
		    boolean teleport = cs.getBoolean("teleport");
		    String offlineInt = cs.getString("offline-int");
		    String offlineMotd = cs.getString("offline-motd");
		    String offlineMessage = cs.getString("offline-message");
		    String cooldownMessage = cs.getString("cooldown-message");
		    SignLayout signLayout = new SignLayout(layout, online, offline, lines, teleport, offlineInt, offlineMotd, offlineMessage, cooldownMessage);
		    this.layouts.put(layout, signLayout);
		}
	}
	
	private void loadSigns()
	{
		for(String sign : this.sign.getStringList("signs"))
		{
			Location location = LocationSerialiser.stringToLocationSign(sign);
			String server = LocationSerialiser.getServerFromSign(sign);
			String layout = LocationSerialiser.getLayoutFromSign(sign);
			
			Block b = location.getBlock();
			this.blocks.add(b);
			
			BungeeSign serversign = new BungeeSign(server, location, layout);			
			this.signs.add(serversign);
		}
	}
	
	private void loadVariables()
	{
		ConfigurationSection variables = this.layout.getConfigurationSection("variables");
		
		for(String var : variables.getKeys(false)) 
		{
		    ConfigurationSection cs = variables.getConfigurationSection(var);
		    String type = cs.getString("type");
		    String args = cs.getString("arguments");
		    
		    CustomVariable cvar = new CustomVariable(type, "%"+var+"%", args);
		    this.variables.add(cvar);
		}
	}
	
	public SignLayout getLayout(String layout)
	{
		return this.layouts.get(layout);
	}
	
	public ServerPing getServer(String server)
	{
		for(ServerPing info : this.servers)
		{
			if(info.getName().equals(server))
			{
				return info;
			}
		}
		
		return null;
	}
	
	public long getCooldown()
	{
		return this.cooldown;
	}
	
	public void setCooldown(int seconds)
	{
		this.cooldown = seconds * 1000;
	}
	
	public boolean getConsoleLog()
	{
		return this.log;
	}
	
	public BungeeSigns getPlugin()
	{
	    return this.plugin;
	}

	public FileConfiguration getConfig(ConfigType type)
	{
	    if(type.equals(ConfigType.SETTINGS))
	    {
	    	return this.config;
	    }
	    else if(type.equals(ConfigType.LAYOUTS))
	    {
	    	return this.layout;
	    }
	    else if(type.equals(ConfigType.SIGNS))
	    {
	    	return this.sign;
	    }
	    
		return null;
	}
	
	public void saveConfig(ConfigType type) throws IOException
	{
		if(type.equals(ConfigType.SETTINGS))
	    {
	    	config.save(config_file);
	    }
	    else if(type.equals(ConfigType.LAYOUTS))
	    {
	    	layout.save(layout_file);
	    }
	    else if(type.equals(ConfigType.SIGNS))
	    {
	    	sign.save(sign_file);
	    }
	}
	
	public List<ServerPing> getServers()
	{
		return this.servers;
	}
	
	public void setServers(List<ServerPing> servers)
	{
	    this.servers = servers;
	}
	
	public List<BungeeSign> getSigns()
	{
		return this.signs;
	}
	
	public void setSigns(List<BungeeSign> signs)
	{
	    this.signs = signs;
	}
	
	public Map<String, SignLayout> getLayouts()
	{
	    return this.layouts;
	}
	
	public void setLayouts(Map<String, SignLayout> signLayouts)
	{
	    this.layouts = signLayouts;
	}
	
	public List<CustomVariable> getVariables() 
	{
		return variables;
	}

	public void setVariables(List<CustomVariable> variables) 
	{
		this.variables = variables;
	}

	public List<Block> getBlocks()
	{
		return this.blocks;
	}
	
	public void setBlocks(List<Block> blocks)
	{
		this.blocks = blocks;
	}
	
	public int getPingInterval()
	{
		return this.pingInterval;
	}
	
	public int getPingTimeout()
	{
		return this.pingTimeout;
	}
	
	public void setPingInterval(int interval)
	{
		this.pingInterval = interval;
	}
	
	public void setPingTimeout(int timeout)
	{
		this.pingTimeout = timeout;
	}
	
	public BungeeSign getSignFromLocation(Location l)
	{
		for(BungeeSign sign : signs)
		{
			if(l.equals(sign.getLocation()))
			{
				return sign;
			}
		}
		
		return null;
	}
	
	public boolean containsSign(Block b)
	{
		if(blocks.contains(b))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void addSign(Block b, String server, String layout)
	{
		String index = LocationSerialiser.locationSignToString(b.getLocation(), server, layout);
		List<String> list = this.sign.getStringList("signs");
		list.add(index);
		this.sign.set("signs", list);
		
		try 
		{
			this.saveConfig(ConfigType.SIGNS);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		BungeeSign ssign = new BungeeSign(server, b.getLocation(), layout);
		signs.add(ssign);
		blocks.add(b);
	}
	
	public void removeSign(Block b)
	{
		Location location;
		for(BungeeSign sign : signs)
		{
			if(b.getLocation().equals(sign.getLocation()))
			{
				location = sign.getLocation();
				String index = LocationSerialiser.locationSignToString(location, sign.getServer(), sign.getLayout());
				List<String> list = this.sign.getStringList("signs");
				list.remove(index);
				this.sign.set("signs", list);
				
				try 
				{
					this.saveConfig(ConfigType.SIGNS);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				
				signs.remove(sign);
				blocks.remove(b);
				break;
			}
		}
	}
}
