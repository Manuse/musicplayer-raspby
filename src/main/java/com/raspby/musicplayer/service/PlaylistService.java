package com.raspby.musicplayer.service;

import java.util.List;

import com.raspby.musicplayer.model.Playlist;
import com.raspby.musicplayer.model.Song;

/**
 * 
 * @author Manuel
 *
 */
public interface PlaylistService {

	public Playlist save(Playlist playlist);
	
	public boolean deleteById(int playlistId);
	
	public List<Playlist> findAll();
	
	public Playlist findById(int playlistId);
	
	public List<Playlist> findByUser();
	
	public void addSongPlaylist(int playlistId, int songId);
	
	public void quitSongPlaylist(int playlistId, int songId);
	
	public void updateSortSong(List<Song> songs, int playlistId);
	
}
