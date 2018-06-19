/**
 * 
 */
package com.raspby.musicplayer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raspby.musicplayer.persistence.dto.TokenDTO;

/**
 * @author Manuel
 *
 */
public interface TokenRepository extends JpaRepository<TokenDTO, String>{

}
