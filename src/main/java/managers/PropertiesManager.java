package managers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;

public class PropertiesManager {
	private static PropertiesManager _instance = null;
	private Properties properties;
	
	private static final String PROPS_FILENAME = "config.properties";
	private static final String PROPS_DEFAULT_FILENAME = "config.default.properties";
	private static final String PROPS_LINUX_DEFAULT_FILENAME = "config.linux.default.properties";
	private static final String PROPS_WINDOWS_DEFAULT_FILENAME = "config.windows.default.properties";
	
	public static PropertiesManager getInstance() {
		if (_instance == null) {
			_instance = new PropertiesManager();
		}
		return _instance;
	}
	
	public PropertiesManager() {
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPS_FILENAME);
			InputStream inputStreamDefault = getClass().getClassLoader().getResourceAsStream(PROPS_DEFAULT_FILENAME);
			InputStream inputStreamLinuxDefault = getClass().getClassLoader().getResourceAsStream(PROPS_DEFAULT_FILENAME);
			InputStream inputStreamWindowsDefault = getClass().getClassLoader().getResourceAsStream(PROPS_DEFAULT_FILENAME);){
			properties = new Properties();
			if (inputStream != null) {
				properties.load(inputStream);
			} else if (SystemUtils.IS_OS_LINUX && inputStreamLinuxDefault != null) {
				properties.load(inputStreamDefault);
			} else if (SystemUtils.IS_OS_WINDOWS && inputStreamWindowsDefault != null) {
				properties.load(inputStreamDefault);
			} else if (inputStreamDefault != null) {
				properties.load(inputStreamDefault);
			} else {
				throw new FileNotFoundException("No properties file found in the classpath");
			}
		} catch (IOException e) {
			System.err.println("Couldn't load config.properties.");
		}
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
}
