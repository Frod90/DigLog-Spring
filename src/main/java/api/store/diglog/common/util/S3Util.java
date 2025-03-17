package api.store.diglog.common.util;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Util {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.region.static}")
    private String region;

    // 이미지를 업로드하고 이미지 url을 반환
    public String uploadImage(MultipartFile file) {
        String filePath = getFilePath(file);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filePath)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        PutObjectResponse response = s3Client.putObject(putObjectRequest, getRequestBody(file));
        if (response.sdkHttpResponse().statusCode() != 200) {
            throw new CustomException(ErrorCode.S3_IMAGE_UPLOAD_FAILED);
        }

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + filePath;
    }

    private String getFilePath(MultipartFile file) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String year = date.substring(0, 2);
        String month = date.substring(2, 4);
        String day = date.substring(4, 6);
        String folderPath = String.format("diglog/%s/%s/%s/", year, month, day);
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        return folderPath + fileName;
    }

    private RequestBody getRequestBody(MultipartFile file) {
        try {
            return RequestBody.fromBytes(file.getBytes());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.S3_WRONG_FILE);
        }
    }

    public void deleteImages(List<String> urls) {
        try {
            List<ObjectIdentifier> objectIdentifiers = urls.stream()
                    .map(url -> ObjectIdentifier.builder()
                            .key(getKey(url))
                            .build())
                    .toList();
            Delete delete = Delete.builder()
                    .objects(objectIdentifiers)
                    .build();
            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(bucket)
                    .delete(delete)
                    .build();

            if (!objectIdentifiers.isEmpty()) {
                s3Client.deleteObjects(deleteObjectsRequest);
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.S3_IMAGE_DELETE_FAILED);
        }
    }

    private String getKey(String url) {
        return url.substring(48);
    }
}
