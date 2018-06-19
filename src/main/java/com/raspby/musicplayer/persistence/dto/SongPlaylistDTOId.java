/**
 * 
 */
package com.raspby.musicplayer.persistence.dto;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Manuel
 *
 */
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SongPlaylistDTOId implements Serializable{

	private static final long serialVersionUID = -4514139236956557879L;

	@ManyToOne
	@JoinColumn(name="song_id")
	private SongDTO song;
	
	@ManyToOne
	@JoinColumn(name="playlist_id")
	private PlaylistDTO playlist;
}
