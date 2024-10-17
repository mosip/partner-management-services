package io.mosip.pms.common.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchV2Dto {

    private List<SearchFilterV2> filters;

    private SearchSortV2 sort;

    private PaginationV2 pagination;
}
