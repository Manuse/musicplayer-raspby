/**
 * 
 */
package com.raspby.musicplayer.controller;

import static com.raspby.musicplayer.util.ConstantViews.INDEX;
import static com.raspby.musicplayer.util.ConstantViews.LIST_SONGS_FRAG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.raspby.musicplayer.exception.GeneralException;
import com.raspby.musicplayer.model.Playlist;
import com.raspby.musicplayer.model.Song;
import com.raspby.musicplayer.service.FileService;
import com.raspby.musicplayer.service.PlaylistService;
import com.raspby.musicplayer.service.SongService;
import com.raspby.musicplayer.util.SecurityUtils;

/**
 * @author Manuel
 *
 */
@Controller
public class IndexController {
	
	@Autowired
	private PlaylistService playlistService;
	
	@Autowired
	private SongService songService;
	
	@Autowired
	private FileService fileService;
	
	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("user", SecurityUtils.getCurrentLogin());
		model.addAttribute("playlists", playlistService.findByUser());
		model.addAttribute("songs", songService.findByUser());
		return INDEX;
	}
	
	@GetMapping("/playlist/{playlistId}/songs")
	public String getSongsFromPlaylist(@PathVariable("playlistId") int playlistId, Model model) {
		List<Song> songs;
		boolean isPlaylist;
		if(playlistId<1) {
			songs = songService.findByUser();
			isPlaylist = false;
		} else {
			songs =  playlistService.findById(playlistId).getSongs();
			isPlaylist = true;
		}
		model.addAttribute("songs", songs);
		model.addAttribute("isPlaylist", isPlaylist);
		return LIST_SONGS_FRAG;
	}
	
	@GetMapping("/playlist/{playlistId}/not-songs")
	public @ResponseBody List<Song> findSongNotInPlaylist(@PathVariable("playlistId") int playlistId){
		return songService.findByUserAndNotInPlaylist(playlistId);
	}
	
	@PostMapping("/playlist/{playlistId}/add-song")
	public String addSongToPlaylist(@PathVariable("playlistId") int playlistId,@RequestParam("song") int songId, Model model) {
		playlistService.addSongPlaylist(playlistId, songId);
		return fragSongPlaylist(playlistId, model);
		
	}
	
	@PostMapping("/playlist/{playlistId}/quit-song")
	public String quitSongPlaylist(@PathVariable("playlistId") int playlistId,@RequestParam("song") int songId, Model model) {
		playlistService.quitSongPlaylist(playlistId, songId);
		return fragSongPlaylist(playlistId, model);
	}
	
	@GetMapping("/song/updateSongList")
	public String indexer(Model model) {

		songService.indexer();	
		model.addAttribute("songs", songService.findByUser());
		
		return LIST_SONGS_FRAG;
		
	}
	
	@PostMapping("/playlist/addPlaylist")
	public @ResponseBody Playlist addPlaylist(@RequestBody Playlist playlist) {
		return playlistService.save(playlist);
	}
	
	@GetMapping("song/{songId}/delete-song")
	public String deleteSong(@PathVariable("songId") int songId, RedirectAttributes redirectAttributes) {
		boolean error = false;
		String message = null;
		try {
			if(!fileService.deleteFile(songService.findById(songId).getName())) {
				error = true;
				message = "Error al borrar el archivo";
			} else {
				songService.deleteById(songId);
				error = false;
				message = "Archivo borrado con exito";
			}
		}catch(Exception e) {
			error = true;
			message = "Ha ocurrido un error: "+e.getMessage();
		}
		redirectAttributes.addFlashAttribute("error", error);
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/";
	}
	
	@PostMapping("playlist/{playlistId}/sort-list")
	public String sortListSong(@RequestBody Song[] songs, @PathVariable("playlistId") int playlistId, Model model) {
		playlistService.updateSortSong(Arrays.asList(songs), playlistId);
		return getSongsFromPlaylist(playlistId, model);
	}
	
	@RequestMapping(value = "song/upload-multiple-Song", method = RequestMethod.POST, headers = "Content-type=multipart/form-data")
	public String uploadMultipleSong(@RequestParam("file") MultipartFile[] files, RedirectAttributes redirectAttributes) {
		
		String message = "";
		boolean fileSave = true;
		
		if (files.length < 1) {
			fileSave = false;
			message = "No hay ningun archivo";
		}
		
		if (fileSave) {
			int notSave = 0;
			for (MultipartFile file : files) {
				try {

					fileService.saveFile(file);
					songService.save(Song.builder().name(file.getOriginalFilename()).build());

				} catch (GeneralException e) {
					notSave++;
					fileSave = false;
					message += e.getMessage()+"<p/>";
				}
			}
			
			if(notSave > 0) {
				message = "No se han guardado "+notSave+" archivos:<p/> "+message;
			}
		}
		
		redirectAttributes.addFlashAttribute("message", message);
		redirectAttributes.addFlashAttribute("fileSave", fileSave);

		return "redirect:/";
	}
	
	@GetMapping("/files/{filenameId}")
	public @ResponseBody ResponseEntity<byte[]> serveFile(HttpServletRequest request,@PathVariable("filenameId") int filenameId) {

		String filename = songService.findById(filenameId).getName();
		ServletContext context = request.getServletContext();
		File  file = new File(fileService.getUserMusicPath()+filename);
		String mimeType = context.getMimeType(file.getAbsolutePath());
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		HttpHeaders headers = new HttpHeaders();
		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods", "GET, POST, PUT");
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		headers.add("Content-Disposition", "filename=" + filename);
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new GeneralException(e.getMessage());
		}
	
		byte[] content=null;
		try {
			content=IOUtils.toByteArray(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		headers.setContentLength(file.length());
		return ResponseEntity.ok().headers(headers).contentLength(file.length())
				.contentType(MediaType.parseMediaType(mimeType)).body(content);
				
	}
	
	private String fragSongPlaylist(int playlistId, Model model) {
		model.addAttribute("isPlaylist", true);
		model.addAttribute("songs", playlistService.findById(playlistId).getSongs());
		return LIST_SONGS_FRAG;
	}

}
