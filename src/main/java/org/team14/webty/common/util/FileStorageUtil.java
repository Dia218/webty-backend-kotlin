package org.team14.webty.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.team14.webty.common.exception.BusinessException;
import org.team14.webty.common.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FileStorageUtil {

	@Value("${upload.path}")
	private String uploadPath;

	// 절대경로 변환 메서드
	private String getAbsoluteUploadDir() {
		Path projectRoot = Paths.get("").toAbsolutePath(); // 현재 프로젝트 루트 경로 가져오기
		return projectRoot.resolve(uploadPath).normalize().toString();
	}

	public String storeImageFile(MultipartFile file) throws IOException {

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new BusinessException(ErrorCode.FILE_UPLOAD_TYPE_ERROR);
		}

		String baseUploadDir = getAbsoluteUploadDir();
		String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yy-MM-dd"));
		String uploadDir = Paths.get(baseUploadDir, dateFolder).toString(); // 날짜별 폴더 추가

		File directory = new File(uploadDir);
		if (!directory.exists() && !directory.mkdirs()) {
			log.error("디렉토리 생성 실패: {}", uploadDir);
			throw new BusinessException(ErrorCode.DIRECTORY_CREATION_FAILED);
		}

		String originalFileName = file.getOriginalFilename();
		String newFileName = UUID.randomUUID() + "--originalFileName_" + originalFileName;

		String filePath = Paths.get(uploadDir, newFileName).toString();
		file.transferTo(new File(filePath));

		log.info("파일이 저장되었습니다: {}", filePath);

		// 프론트엔드에서 접근할 수 있도록 상대 경로 반환
		return "/uploads/" + dateFolder + "/" + newFileName;
	}

	public List<String> storeImageFiles(List<MultipartFile> files) throws IOException {
		List<String> imageUrls = new ArrayList<>();
		for (MultipartFile file : files) {
			String contentType = file.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				throw new BusinessException(ErrorCode.FILE_UPLOAD_TYPE_ERROR);
			}
			String baseUploadDir = getAbsoluteUploadDir();
			String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yy-MM-dd"));
			String uploadDir = Paths.get(baseUploadDir, dateFolder).toString();// 날짜별 폴더 추가
			File directory = new File(uploadDir);
			if (!directory.exists() && !directory.mkdirs()) {
				log.error("디렉토리 생성 실패: {}", uploadDir);
				throw new BusinessException(ErrorCode.DIRECTORY_CREATION_FAILED);
			}
			String originalFileName = file.getOriginalFilename();
			String newFileName = UUID.randomUUID() + "--originalFileName_" + originalFileName;
			String filePath = Paths.get(uploadDir, newFileName).toString();
			file.transferTo(new File(filePath));
			log.info("파일이 저장되었습니다: {}", filePath);
			imageUrls.add("/uploads/" + dateFolder + "/" + newFileName);
		}
		return imageUrls;
	}

	public void deleteFile(String fileUrl) {
		try {
			String baseUploadDir = getAbsoluteUploadDir();

			// fileUrl에서 업로드 디렉토리 경로 추출 (OS 경로 호환 처리)
			Path relativePath = Paths.get(fileUrl.replace("/uploads/", ""));
			Path fullPath = Paths.get(baseUploadDir).resolve(relativePath).normalize();

			File file = fullPath.toFile();
			if (file.exists() && file.isFile()) { // 파일인지 확인 후 삭제
				if (file.delete()) {
					log.info("파일 삭제 성공: {}", fullPath);

					// 디렉토리가 비어 있으면 삭제 (날짜별 폴더 정리)
					File parentDir = file.getParentFile();
					if (parentDir.isDirectory() && parentDir.list().length == 0) {
						if (parentDir.delete()) {
							log.info("빈 디렉토리 삭제 성공: {}", parentDir.getAbsolutePath());
						} else {
							log.warn("빈 디렉토리 삭제 실패: {}", parentDir.getAbsolutePath());
						}
					}
				} else {
					throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
				}
			} else {
				log.warn("삭제할 파일이 존재하지 않거나, 파일이 아님: {}", fullPath);
			}
		} catch (Exception e) {
			log.error("파일 삭제 중 오류 발생: {}", fileUrl, e);
			throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
		}
	}

}
