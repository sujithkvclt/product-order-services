package com.ecommerce.productorder.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic wrapper for paginated API responses
 * @param <T> The type of content in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    private List<T> content;
    private Integer page;
    private Integer size;
    private Long totalItems;
    private Integer totalPages;

    /**
     * Create a PagedResponse from a Spring Data Page object
     * @param page The Spring Data Page
     * @param <T> The type of content
     * @return PagedResponse containing the page data
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
