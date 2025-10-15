package com.fiserv.fico.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PagedSection<T> {
  private List<T> content;
  private long totalElements;
  private int page;
  private int size;
  private int totalPages;

  // Preserve record-style accessors
  public List<T> content() { return content; }
  public long totalElements() { return totalElements; }
  public int page() { return page; }
  public int size() { return size; }
  public int totalPages() { return totalPages; }
}
