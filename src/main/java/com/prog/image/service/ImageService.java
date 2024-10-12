package com.prog.image.service;

import com.prog.image.model.FileMap;
import com.prog.image.model.FileObject;
import com.prog.image.model.ResponseObject;
import com.prog.image.repository.FileMapRepository;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.*;

import org.opencv.core.Mat;


@Slf4j
@Service
public class ImageService {

    private static final Map<String, String> IMAGE_TYPE_SIGNATURES = new HashMap<>();
    static {
        IMAGE_TYPE_SIGNATURES.put("FFD8FF", "jpg");
        IMAGE_TYPE_SIGNATURES.put("89504E47", "png");
        IMAGE_TYPE_SIGNATURES.put("474946383761", "gif"); // GIF87a
        IMAGE_TYPE_SIGNATURES.put("474946383961", "gif"); // GIF89a
        IMAGE_TYPE_SIGNATURES.put("424D", "bmp");
        IMAGE_TYPE_SIGNATURES.put("49492A00", "tif"); // Little-endian
        IMAGE_TYPE_SIGNATURES.put("4D4D002A", "tif"); // Big-endian
        IMAGE_TYPE_SIGNATURES.put("52494646", "webp"); // 'RIFF' followed by 'WEBP'
        IMAGE_TYPE_SIGNATURES.put("00000100", "ico");
        IMAGE_TYPE_SIGNATURES.put("66747970", "heif"); // 'ftyp'
        IMAGE_TYPE_SIGNATURES.put("6674797068656963", "heic"); // 'ftypheic'
        IMAGE_TYPE_SIGNATURES.put("3C73766720", "svg"); // '<svg '
        IMAGE_TYPE_SIGNATURES.put("38425053", "psd");
        IMAGE_TYPE_SIGNATURES.put("66747970", "avif"); // 'ftyp' followed by 'avif'
        IMAGE_TYPE_SIGNATURES.put("0000000C6A5020200D0A", "jp2");
    }

    private final FileMapRepository fileMapRepository;
    private static final String UPLOAD_DIRECTORY = "uploads";

    @Autowired
    public ImageService(FileMapRepository fileMapRepository) {
        this.fileMapRepository = fileMapRepository;

        File directory = new File(UPLOAD_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < length && i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    // this function will detect file type from file data.
    private String detectImageType(byte[] fileBytes) throws IOException {
        String hexSignature = bytesToHex(fileBytes, 16); // Check first 16 bytes

        for (Map.Entry<String, String> entry : IMAGE_TYPE_SIGNATURES.entrySet()) {
            if (hexSignature.startsWith(entry.getKey())) {
                // Special case for WEBP, HEIF, and AVIF as they share initial bytes
                if (entry.getKey().equals("52494646") && hexSignature.substring(16).startsWith("57454250")) {
                    return "webp";
                }
                if (entry.getKey().equals("66747970")) {
                    if (hexSignature.substring(8).startsWith("68656963")) {
                        return "heic";
                    } else if (hexSignature.substring(8).startsWith("61766966")) {
                        return "avif";
                    } else {
                        return "heif";
                    }
                }
                return entry.getValue();
            }
        }
        return "unknown_fmt";
    }

    private String updateFileExt(String fileNameOld, String ext) {
        if (fileNameOld == null || fileNameOld.isEmpty()) {
            return fileNameOld;
        }

        // Find the last occurrence of '.'
        int dotIndex = fileNameOld.lastIndexOf('.');

        if (dotIndex == -1) {
            // No extension found, simply append the new extension
            return fileNameOld + "." + ext;
        } else {
            // Replace the existing extension
            return fileNameOld.substring(0, dotIndex + 1) + ext;
        }
    }

    private Mat decodedBytes(byte[] bytes) throws IOException {
        Mat mat = new MatOfByte(bytes);
        if (mat.empty()) {
            throw new IOException("Error: Cant read input file in bytes.");
        }

        mat = Imgcodecs.imdecode(mat, Imgcodecs.IMREAD_COLOR);
        if (mat.empty()) {
            throw new IOException("Error: Unable to decode Input File.");
        }

        return mat;
    }

    private String removeFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }

        int lastDotIndex = filename.lastIndexOf('.');
        int lastSeparatorIndex = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));

        // If dot is present and is after the last separator
        if (lastDotIndex > lastSeparatorIndex && lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }

        return filename;
    }

    public List<ResponseObject> uploadImages(List<MultipartFile> files) throws IOException {

        List<ResponseObject> uploads = new ArrayList<>();
        for (MultipartFile file : files) {
            String fmt = detectImageType(file.getBytes());
            String fname = removeFileExtension(file.getOriginalFilename());
            Path filePath = Paths.get(UPLOAD_DIRECTORY, file.getOriginalFilename());

            if(Files.exists(filePath)){
                uploads.add(new ResponseObject(-1, fname,  "FAIL - Another file with " + file.getOriginalFilename() + " name exist. Try again, after renaming this file.", fmt));
                continue;
            }

            Files.copy(file.getInputStream(), filePath);
            FileMap dbFileObj = new FileMap(filePath.toString() , fname, fmt);
            dbFileObj = fileMapRepository.save(dbFileObj);

            uploads.add(new ResponseObject(dbFileObj.getId(), dbFileObj.getFname(), "OK", dbFileObj.getFmt()));
        }

        return uploads;
    }

    public FileObject retrieve( int id ) throws IOException {
        FileMap fileMap = fileMapRepository.findById(id).orElse(null);
        if(fileMap == null) throw new RuntimeException("File does not Exist");

        Path path = Paths.get(fileMap.getPath());
        return new FileObject(fileMap.getFname(), Files.readAllBytes(path), fileMap.getFmt());
    }

    public FileObject convert(int id, String fmt) throws IOException {
        FileMap fileMap = fileMapRepository.findById(id).orElse(null);
        if(fileMap == null) throw new RuntimeException("File does not exist");

        File file = new File(fileMap.getPath());
        if (!file.exists()) {
            fileMapRepository.delete(fileMap);
            throw new RuntimeException("File does not Exist on Disk");
        }

        Path path = Paths.get(fileMap.getPath());
        FileObject fileObject = new FileObject(fileMap.getFname(), Files.readAllBytes(path), fileMap.getFmt());

        if(fmt.equalsIgnoreCase(fileMap.getFmt())) {
            return fileObject;
        }

        Mat mat = decodedBytes(Files.readAllBytes(path));

        MatOfByte byteMat = new MatOfByte();
        boolean isConverted = Imgcodecs.imencode("." + fmt, mat, byteMat);
        if(!isConverted) throw new RuntimeException("Fail to convert image. Target format encoding not supported");

        fileObject.setData(byteMat.toArray());
        fileObject.setFmt(fmt);

        fileMap.setFmt(fmt);
        fileMap.setPath(updateFileExt(fileMap.getPath(), fmt));
        fileMap = fileMapRepository.save(fileMap);
        file.delete();

        path = Paths.get(fileMap.getPath());
        Files.write(path, byteMat.toArray());

        return fileObject;
    }

    public byte[] convert(MultipartFile file, String fmt) throws IOException {
        Mat mat = decodedBytes(file.getBytes());

        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode("." + fmt, mat, byteMat);

        return byteMat.toArray();
    }

    public List<ResponseObject> getAllFiles() throws IOException {

        List<ResponseObject> retrieveData = new ArrayList<>();
        List<FileMap> files = fileMapRepository.findAll();

        for (var file : files) {

            File fileOnDisk = new File(file.getPath());
            if (!fileOnDisk.exists()) {
                fileMapRepository.delete(file);
                continue;
            }

            ResponseObject object = new ResponseObject(file.getId(), file.getFname(), "OK", file.getFmt());
            retrieveData.add(object);
        }

        return retrieveData;
    }

    //TODO for additional feature not in current scope

    // Compression
    public boolean compressImage(Mat input, String outputPath, int quality) {
        MatOfInt params = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality);
        return Imgcodecs.imwrite(outputPath, input, params);
    }

    // Rotation
    public boolean rotateImage(UUID uuid, String outputPath, double angle) {
        return false;
        // Rotation logic goes here
        /*
        Mat input = null;
        Mat rotated = new Mat();
        Point center = new Point(input.cols() / 2.0, input.rows() / 2.0);
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        Imgproc.warpAffine(input, rotated, rotationMatrix, input.size());
        return Imgcodecs.imwrite("rotated.jpg", rotated);
        */
    }

    // Applying a filter (Gaussian Blur)
    public boolean applyFilter(Mat input, String outputPath) {
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(input, blurred, new Size(15, 15), 0);
        return Imgcodecs.imwrite(outputPath, blurred);
    }

    // Creating a thumbnail
    public boolean createThumbnail(Mat input, String outputPath, int width, int height) {
        Mat thumbnail = new Mat();
        Imgproc.resize(input, thumbnail, new Size(width, height));
        return Imgcodecs.imwrite(outputPath, thumbnail);
    }

    // Masking
    public boolean applyMask(Mat input, String outputPath) {
        Mat mask = Mat.zeros(input.size(), CvType.CV_8U);
        Point center = new Point(input.cols() / 2.0, input.rows() / 2.0);
        Scalar white = new Scalar(255, 255, 255);
        Imgproc.circle(mask, center, Math.min(input.cols(), input.rows()) / 4, white, -1);

        Mat masked = new Mat();
        Core.bitwise_and(input, input, masked, mask);
        return Imgcodecs.imwrite(outputPath, masked);
    }

}