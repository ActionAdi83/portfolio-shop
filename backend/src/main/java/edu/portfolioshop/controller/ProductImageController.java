package edu.portfolioshop.controller;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Product photo storage — GridFS, the same mechanism fanvote's PictureController
 * uses, minus the moderation/thumbnail machinery a shop demo doesn't need.
 *
 * Upload is a POST under /api/admin/**, so SecurityConfig already restricts it
 * to shop-admin. Serving is grouped under /api/products/** so it rides the same
 * "browsing the catalogue is public" rule as everything else there.
 */
@RestController
@RequiredArgsConstructor
public class ProductImageController {

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    @PostMapping("/api/admin/products/images")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (file.isEmpty() || contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only image files are accepted"));
        }

        ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), contentType);
        // Stored as a path relative to the API, the same shape fanvote uses for
        // pictures — the frontend resolves it against its own base URL.
        String url = "products/images/" + id.toHexString();
        return ResponseEntity.ok(Map.of("id", id.toHexString(), "url", url));
    }

    @GetMapping("/api/products/images/{id}")
    public ResponseEntity<byte[]> get(@PathVariable String id) throws IOException {
        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(id))));
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        GridFsResource resource = gridFsOperations.getResource(file);
        String contentType = resource.getContentType() != null ? resource.getContentType() : MediaType.IMAGE_JPEG_VALUE;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // Content-addressed by id, so it can be cached hard.
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .body(resource.getInputStream().readAllBytes());
    }
}
