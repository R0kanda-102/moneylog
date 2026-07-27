package com.example.moneylog.global.common;

import org.springframework.data.domain.Page;

public record PageMeta(Pagination pagination) {

    public record Pagination(
            int page,
            int size,
            long totalItems,
            int totalPages,
            boolean hasNext,
            boolean hasPrev
    ) {}

    public static PageMeta from(Page<?> page) {
        return new PageMeta(new Pagination(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        ));
    }
}