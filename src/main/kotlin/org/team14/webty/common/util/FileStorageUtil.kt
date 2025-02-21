package org.team14.webty.common.util

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.outputStream

@Component
class FileStorageUtil(
    @Value("\${upload.path}") private val uploadPath: String
) {
    private val log = LoggerFactory.getLogger(FileStorageUtil::class.java)

    fun getAbsoluteUploadDir(): Path = Path.of("").toAbsolutePath().resolve(uploadPath).normalize()

    fun storeImageFile(file: MultipartFile): String = runCatching {
        require(file.contentType?.startsWith("image/") == true) {
            throw BusinessException(ErrorCode.FILE_UPLOAD_TYPE_ERROR)
        }

        val uploadDir = getAbsoluteUploadDir().resolve(getDateFolder()).apply {
            if (!exists()) createDirectories()
        }

        val newFileName = "${UUID.randomUUID()}--${file.originalFilename ?: "unknown_file"}"
        val filePath = uploadDir.resolve(newFileName)

        file.inputStream.use { input -> filePath.outputStream().use { input.copyTo(it) } }

        log.info("파일이 저장되었습니다: {}", filePath)
        "/uploads/${getDateFolder()}/$newFileName"
    }.onFailure {
        log.error("파일 업로드 실패", it)
        throw BusinessException(ErrorCode.FILE_UPLOAD_TYPE_ERROR)
    }.getOrThrow()

    fun storeImageFiles(files: List<MultipartFile>): List<String> =
        files.map { storeImageFile(it) }

    fun deleteFile(fileUrl: String) = runCatching {
        val fullPath = getAbsoluteUploadDir().resolve(fileUrl.removePrefix("/uploads/")).normalize()

        if (fullPath.exists()) {
            fullPath.deleteIfExists()
            log.info("파일 삭제 성공: {}", fullPath)

            fullPath.parent.takeIf { it.exists() && it.toFile().list()?.isEmpty() == true }
                ?.deleteIfExists()
        } else {
            log.warn("삭제할 파일이 존재하지 않음: {}", fullPath)
        }
    }.onFailure {
        log.error("파일 삭제 중 오류 발생: {}", fileUrl, it)
        throw BusinessException(ErrorCode.FILE_DELETE_FAILED)
    }

    // 날짜별 폴더 생성
    private fun getDateFolder(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("yy-MM-dd"))
}