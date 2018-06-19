package com.raspby.musicplayer.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raspby.musicplayer.persistence.dto.SongDTO;
import com.raspby.musicplayer.persistence.dto.UsersDTO;

/**
 * 
 * @author Manuel
 *
 */
public interface SongRepository extends JpaRepository<SongDTO, Integer>{
	
	public List<SongDTO> findByUsers(UsersDTO users);
	
	public SongDTO findByUsersAndName(UsersDTO users, String name);
	
	@Modifying
	@Query("delete from SongDTO where users.id = :userId and name like :name")
	public void deleteByUserAndName(@Param("userId") Short user, @Param("name") String name);
	
	@Query(value="select * from song where user_id=:userId and id not in (select song_id from song_playlist where playlist_id = :playlistId)", nativeQuery=true)
	public List<SongDTO> findByUsersAndNotInPlaylist(@Param("userId") Short usersId, @Param("playlistId") Integer playlistId);

}
