package ao.angogas.backend.service.impl;

import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/webp"};

    @Override
    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Ficheiro vazio");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException("Imagem demasiado grande. Máximo 5 MB.");
        }
        String type = file.getContentType();
        boolean allowed = false;
        for (String t : ALLOWED_TYPES) {
            if (t.equals(type)) { allowed = true; break; }
        }
        if (!allowed) {
            throw new BusinessException("Formato inválido. Aceite: JPEG, PNG, WebP.");
        }

        try {
            @SuppressWarnings("rawtypes")
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "angogas/" + folder,
                            "resource_type", "image",
                            "transformation", "q_auto,f_auto,w_800,h_800,c_limit"
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new BusinessException("Erro ao fazer upload da imagem. Tenta novamente.");
        }
    }
}
