package com.integrixs.backend.api.dto.response;

import java.util.List;

/**
 * Response object for paginated message results
 */
public class PagedMessageResponse {

    private List<MessageResponse> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    private boolean empty = false;

    // Default constructor
    public PagedMessageResponse() {
    }

    public List<MessageResponse> getContent() {
        return content;
    }

    public void setContent(List<MessageResponse> content) {
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

    public static PagedMessageResponseBuilder builder() {
        return new PagedMessageResponseBuilder();
    }

    public static class PagedMessageResponseBuilder {
        private PagedMessageResponse response = new PagedMessageResponse();

        public PagedMessageResponseBuilder content(List<MessageResponse> content) {
            response.setContent(content);
            return this;
        }

        public PagedMessageResponseBuilder pageNumber(int pageNumber) {
            response.setPageNumber(pageNumber);
            return this;
        }

        public PagedMessageResponseBuilder pageSize(int pageSize) {
            response.setPageSize(pageSize);
            return this;
        }

        public PagedMessageResponseBuilder totalElements(long totalElements) {
            response.setTotalElements(totalElements);
            return this;
        }

        public PagedMessageResponseBuilder totalPages(int totalPages) {
            response.setTotalPages(totalPages);
            return this;
        }

        public PagedMessageResponseBuilder first(boolean first) {
            response.setFirst(first);
            return this;
        }

        public PagedMessageResponseBuilder last(boolean last) {
            response.setLast(last);
            return this;
        }

        public PagedMessageResponseBuilder empty(boolean empty) {
            response.setEmpty(empty);
            return this;
        }

        public PagedMessageResponse build() {
            return response;
        }
    }
}
