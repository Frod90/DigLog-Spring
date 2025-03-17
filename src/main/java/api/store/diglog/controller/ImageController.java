package api.store.diglog.controller;

import api.store.diglog.model.dto.image.ImageRequest;
import api.store.diglog.model.dto.image.ImageUrlResponse;
import api.store.diglog.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImageUrlResponse> uploadImage(ImageRequest imageRequest) {
        ImageUrlResponse imageUrlResponse = imageService.uploadImage(imageRequest);

        return ResponseEntity.ok().body(imageUrlResponse);
    }
}
