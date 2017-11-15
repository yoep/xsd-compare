package com.compare.xsd.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an Excel cell.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cell {
    private int column;
    private int row;

    private Object value;

    private boolean autoSizeColumn;
}
