/**
 * 
 */
package com.raspby.musicplayer.service;

import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Manuel
 *
 */
public interface FileService {
	
	public boolean exists(String path);
	
	public String getUserMusicPath();
	
	public Set<String> getListMusicFile();
	
	public void saveFile(MultipartFile file);
	
	public Resource getFile(String name);
	
	public boolean deleteFile(String name);
	
}
