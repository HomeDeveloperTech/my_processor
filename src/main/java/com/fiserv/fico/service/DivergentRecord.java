package com.fiserv.fico.service;

import java.util.List;

public record DivergentRecord(CompareKey key, List<ValueDiff> diffs) {}
