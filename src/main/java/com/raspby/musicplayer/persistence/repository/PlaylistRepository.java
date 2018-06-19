/**
 * 
 */
package com.raspby.musicplayer.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raspby.musicplayer.persistence.dto.PlaylistDTO;
import com.raspby.musicplayer.persistence.dto.UsersDTO;

/**
 * @author Manuel
 *
 */
public interface PlaylistRepository extends JpaRepository<PlaylistDTO, Integer>{
	
	public List<PlaylistDTO> findByUsers(UsersDTO users);

}
