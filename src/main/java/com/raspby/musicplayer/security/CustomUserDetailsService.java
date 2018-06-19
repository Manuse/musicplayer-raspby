package com.raspby.musicplayer.security;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raspby.musicplayer.exception.GeneralException;
import com.raspby.musicplayer.persistence.dto.UsersDTO;
import com.raspby.musicplayer.persistence.repository.UsersRepository;

/**
 * 
 * @author Manuel
 *
 */
@Service
public class CustomUserDetailsService implements UserDetailsService{

	@Autowired
    private UsersRepository usersRepository;
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		UsersDTO user = usersRepository.findByUsername(username);
		
        if (user == null) {
            throw new UsernameNotFoundException("User " + username + " was not found in the database");
        } else if (!user.isEnabled()) {
           throw new GeneralException("User " + username + " was not enabled");
        }

        Collection<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
        																	.filter(u -> u.getName().contains("musicplayer"))
        																	.map(u -> new SimpleGrantedAuthority(u.getName()))
        																	.collect(Collectors.toList());
        return new User(username, user.getPassword(),user.isEnabled(), true, true, true, grantedAuthorities);
	}

}
