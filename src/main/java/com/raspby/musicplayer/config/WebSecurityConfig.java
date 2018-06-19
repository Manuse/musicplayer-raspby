/**
 * 
 */
package com.raspby.musicplayer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.raspby.musicplayer.security.CustomRememberMeServices;
import com.raspby.musicplayer.security.CustomUserDetailsService;


/**
 * @author Manuel
 *
 */
@Configuration
@EnableGlobalMethodSecurity(securedEnabled=true, prePostEnabled=true)
@EnableWebSecurity
@ComponentScan(basePackages = {"com.raspby.musicplayer.security"})
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	public static final String REMEMBER_ME_KEY = "rememberme_key";
	
	@Autowired
	private CustomUserDetailsService userDetailsService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private CustomRememberMeServices rememberMeService;
	
	@Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**");
    }
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers()
	        	.disable()
	        .csrf().
	        	disable()     
	        .authorizeRequests()  
                .anyRequest()
                .hasAuthority("musicplayer-user")
                .and()
            .formLogin()
                .loginPage("/login")
                .failureUrl("/login-error")
                .permitAll()
                .and()
            .logout()
            	.logoutUrl("/logout")
            	.logoutSuccessUrl("/login")
            	.deleteCookies("JSESSIONID")
                .permitAll()
                .and()
            .rememberMe()
            	.rememberMeServices(rememberMeService)
            	.key(REMEMBER_ME_KEY);
    }
	
	@Autowired
	public void configurerGlobal(AuthenticationManagerBuilder build) throws Exception{	
		build.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}
	
	@Bean
	public BCryptPasswordEncoder getBCryptPasswordEncoder() {
		return new BCryptPasswordEncoder(11);
	}

}
