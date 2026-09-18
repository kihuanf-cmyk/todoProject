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

    /**
     * MultipartFile을 디스크에 저장하고, 저장된 파일명을 반환
     * 원본 파일명 충돌을 피하기 위해 UUID를 접두어로 붙여서 저장
     */
    public String storeFile(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String savedName = UUID.randomUUID().toString() + "_" + originalName;

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 업로드 디렉토리가 없으면 생성
        }

        File dest = new File(dir, savedName);
        try {
            file.transferTo(dest);
            log.info("파일 저장 완료 - originalName={}, savedName={}, size={}bytes",
                    originalName, savedName, file.getSize());
        } catch (IOException e) {
            log.error("파일 저장 실패 - fileName={}", originalName, e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }

        return savedName; // DB의 file_url 컬럼에 저장할 값
    }
}