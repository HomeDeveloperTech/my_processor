package com.fiserv.fico.service;

import com.fiserv.fico.domain.AluguelExcecao;
import com.fiserv.fico.domain.AluguelProcessamentoAlianca;
import java.util.List;

public record PagedCompareReport(
    PagedSection<AluguelProcessamentoAlianca> onlyInAlianca,
    PagedSection<AluguelExcecao> onlyInExcecao,
    PagedSection<DivergentRecord> divergent) {

  public static PagedCompareReport from(CompareReport report, int page, int size) {
    return new PagedCompareReport(
        paginate(report.onlyInAlianca(), page, size),
        paginate(report.onlyInExcecao(), page, size),
        paginate(report.divergent(), page, size));
  }

  private static <T> PagedSection<T> paginate(List<T> items, int page, int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("size must be greater than zero");
    }
    if (items == null || items.isEmpty()) {
      return new PagedSection<>(List.of(), 0, page, size, 0);
    }

    int total = items.size();
    long offset = (long) page * size;
    if (offset >= total) {
      int totalPages = calculateTotalPages(total, size);
      return new PagedSection<>(List.of(), total, page, size, totalPages);
    }

    int fromIndex = (int) offset;
    int toIndex = Math.min(fromIndex + size, total);
    List<T> content = List.copyOf(items.subList(fromIndex, toIndex));
    int totalPages = calculateTotalPages(total, size);

    return new PagedSection<>(content, total, page, size, totalPages);
  }

  private static int calculateTotalPages(int total, int size) {
    if (total == 0) {
      return 0;
    }
    return ((total - 1) / size) + 1;
  }
}
