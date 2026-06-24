package ao.angogas.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String upload(MultipartFile file, String folder);
}
