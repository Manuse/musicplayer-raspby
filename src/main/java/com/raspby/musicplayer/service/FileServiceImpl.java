/**
 * 
 */
package com.raspby.musicplayer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.raspby.musicplayer.exception.GeneralException;
import com.raspby.musicplayer.util.ConfProperties;
import com.raspby.musicplayer.util.SecurityUtils;

/**
 * @author Manuel
 *
 */
@Service
public class FileServiceImpl implements FileService{

	@Autowired
	private ConfProperties confProperties;

	@Override
	public boolean exists(String name) {
		return new File(getUserMusicPath()+name).exists();
	}
	
	@Override
	public String getUserMusicPath() {
		return confProperties.getPath()+"/"+SecurityUtils.getCurrentLogin()+"/";
	}

	@Override
	public Set<String> getListMusicFile() {
		File file=new File(getUserMusicPath());
		if(!file.exists()) {
			file.mkdirs();
			return new HashSet<String>();
		}
		return Arrays.stream(file.list()).filter(f -> f.toLowerCase().endsWith(".mp3")).collect(Collectors.toSet());
	}

	@Override
	public void saveFile(MultipartFile file) {
		try {
			Path path = Paths.get(getUserMusicPath());
			
			if (Files.notExists(path)) {
				Files.createDirectories(path);
			}

			if (file == null) {
				throw new GeneralException("Archivo vacio o incorrecto");
			}else if (!file.getOriginalFilename().endsWith(".mp3")) {
				throw new GeneralException(file.getOriginalFilename()+": Archivo incorrecto");
			}else if(Files.exists(Paths.get(getUserMusicPath()+file.getOriginalFilename()))) {
				throw new GeneralException(file.getOriginalFilename()+": Archivo ya existente, actualice la lista ");
			}

			Files.copy(file.getInputStream(), Paths.get(getUserMusicPath()+file.getOriginalFilename()));
		} catch (IOException e) {
			throw new GeneralException(e.getMessage());
		}
	
	}

	@Override
	public Resource getFile(String name) {
		 try {
	            Resource resource = new InputStreamResource(new FileInputStream(new File(getUserMusicPath()+name)));//UrlResource(getUserMusicPath()+name);
	            if(resource.exists()) {
	                return resource;
	            }
	            else {
	                throw new GeneralException("No se pudo leer el fichero: " + name);

	            }
	        } catch (Exception e) {
	        	
	            throw new GeneralException("No se pudo leer el fichero: " + name + "["+e.getMessage()+"]");
	        }
	}

	@Override
	public boolean deleteFile(String name) {
		File file = new File(getUserMusicPath()+name);
		if(!file.exists()) {
			return false;
		}
		return file.delete();
	}

}
