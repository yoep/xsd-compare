package com.compare.xsd.settings.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static java.util.Arrays.asList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareSettings {
    @Builder.Default
    private List<CompareColumns> shownColumns = asList(CompareColumns.TYPE, CompareColumns.CARDINALITY);
}
