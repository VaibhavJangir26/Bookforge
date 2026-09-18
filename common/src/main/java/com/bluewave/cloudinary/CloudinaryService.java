package com.bluewave.cloudinary;

import com.bluewave.dto.CloudinaryResponseDTO;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryResponseDTO uploadImage(MultipartFile file, String folderName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file cannot be empty or null");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "bluewave/" + folderName,
                    "resource_type", "auto"
            ));

            return CloudinaryResponseDTO.builder()
                    .publicId((String) uploadResult.get("public_id"))
                    .secureUrl((String) uploadResult.get("secure_url"))
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("failed to upload image to cloudinary", e);
        }
    }

    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("failed to delete image from cloudinary" + publicId, e);
        }
    }

    public CloudinaryResponseDTO updateImage(MultipartFile newFile, String folderName, String oldPublicId) {
        if (oldPublicId != null && !oldPublicId.isBlank()) {
            deleteImage(oldPublicId);
        }
        return uploadImage(newFile, folderName);
    }
}