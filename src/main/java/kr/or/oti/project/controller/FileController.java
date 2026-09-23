package kr.or.oti.project.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 디스크에 저장된 파일을 읽어서 브라우저로 응답 (이미지는 바로 보여지고, 그 외는 다운로드됨)
    @GetMapping("/files/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) {
        File file = new File(uploadDir, fileName);

        // Path Traversal 방어: "../" 등으로 업로드 디렉터리 밖에 접근하는 요청 차단
        try {
            String canonicalUploadDir = new File(uploadDir).getCanonicalPath();
            String canonicalFilePath  = file.getCanonicalPath();
            if (!canonicalFilePath.startsWith(canonicalUploadDir + File.separator)) {
                log.warn("경로 조작 공격 감지 - fileName={}", fileName);
                return ResponseEntity.badRequest().build();
            }
        } catch (IOException e) {
            log.error("파일 경로 검증 실패 - fileName={}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }

        if (!file.exists()) {
            log.warn("존재하지 않는 파일 요청 - fileName={}", fileName);
            return ResponseEntity.notFound().build();
        }
        log.debug("파일 조회 요청 - fileName={}", fileName);

        Resource resource;
        try {
            resource = new FileSystemResource(file);
        } catch (Exception e) {
            log.error("파일 로드 실패 - fileName={}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }

        log.info("파일 응답 완료 - fileName={}, size={}bytes", fileName, file.length());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
