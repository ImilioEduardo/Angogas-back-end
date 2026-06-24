package ao.angogas.backend.controller;

import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "Upload de imagens")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de imagem — devolve URL pública")
    public ResponseEntity<ApiResponse<?>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "geral") String folder) {
        String url = cloudinaryService.upload(file, folder);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url), "Upload concluído"));
    }
}
