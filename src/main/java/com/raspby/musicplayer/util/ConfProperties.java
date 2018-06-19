/**
 * 
 */
package com.raspby.musicplayer.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


import lombok.Getter;

/**
 * @author Manuel
 *
 */
@Configuration
//@PropertySource("file:/var/myapp/musicplayer/conf.properties")
//@PropertySource("classpath:../conf.properties")
@PropertySource("file:conf.properties")
@Getter
public class ConfProperties {

	@Value("${music.path}")
	private String path;
	
	public void setPath(String path) throws FileNotFoundException, IOException {
		Properties p = loadProperties();
		p.setProperty("music.path", path);
		saveProperties(p);
		this.path=path;
	}
	
	private Properties loadProperties() throws FileNotFoundException, IOException {
		Properties p = new Properties();
		p.load(new FileReader("conf.properties"));
		return p;
	}
	
	private void saveProperties(Properties p) throws IOException {
		p.store(new FileWriter("conf.properties"),"user-app config");
	}
	
}
