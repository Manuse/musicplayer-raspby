/**
 * 
 */
package com.raspby.musicplayer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.raspby.musicplayer.persistence.dto.UsersDTO;

/**
 * @author Manuel
 *
 */
public interface UsersRepository extends JpaRepository<UsersDTO, Short>{
	
	UsersDTO findByUsername(String username);

}
