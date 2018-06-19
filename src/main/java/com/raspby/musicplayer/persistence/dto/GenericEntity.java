/**
 * 
 */
package com.raspby.musicplayer.persistence.dto;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Manuel
 *
 */
@MappedSuperclass
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	//@GeneratedValue(strategy=GenerationType.SEQUENCE)
	protected Integer id;
	
	@Column(name="name")
	protected String name;
	
	
}
