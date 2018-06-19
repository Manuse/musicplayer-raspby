package com.raspby.musicplayer.service;

import java.util.List;

import com.raspby.musicplayer.model.Song;

public interface SongService {
	
	public Song save(Song song);
	
	public List<Song> findByUser();
	
	public boolean deleteById(int id);
	
	public Song findById(int id);
	
	public void indexer();
	
	public List<Song> findByUserAndNotInPlaylist(Integer playlistId);
	
	

}
