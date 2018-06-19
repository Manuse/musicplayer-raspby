/**
 * 
 */
package com.raspby.musicplayer.persistence.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Manuel
 *
 */
@Entity
@Table(name="authority")
@Data
public class AuthorityDTO {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Short id;

	@Column(name="name")
	private String name;

	public AuthorityDTO() {}
	
	/**
	 * @param name
	 */
	public AuthorityDTO(String name) {
		this.name = name;
	}

	
}
