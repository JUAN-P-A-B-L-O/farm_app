package com.jpsoftware.farmapp.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated response payload.")
public class PaginatedResponse<T> {

    @Schema(description = "Current page content.")
    private List<T> content;

    @Schema(description = "Current page index.", example = "0")
    private int page;

    @Schema(description = "Requested page size.", example = "10")
    private int size;

    @Schema(description = "Total number of elements.", example = "25")
    private long totalElements;

    @Schema(description = "Total number of pages.", example = "3")
    private int totalPages;

    public PaginatedResponse() {
    }

    public PaginatedResponse(List<T> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
