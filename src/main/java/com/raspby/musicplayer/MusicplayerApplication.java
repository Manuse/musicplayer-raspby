package com.raspby.musicplayer;

import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.raspby.musicplayer.persistence.repository.AuthorityRepository;
import com.raspby.musicplayer.persistence.repository.UsersRepository;

@SpringBootApplication
public class MusicplayerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusicplayerApplication.class, args);
	}
	
	//@Bean
	public CommandLineRunner demoRepository(UsersRepository usersRepository, AuthorityRepository authorityRepository,
			PasswordEncoder passwordEncoder) {
		return (args) -> {
			
			
//
//			if (!usersRepository.existsById((short) 1)) {
//				List<AuthorityDTO> ls = authorityRepository.findAll();
//				usersRepository.save(new UsersDTO(null, "manu1", passwordEncoder.encode("manu1"), true, ls));
//			}
//			for (UsersDTO u : usersRepository.findAll()) {
//				//System.err.println(u);
//			}

		};

	}
	
	@Bean
	public ModelMapper getModelMapper() {
		ModelMapper modelMapper=new ModelMapper();
		return modelMapper;
	}
}
