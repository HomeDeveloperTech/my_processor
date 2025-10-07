package com.fiserv.fico.service;

import java.util.List;

public record PagedSection<T>(
    List<T> content, long totalElements, int page, int size, int totalPages) {}
