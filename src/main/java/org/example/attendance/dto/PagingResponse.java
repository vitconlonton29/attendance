package org.example.attendance.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PagingResponse<T> {
    private T content;
    private PaginationMetadata pagination;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PaginationMetadata {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
    }

    public static <T> PagingResponse<T> of(T content, int page, int size, long totalElements, int totalPages) {
        PaginationMetadata pagination = new PaginationMetadata(
                page,
                size,
                totalElements,
                totalPages,
                page < totalPages - 1,
                page > 0
        );
        return new PagingResponse<>(content, pagination);
    }
}
