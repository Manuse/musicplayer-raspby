/**
 * 
 */
package com.raspby.musicplayer.persistence.dto;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Manuel
 *
 */
@Entity
@Table(name="song")
@EqualsAndHashCode(callSuper=false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SongDTO extends GenericEntity{
	
	@OneToMany(mappedBy="songPlaylistDTOId.song")
	private List<SongPlaylistDTO> songPlaylist;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private UsersDTO users;

	public SongDTO(Integer id) {
		this.id=id;
	}
}
