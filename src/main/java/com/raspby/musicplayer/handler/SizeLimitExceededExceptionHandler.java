package com.raspby.musicplayer.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 
 * @author Manuel
 *
 */
@ControllerAdvice
public class SizeLimitExceededExceptionHandler {
	
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public String fileAlreadyExistsException(MaxUploadSizeExceededException ex, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("message", "Exceso en el tamaño de subida(max 100Mb simultaneos) ");
		redirectAttributes.addFlashAttribute("fileSave", false);
		return "redirect:/";
	}
	
	@ExceptionHandler(MultipartException.class)
    public String handleError1(MultipartException e, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("message", e.getCause().getMessage());
        return "redirect:/";

    }

}
