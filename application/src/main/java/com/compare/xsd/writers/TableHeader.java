package com.compare.xsd.writers;

import com.compare.xsd.excel.CellRange;
import com.compare.xsd.excel.Worksheet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableHeader {
    private List<CellRange> levelColumns;
    private CellRange typeColumn;
    private CellRange cardinalityColumn;
    private CellRange fixedValueColumn;
    private CellRange patternColumn;
    private CellRange enumerationColumn;

    /**
     * Get the column end index of this header.
     *
     * @return Returns the column end index.
     */
    public int getColumnEndIndex() {
        return enumerationColumn.getRange().getColumnEndIndex();
    }

    /**
     * Write the header to the worksheet.
     *
     * @param worksheet Set the worksheet to write to.
     */
    public void writeHeader(Worksheet worksheet) {
        Assert.notNull(worksheet, "worksheet cannot be null");

        worksheet.write(this.levelColumns);
        worksheet.write(this.typeColumn);
        worksheet.write(this.cardinalityColumn);
        worksheet.write(this.fixedValueColumn);
        worksheet.write(this.patternColumn);
        worksheet.write(this.enumerationColumn);
    }
}
