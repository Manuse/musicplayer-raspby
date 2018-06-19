package com.raspby.musicplayer.persistence.dto;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Manuel
 *
 */
@Entity
@Table(name="song_playlist")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SongPlaylistDTO {
	
	@EmbeddedId
	private SongPlaylistDTOId songPlaylistDTOId;
	
	@Column(name="sort")
	private Integer sort;

}
