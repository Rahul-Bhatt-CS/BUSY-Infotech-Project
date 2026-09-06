package com.BUSY.learnWithUs.Dto.Course;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Getter
@Setter
public class PageResponse<T> {

    private List<T> items;
    private int page;
    private int size;
    private long totalMatches;
}
