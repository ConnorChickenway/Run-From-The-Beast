package mx.connorchickenway.rftb.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import mx.connorchickenway.rftb.RFTB;

public class Config {

	private String name;
	
	private File file;
	private FileConfiguration configuration;
	
	public Config(String directory, String name) {
		this.name = name.contains(".yml") ? name : name+".yml";
		this.createDirectory(directory);
		this.file = new File(directory, this.name);	
		this.saveResource();
		this.configuration = YamlConfiguration.loadConfiguration(file);
	}
	
	private void createDirectory(String string) {
		File directory = new File(string);
		if(!directory.exists()) {
			directory.mkdirs();
		}
	}
	
	private void saveResource() {
		try {
			if(!file.exists()) {
				file.createNewFile();
			}else return;
			
			InputStream in = RFTB.getInstance().getResource(name);
			
			if(in == null) {
				return;
			}
			
			OutputStream out = new FileOutputStream(file);
			
			byte[] buf = new byte[1024];
			int read;

			while ((read = in.read(buf)) > 0) {
				out.write(buf, 0, read);
			}
			
			in.close();
			out.flush();
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public boolean getBoolean(String path) {
		return configuration.getBoolean(path);
	}
	
	public int getInt(String path) {
		return configuration.getInt(path);
	}
	
	public String getString(String path) {
		return configuration.getString(path);
	}
	
	public String getString(String path, String def) {
		return configuration.getString(path, def);
	}
		
	public List<String> getStringList(String path){
		return configuration.getStringList(path);
	}
	
	public boolean existsPath(String path) {
		if(configuration.contains(path)) {
			return true;
		}
		return false;
	}
	
	public void setObject(String path, Object object) {
		configuration.set(path, object);
	}
	
	public void saveConfig() {
		try {
			
			configuration.save(file);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public FileConfiguration getFileConfiguration() {
		return configuration;
	}
	
}
