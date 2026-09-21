package kr.or.oti.project.util;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileStorageUtil {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String savedName = UUID.randomUUID().toString() + "_" + originalName;

        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            log.error("업로드 디렉터리 생성 실패 - uploadDir={}", uploadDir);
            throw new IllegalStateException("첨부파일 저장 경로를 만들 수 없습니다.");
        }

        File dest = new File(dir, savedName);
        try {
            file.transferTo(dest);
            log.info("첨부파일 저장 완료 - originalName={}, savedName={}, size={}bytes",
                    originalName, savedName, file.getSize());
            return savedName;
        } catch (IOException e) {
            log.error("첨부파일 저장 실패 - originalName={}", originalName, e);
            throw new RuntimeException("첨부파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    public void deleteFile(String savedName) {
        if (savedName == null || savedName.isBlank()) {
            return;
        }

        File file = new File(uploadDir, savedName);
        if (!file.exists()) {
            log.debug("삭제할 물리 파일 없음 - savedName={}", savedName);
            return;
        }
        if (!file.delete()) {
            log.warn("첨부파일 물리 삭제 실패 - savedName={}", savedName);
            return;
        }
        log.debug("첨부파일 물리 삭제 완료 - savedName={}", savedName);
    }
}

