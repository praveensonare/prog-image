package com.prog.image.controller;

import com.prog.image.model.FileObject;
import com.prog.image.model.ResponseObject;
import com.prog.image.service.ImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * REST Controller for handling image-related operations.
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {

	private final ImageService imageService;

	/**
	 * Constructor for ImageController.
	 * @param imageService The ImageService to be used for image operations.
	 * @throws IOException If there's an error initializing the controller.
	 */
	public ImageController(ImageService imageService) throws IOException {
		this.imageService = imageService;
	}

	/**
	 * Determines the appropriate MediaType based on the file format.
	 * @param fmt The file format (e.g., "png", "jpg", "gif").
	 * @return The corresponding MediaType.
	 */
	public MediaType getMediaTypeForFormat(String fmt) {
		switch (fmt.toLowerCase()) {
			case "png":
				return MediaType.IMAGE_PNG;
			case "jpg":
			case "jpeg":
				return MediaType.IMAGE_JPEG;
			case "gif":
				return MediaType.IMAGE_GIF;
			// Add more cases as needed
			default:
				return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	/**
	 * Handles image upload requests.
	 * @param files List of MultipartFile objects representing the images to be uploaded.
	 * @return ResponseEntity containing a list of ResponseObjects with upload results.
	 */
	@CrossOrigin
	@PostMapping("/upload")
	public ResponseEntity<List<ResponseObject>> uploadImage(@RequestParam("files") List<MultipartFile> files) {
		try {
			if (files.isEmpty()) {
				return ResponseEntity.badRequest().body(new ArrayList<>());
			}

			return ResponseEntity.ok(imageService.uploadImages(files));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(new ArrayList<>());
		}
	}

	/**
	 * Retrieves an image by its ID.
	 * @param id The ID of the image to retrieve.
	 * @return ResponseEntity containing the image data and appropriate headers.
	 */
	@CrossOrigin
	@GetMapping("/retrieve")
	public ResponseEntity<byte[]> retrieve(@RequestParam int id) {
		try {
			FileObject object = imageService.retrieve(id);
			byte[] bytes = object.getData();

			MediaType mediaType = getMediaTypeForFormat(object.getFmt());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(mediaType);
			headers.setContentDispositionFormData("attachment", "retrieve_" + object.getFileName() + "." + object.getFmt());

			return ResponseEntity
					.ok()
					.headers(headers)
					.body(bytes);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(e.getMessage().getBytes());
		}
	}

	/**
	 * Converts an image to a specified format.
	 * @param id The ID of the image to convert.
	 * @param fmt The target format for conversion.
	 * @return ResponseEntity containing the converted image data and appropriate headers.
	 */
	@CrossOrigin
	@GetMapping("/convert-image")
	public ResponseEntity<byte[]> convertImage(@RequestParam int id, @RequestParam String fmt) {
		try {
			FileObject object = imageService.convert(id, fmt);
			byte[] bytes = object.getData();

			MediaType mediaType = getMediaTypeForFormat(object.getFmt());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(mediaType);
			headers.setContentDispositionFormData("attachment", "retrieve_" + object.getFileName() + "." + object.getFmt());

			return ResponseEntity
					.ok()
					.headers(headers)
					.body(bytes);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(e.getMessage().getBytes());
		}
	}

	/**
	 * Converts an uploaded file to a specified format without persisting it.
	 * @param file The MultipartFile to be converted.
	 * @param fmt The target format for conversion.
	 * @return ResponseEntity containing the converted file data and appropriate headers.
	 */
	@CrossOrigin
	@GetMapping("/convert-file")
	public ResponseEntity<byte[]> convertFile(@RequestParam("file") MultipartFile file, @RequestParam String fmt) {
		try {
			byte[] convertedImage = imageService.convert(file, fmt);

			MediaType mediaType = getMediaTypeForFormat(fmt);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(mediaType);
			headers.setContentDispositionFormData("attachment", "converted_image." + fmt);

			return ResponseEntity
					.ok()
					.headers(headers)
					.body(convertedImage);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(e.getMessage().getBytes());
		}
	}

	/**
	 * Retrieves information about all files stored in the database.
	 * @return ResponseEntity containing a list of ResponseObjects with file information.
	 */
	@GetMapping("/get-all")
	public ResponseEntity<List<ResponseObject>> getAllImages() {
		try {
			return ResponseEntity.ok(imageService.getAllFiles());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(null);
		}
	}
}