package com.raspby.musicplayer.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Manuel
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {

	private Integer id;
	
	private String name;
	
	private List<Song> songs;
	
}
