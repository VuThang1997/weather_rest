package com.example.weather_rest_clone.model.pojo.paging;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor
public class PaginationResult<T> {
    private PaginationInfo paginationInfo;
    private List<T> data;

    public PaginationResult(PaginationInfo paginationInfo, List<T> data) {
        this.paginationInfo = paginationInfo;
        this.data = data;
    }
}
