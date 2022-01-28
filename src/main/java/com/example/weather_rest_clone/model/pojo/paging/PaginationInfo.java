package com.example.weather_rest_clone.model.pojo.paging;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class PaginationInfo {
    private int pageIndex;
    private int pageSize;
    private int totalCount;
    private int totalPage;

    public PaginationInfo(int pageIndex, int pageSize, int totalCount, int totalPage) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPage = totalPage;
    }
}
