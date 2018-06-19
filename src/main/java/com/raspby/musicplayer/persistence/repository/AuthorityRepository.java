package com.raspby.musicplayer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raspby.musicplayer.persistence.dto.AuthorityDTO;

/**
 * 
 * @author Manuel
 *
 */
public interface AuthorityRepository extends JpaRepository<AuthorityDTO, Short>{

}
