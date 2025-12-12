package com.example.demo.dto;

public record UploadResponseDto(
        int totalRows,
        int successCount,
        int failureCount) {
}
