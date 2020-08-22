package com.compare.xsd.settings.model;

import lombok.*;

import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareSettings extends AbstractSettings {
    public static final String SHOW_COLUMNS_PROPERTY = "showColumns";

    @Builder.Default
    private List<CompareColumns> shownColumns = asList(CompareColumns.TYPE, CompareColumns.CARDINALITY);

    public void setShownColumns(List<CompareColumns> shownColumns) {
        if (Objects.equals(this.shownColumns, shownColumns))
            return;

        var oldValue = this.shownColumns;
        this.shownColumns = shownColumns;
        changes.firePropertyChange(SHOW_COLUMNS_PROPERTY, oldValue, shownColumns);
    }
}
