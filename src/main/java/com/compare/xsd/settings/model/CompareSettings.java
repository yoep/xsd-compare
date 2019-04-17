package com.compare.xsd.settings.model;

import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.Observable;

import static java.util.Arrays.asList;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareSettings extends Observable {
    @Builder.Default
    private List<CompareColumns> shownColumns = Collections.unmodifiableList(asList(CompareColumns.TYPE, CompareColumns.CARDINALITY));

    public void setShownColumns(List<CompareColumns> shownColumns) {
        this.shownColumns = Collections.unmodifiableList(shownColumns);
        this.setChanged();
        this.notifyObservers();
    }
}
