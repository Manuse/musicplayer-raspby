package com.raspby.musicplayer.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 
 * @author Manuel
 *
 */
public class SecurityUtils {

	/**
	 * Get the login of the current user.
	 * 
	 */
	public static String getCurrentLogin() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		UserDetails springSecurityUser = null;
		String userName = null;

		if (authentication != null) {
			if (authentication.getPrincipal() instanceof UserDetails) {
				springSecurityUser = (UserDetails) authentication.getPrincipal();
				userName = springSecurityUser.getUsername();
			} else if (authentication.getPrincipal() instanceof String) {
				userName = (String) authentication.getPrincipal();
			}
		}

		return userName;
	}
}
