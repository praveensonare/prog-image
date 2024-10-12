package com.prog.image;

import com.prog.image.config.OpenCVConfig;
import lombok.extern.slf4j.Slf4j;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Component
@Slf4j
public class OpenCVLoader {

    public OpenCVConfig openCVConfig;

    @Autowired
    OpenCVLoader(OpenCVConfig openCVConfig){
        this.openCVConfig = openCVConfig;
    }

    @PostConstruct
    public void loadOpenCVLibrary() {
        try {
            // Load the OpenCV native library from the resources folder
            String libName = System.mapLibraryName(openCVConfig.getFileName());
            InputStream libInputStream = getClass().getClassLoader().getResourceAsStream(libName);

            if (libInputStream == null) {
                throw new IllegalStateException("OpenCV library not found in resources folder");
            }

            // Copy the library to a temporary location
            File tempLibFile = File.createTempFile("lib", libName);
            tempLibFile.deleteOnExit();
            Files.copy(libInputStream, tempLibFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Load the native library from the temporary file
            System.load(tempLibFile.getAbsolutePath());

            log.info("OpenCV library loaded successfully from resources!");
            String[] extensions = {".jpg", ".jpeg", ".png", ".bmp", ".tiff", ".tif",
                    ".webp", ".ppm", ".pgm", ".pbm", ".sr", ".ras",
                    ".jp2", ".exr", ".hdr"};

            for (String ext : extensions) {
                boolean supported = Imgcodecs.haveImageWriter(ext);
                log.info("{} format is {}",ext,supported ? "supported" : "not supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load OpenCV library", e);
        }
    }
}
