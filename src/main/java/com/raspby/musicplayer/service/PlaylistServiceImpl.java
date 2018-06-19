package com.raspby.musicplayer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raspby.musicplayer.model.Playlist;
import com.raspby.musicplayer.model.Song;
import com.raspby.musicplayer.persistence.dto.PlaylistDTO;
import com.raspby.musicplayer.persistence.dto.SongDTO;
import com.raspby.musicplayer.persistence.dto.SongPlaylistDTO;
import com.raspby.musicplayer.persistence.dto.SongPlaylistDTOId;
import com.raspby.musicplayer.persistence.repository.PlaylistRepository;
import com.raspby.musicplayer.persistence.repository.SongPlaylistRepository;
import com.raspby.musicplayer.persistence.repository.SongRepository;
import com.raspby.musicplayer.persistence.repository.UsersRepository;
import com.raspby.musicplayer.util.MapperUtils;
import com.raspby.musicplayer.util.SecurityUtils;

/**
 * 
 * @author Manuel
 *
 */
@Service
public class PlaylistServiceImpl implements PlaylistService {

	@Autowired
	private PlaylistRepository playlistRepository;

	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private SongPlaylistRepository songPlaylistRepository;
	
	@Autowired
	private SongRepository songRepository;

	@Override
	@Transactional
	public Playlist save(Playlist playlist) {
		return MapperUtils.convertDtoToModel(playlistRepository.save(MapperUtils.convertModelToDto(playlist,
				usersRepository.findByUsername(SecurityUtils.getCurrentLogin()))));
	}

	@Override
	@Transactional
	public boolean deleteById(int playlistId) {
		playlistRepository.deleteById(playlistId);
		return !playlistRepository.existsById(playlistId);
	}

	@Override
	@Transactional
	public List<Playlist> findAll() {
		return playlistRepository.findAll().stream().map(i -> MapperUtils.convertDtoToModel(i))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly=true)
	public Playlist findById(int playlistId) {
		return MapperUtils.convertDtoToModel(playlistRepository.findById(playlistId).get());
	}

	@Override
	@Transactional
	public List<Playlist> findByUser() {
		return playlistRepository.findByUsers(usersRepository.findByUsername(SecurityUtils.getCurrentLogin())).stream()
				.map(i -> MapperUtils.convertDtoToModel(i)).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void addSongPlaylist(int playlistId, int songId) {
		Long sort = songPlaylistRepository.findBySongPlaylistDTOIdPlaylistIdMaxSort(playlistId);	
		sort = sort==null ? 1L: (sort.intValue()+1);
		songPlaylistRepository.save(SongPlaylistDTO.builder()
		.songPlaylistDTOId(new SongPlaylistDTOId(songRepository.findById(songId).get(), new PlaylistDTO(playlistId)))
		.sort(sort.intValue())
		.build());
	}

	@Override
	@Transactional
	public void quitSongPlaylist(int playlistId, int songId) {
		songPlaylistRepository.deleteById((new SongPlaylistDTOId(new SongDTO(songId), new PlaylistDTO(playlistId))));
	}

	@Override
	@Transactional
	public void updateSortSong(List<Song> songs, int playlistId) {
		Map<Integer, SongPlaylistDTO> map = playlistRepository.findById(playlistId).get().getSongPlaylist()
				.stream().collect(Collectors.toMap(e -> e.getSongPlaylistDTOId().getSong().getId(), i -> i));
		List<SongPlaylistDTO> spdtoList = new ArrayList<SongPlaylistDTO>();
		songs.forEach(e -> {
			SongPlaylistDTO spdto=map.get(e.getId());
			spdto.setSort(e.getSort());
			spdtoList.add(spdto);
		});
		songPlaylistRepository.saveAll(spdtoList);
		
	}
}
