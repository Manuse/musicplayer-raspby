/**
 * 
 */
package com.raspby.musicplayer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raspby.musicplayer.persistence.dto.SongPlaylistDTO;
import com.raspby.musicplayer.persistence.dto.SongPlaylistDTOId;

/**
 * @author Manuel
 *
 */
public interface SongPlaylistRepository extends JpaRepository<SongPlaylistDTO, SongPlaylistDTOId>{

	@Query("select max(sort) from SongPlaylistDTO where songPlaylistDTOId.playlist.id = :playlistId")
	public Long findBySongPlaylistDTOIdPlaylistIdMaxSort(@Param("playlistId") Integer playlistId);
}
