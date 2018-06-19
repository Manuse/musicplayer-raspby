package com.raspby.musicplayer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.raspby.musicplayer.util.ConstantViews.LOGIN;

/**
 * 
 * @author Manuel
 *
 */
@Controller
public class LoginController {
	
	private static Logger logger=LoggerFactory.getLogger(LoginController.class);
	
	@RequestMapping("/login")
	public String login(Model model) {
		logger.info("carga del login");
		return LOGIN;
	}
	
	@RequestMapping("/login-error")
	public String loginError(Model model) {
		model.addAttribute("error", "Usuario o contraseña incorrecto");
		return LOGIN;
	}

}
