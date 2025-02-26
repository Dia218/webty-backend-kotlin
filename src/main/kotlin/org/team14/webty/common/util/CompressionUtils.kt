package org.team14.webty.common.util

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater
import org.slf4j.LoggerFactory

/**
 * 문자열 압축 및 해제를 위한 유틸리티 클래스입니다.
 */
object CompressionUtils {
    private val log = LoggerFactory.getLogger(CompressionUtils::class.java)
    private const val COMPRESSION_PREFIX = "COMP:"
    
    /**
     * 문자열이 압축되었는지 확인합니다.
     */
    fun isCompressed(data: String): Boolean {
        return data.startsWith(COMPRESSION_PREFIX)
    }

    /**
     * 문자열을 압축합니다.
     */
    fun compress(data: String): String {
        try {
            val input = data.toByteArray(Charsets.UTF_8)
            val deflater = Deflater()
            deflater.setInput(input)
            deflater.finish()

            val outputStream = ByteArrayOutputStream(input.size)
            val buffer = ByteArray(1024)
            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            deflater.end()
            outputStream.close()

            // 압축된 데이터임을 표시하는 접두어 추가
            return "$COMPRESSION_PREFIX${outputStream.toByteArray().toString(Charsets.ISO_8859_1)}"
        } catch (e: Exception) {
            log.error("문자열 압축 중 오류 발생: ${e.message}", e)
            return data // 압축 실패 시 원본 반환
        }
    }

    /**
     * 압축된 문자열을 해제합니다.
     */
    fun decompress(compressedData: String): String {
        try {
            // 접두어 제거
            val actualData = compressedData.substring(COMPRESSION_PREFIX.length)
            val input = actualData.toByteArray(Charsets.ISO_8859_1)
            
            val inflater = Inflater()
            inflater.setInput(input)
            
            val outputStream = ByteArrayOutputStream(input.size * 2)
            val buffer = ByteArray(1024)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            inflater.end()
            outputStream.close()
            
            return outputStream.toByteArray().toString(Charsets.UTF_8)
        } catch (e: Exception) {
            log.error("문자열 압축 해제 중 오류 발생: ${e.message}", e)
            return compressedData // 압축 해제 실패 시 원본 반환
        }
    }
} 