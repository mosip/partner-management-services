package io.mosip.pms.common.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseV2Dto<T> {
    private long pageNo;
    private long pageSize;
    private long totalResults;
    @Valid
    private List<T> data = new ArrayList<>();
}
