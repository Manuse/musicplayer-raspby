package com.raspby.musicplayer.util;

import java.util.stream.Collectors;
import java.io.File;
import java.util.Comparator;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.raspby.musicplayer.model.Playlist;
import com.raspby.musicplayer.model.Song;
import com.raspby.musicplayer.persistence.dto.PlaylistDTO;
import com.raspby.musicplayer.persistence.dto.SongDTO;
import com.raspby.musicplayer.persistence.dto.SongPlaylistDTO;
import com.raspby.musicplayer.persistence.dto.SongPlaylistDTOId;
import com.raspby.musicplayer.persistence.dto.UsersDTO;
import com.raspby.musicplayer.service.FileService;

/**
 * 
 * @author Manuel
 *
 */
@Component
public class MapperUtils {
	
    private static ModelMapper modelMapper;
	
	private static FileService fileService;
	
	@Autowired
	public MapperUtils(FileService fileService, ModelMapper modelMapper) {
		MapperUtils.modelMapper=modelMapper;
		MapperUtils.fileService=fileService;
	}

	public static Playlist convertDtoToModel(PlaylistDTO playlistDto) {
		List<Song> songs = playlistDto.getSongPlaylist().stream().map(i -> {
			Song e = convertDtoToModel(i.getSongPlaylistDTOId().getSong());
			
			e.setSort(i.getSort());
			
			return e;
			}).sorted(Comparator.comparing(Song :: getSort)).collect(Collectors.toList());
		Playlist p = modelMapper.map(playlistDto, Playlist.class);
		p.setSongs(songs);
		return p;
	}
	
	public static PlaylistDTO convertModelToDto(Playlist playlist, UsersDTO users) {
		PlaylistDTO p = modelMapper.map(playlist, PlaylistDTO.class);
		List<SongPlaylistDTO> spldto = playlist.getSongs().stream().map(i -> {			 
			 return new SongPlaylistDTO(SongPlaylistDTOId.builder()
					 .song(new SongDTO(i.getId()))
					 .playlist(new PlaylistDTO(playlist.getId()))
					 .build(), i.getSort());
		}).collect(Collectors.toList());
		p.setSongPlaylist(spldto);
		p.setUsers(users);
		return p;
	}
	
	public static Song convertDtoToModel(SongDTO songDto) {
		Song s = modelMapper.map(songDto, Song.class);
		s.setStatus(fileService.exists(s.getName()));
		
		try {
			s.setDuration(new File(fileService.getUserMusicPath()+s.getName()));
		}catch (Exception ex) {
			//ex.printStackTrace();
			s.setDuration("00:00");
		}
		return s;
	}
	
	public static SongDTO convertModelToDto(Song song, UsersDTO users) {
		SongDTO s = modelMapper.map(song, SongDTO.class);
		s.setUsers(users);
		return s;
	}
}
