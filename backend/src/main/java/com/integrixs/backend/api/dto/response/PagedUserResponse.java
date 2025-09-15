package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for paginated user results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedUserResponse {

    private List<UserResponse> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    @Builder.Default
    private boolean empty = false;
}
