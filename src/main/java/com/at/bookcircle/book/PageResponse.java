package com.at.bookcircle.book;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {

    private List<T> contents;
    private int number;
    private int size;
    private int totalPages;
    private long totalElements;
    private  boolean first;
    private  boolean last;

}
