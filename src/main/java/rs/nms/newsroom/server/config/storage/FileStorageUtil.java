package rs.nms.newsroom.server.config.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Utility component for handling file storage operations such as saving,
 * retrieving, and deleting files on the server filesystem.
 * <p>
 * Used for user profile images or other file upload features.
 */
@Component
public class FileStorageUtil {

    private final Path fileStorageLocation;
    private final String relativeUrlBase = "/profile-images/";

    /**
     * Initializes the file storage utility and ensures the upload directory exists.
     *
     * @param fileStorageProperties properties containing the upload directory path
     * @throws IOException if the directory cannot be created
     */
    @Autowired
    public FileStorageUtil(FileStorageProperties fileStorageProperties) throws IOException {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new IOException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Stores the uploaded file with a unique name and returns its relative URL.
     *
     * @param file the file to store
     * @return the relative URL for accessing the stored file
     * @throws IOException if storage fails
     */
    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new IOException("Cannot store file with relative path outside current directory: " + originalFileName);
        }

        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID() + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return relativeUrlBase + uniqueFileName;
    }

    /**
     * Deletes the specified file from the storage location.
     *
     * @param filePath the relative or absolute path to the file
     * @throws IOException if deletion fails
     */
    public void deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        String fileName = Paths.get(filePath).getFileName().toString();
        Path absolutePath = this.fileStorageLocation.resolve(fileName);

        if (!Files.exists(absolutePath)) {
            return;
        }

        try {
            Files.delete(absolutePath);
        } catch (IOException ex) {
            throw new IOException("Could not delete file: " + absolutePath, ex);
        }
    }

    /**
     * Returns the absolute path to the file storage location.
     *
     * @return the file storage directory as a Path
     */
    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}