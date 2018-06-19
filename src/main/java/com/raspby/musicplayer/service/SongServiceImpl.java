package com.raspby.musicplayer.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raspby.musicplayer.model.Song;
import com.raspby.musicplayer.persistence.dto.SongDTO;
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
public class SongServiceImpl implements SongService {

	@Autowired
	private SongRepository songRepository;

	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private FileService fileService;
	
	@Override
	public Song save(Song song) {
		return MapperUtils.convertDtoToModel(songRepository
				.save(MapperUtils.convertModelToDto(song, usersRepository.findByUsername(SecurityUtils.getCurrentLogin()))));
	}

	@Override
	public List<Song> findByUser() {
		return songRepository.findByUsers(usersRepository.findByUsername(SecurityUtils.getCurrentLogin())).stream()
				.map(s -> MapperUtils.convertDtoToModel(s)).collect(Collectors.toList());
	}

	@Override
	public boolean deleteById(int id) {
		songRepository.deleteById(id);
		return !songRepository.existsById(id);
	}

	@Override
	public Song findById(int id) {
		return MapperUtils.convertDtoToModel(songRepository.findById(id).get());
	}

	@Override
	@Transactional
	public void indexer() {
		Set<String> songFile = fileService.getListMusicFile();
		Set<String> songBd = songRepository.findByUsers(usersRepository.findByUsername(SecurityUtils.getCurrentLogin())).stream().map(SongDTO :: getName).collect(Collectors.toSet());

		if(!songBd.containsAll(songFile)) {
			songFile.forEach(o -> {
				if(!songBd.contains(o)) {
					save(Song.builder().name(o).build());
				}
				
			});
		}
		
		if(!songFile.containsAll(songBd)) {
			songBd.forEach(o -> {
				if(!songFile.contains(o)) {
					songRepository.deleteByUserAndName(usersRepository.findByUsername(SecurityUtils.getCurrentLogin()).getId(),o);
				}
			});
		}
	}

	@Override
	public List<Song> findByUserAndNotInPlaylist(Integer playlistId) {
		return songRepository.findByUsersAndNotInPlaylist( 
				usersRepository.findByUsername(SecurityUtils.getCurrentLogin()).getId(), playlistId)
				.stream()
				.map(i -> MapperUtils.convertDtoToModel(i)).collect(Collectors.toList());
	}

}
