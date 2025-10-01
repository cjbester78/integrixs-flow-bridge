package com.integrixs.backend.api.dto.response;

import java.util.List;

/**
 * Response DTO for paginated user results
 */
public class PagedUserResponse {

    private List<UserResponse> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    private boolean empty = false;

    // Default constructor
    public PagedUserResponse() {
    }

    public List<UserResponse> getContent() {
        return content;
    }

    public void setContent(List<UserResponse> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
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

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    // Builder pattern
    public static PagedUserResponseBuilder builder() {
        return new PagedUserResponseBuilder();
    }

    public static class PagedUserResponseBuilder {
        private List<UserResponse> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean empty;

        public PagedUserResponseBuilder content(List<UserResponse> content) {
            this.content = content;
            return this;
        }

        public PagedUserResponseBuilder pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public PagedUserResponseBuilder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PagedUserResponseBuilder totalElements(long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public PagedUserResponseBuilder totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public PagedUserResponseBuilder first(boolean first) {
            this.first = first;
            return this;
        }

        public PagedUserResponseBuilder last(boolean last) {
            this.last = last;
            return this;
        }

        public PagedUserResponseBuilder empty(boolean empty) {
            this.empty = empty;
            return this;
        }

        public PagedUserResponse build() {
            PagedUserResponse response = new PagedUserResponse();
            response.setContent(this.content);
            response.setPageNumber(this.pageNumber);
            response.setPageSize(this.pageSize);
            response.setTotalElements(this.totalElements);
            response.setTotalPages(this.totalPages);
            response.setFirst(this.first);
            response.setLast(this.last);
            response.setEmpty(this.empty);
            return response;
        }
    }
}
