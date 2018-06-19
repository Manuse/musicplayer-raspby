/**
 * 
 */
package com.raspby.musicplayer.model;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.sampled.file.TAudioFileFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Manuel
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Song {
	
	private Integer id;
	
	private String name;
	
	private String duration;
	
	private Integer sort;
	
	private boolean status;
	
	public void setDuration(File file) throws UnsupportedAudioFileException, IOException {
		 AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
		if (fileFormat instanceof TAudioFileFormat) {
	        Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
	        String key = "duration";
	        Long microseconds = (Long) properties.get(key);
	        int mili = (int) (microseconds / 1000);
	        int sec = (mili / 1000) % 60;
	        int min = (mili / 1000) / 60;
	        duration=min+":"+(sec<10 ? "0"+sec:sec);
	    } else {
	        throw new UnsupportedAudioFileException();
	    }
	}
	
	public void setDuration(String duration) {
		this.duration = duration;
	}

}
