package com.example.weather_rest_clone.model.pojo.paging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationSetting {

    private Integer pageIndex;

    private Integer pageSize;

    // only apply for 0-based result data
    public int getFirstResultIndex() {
        return (pageIndex - 1) * pageSize;
    }

    public PaginationSetting(Integer pageIndex, Integer pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }
}
