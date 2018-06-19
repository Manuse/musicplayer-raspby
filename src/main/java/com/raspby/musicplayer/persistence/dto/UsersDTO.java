/**
 * 
 */
package com.raspby.musicplayer.persistence.dto;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Manuel
 *
 */
@Entity
@Table(name="users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsersDTO {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Short id;

	@Column(name="login")
	@NotBlank
	private String username;
	
	@Column(name="password")
	@NotBlank
	@Length(max=60)
	//@JsonIgnore
	private String password;
	
	@Column(name="enabled")
	@NotNull
	private boolean enabled;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "users_authority", joinColumns = { @JoinColumn(name = "id_user", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "id_authority", table = "authority", referencedColumnName = "id") })
	private List<AuthorityDTO> authorities = new ArrayList<AuthorityDTO>();
	
}
