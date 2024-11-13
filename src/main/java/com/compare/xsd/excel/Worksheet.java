package com.compare.xsd.excel;

import com.compare.xsd.excel.exceptions.NameTooLongException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Represents a worksheet of an excel file.
 *
 * @see XSSFSheet
 */
@Slf4j
@Getter
public class Worksheet {
    private static final char SHEET_NAME_DELIMITER = '!';
    private static final char SPECIAL_NAME_DELIMITER = '\'';
    private static final int NAME_LIMIT = 32;

    private final Workbook workbook;
    private final XSSFSheet worksheet;

    //region Constructors

    /**
     * Initialize a new instance of {@link Worksheet}.
     *
     * @param workbook  Set the workbook of the worksheet.
     * @param worksheet Set the worksheet.
     */
    public Worksheet(Workbook workbook, XSSFSheet worksheet) {
        this.workbook = workbook;
        this.worksheet = worksheet;
    }

    //endregion

    //region Getters & Setters

    /**
     * Get the shortened name for the given name if it's longer then {@link #NAME_LIMIT}.
     *
     * @param name        Set the name.
     * @param shortenName Set if the name must be shortened.
     * @return Returns the name of the worksheet.
     */
    public static String getWorksheetName(String name, boolean shortenName) {
        if (name.length() > NAME_LIMIT) {
            if (shortenName) {
                name = name.substring(0, 28) + "...";
            } else {
                throw new NameTooLongException(name, NAME_LIMIT);
            }
        }
        return name;
    }

    /**
     * Get the last row index present in this worksheet.
     *
     * @return Returns the index of the last row.
     */
    public int getLastRowIndex() {
        return worksheet.getLastRowNum();
    }

    //endregion

    //region Methods

    /**
     * Create a new worksheet with the given name within the given workbook.
     *
     * @param name        Set the name of the worksheet (max. 32 chars.).
     * @param workbook    Set the workbook to create the worksheet in.
     * @param shortenName Set if the name must be shortened when too long.
     * @return Returns the created worksheet.
     * @throws NameTooLongException Is thrown when the given name is too long and #shortenName is false.
     */
    public static Worksheet newWorksheet(String name, Workbook workbook, boolean shortenName) throws NameTooLongException {
        Assert.hasText(name, "name cannot be empty");
        Assert.notNull(workbook, "workbook cannot be null");

        name = getWorksheetName(name, shortenName);

        return new Worksheet(workbook, workbook.getWorkbook().createSheet(name));
    }

    /**
     * Write the given cells in this worksheet.
     *
     * @param cells Set the cells to write.
     */
    public void write(Collection<CellRange> cells) {
        Assert.notNull(cells, "cells cannot be null");

        cells.forEach(this::write);
    }

    /**
     * Write the given cell in this worksheet.
     *
     * @param cell Set the cell to write.
     */
    public void write(CellRange cell) {
        Assert.notNull(cell, "cell cannot be null");
        AreaReference areaReference = new AreaReference(getSheetReference(cell.getRange()), SpreadsheetVersion.EXCEL2007);
        List<CellReference> cellReferences = Arrays.asList(areaReference.getAllReferencedCells());

        for (CellReference cellReference : cellReferences) {
            XSSFRow row = getOrCreateRow(cellReference.getRow());
            XSSFCell xssfCell = getOrCreateCell(cellReference.getCol(), row);
            XSSFCellStyle cellStyle = workbook.getWorkbook().createCellStyle();
            XSSFFont newFont = workbook.getWorkbook().createFont();

            writeCellValue(cell, xssfCell);
            newFont.setFamily(cell.getFontFamily());
            newFont.setBold(cell.isBold());
            newFont.setItalic(cell.isItalic());
            newFont.setColor(new XSSFColor(cell.getFontColor(), null));
            cellStyle.setFillForegroundColor(new XSSFColor(cell.getBackgroundColor(), null));
            cellStyle.setFillPattern(cell.getFillPattern());

            if (cell.isAutoSizeColumn()) {
                worksheet.autoSizeColumn(cellReference.getCol());
            }

            cellStyle.setFont(newFont);
            xssfCell.setCellStyle(cellStyle);
        }
    }

    /**
     * Create a new table with the given name on the given range.
     *
     * @param name  Set the name of the table.
     * @param range Set the range.
     */
    public void createTable(String name, CellRange.Range range) {
        log.debug("Creating table '" + name + "' within '" + worksheet.getSheetName() + "' [" + range.toRange() + "]");
        final int totalColumns = (range.getColumnEndIndex() - range.getColumnStartIndex()) + 1;
        AreaReference tableArea = new AreaReference(getSheetReference(range), SpreadsheetVersion.EXCEL2007);
        XSSFTable table = worksheet.createTable(tableArea);

        table.getCTTable().addNewTableStyleInfo();
        table.getCTTable().getTableStyleInfo().setName("TableStyleMedium2");

        table.setDisplayName(getTableDisplayName(name));
        table.setName(getTableName(name));

        XSSFTableStyleInfo style = (XSSFTableStyleInfo) table.getStyle();
        style.setName("TableStyleMedium2");
        style.setShowColumnStripes(false);
        style.setShowRowStripes(true);
        style.setFirstColumn(false);
        style.setLastColumn(false);
        style.setShowRowStripes(true);
        style.setShowColumnStripes(true);

        for (int i = 0; i < totalColumns; i++) {
            table.createColumn(Integer.toString(i));
        }

        table.setCellReferences(tableArea);

        worksheet.setAutoFilter(new CellRangeAddress(range.getRowStartIndex(), range.getRowStartIndex(), range.getColumnStartIndex(),
                range.getColumnEndIndex()));
        log.debug("Table created");
    }

    //endregion

    //region Functions

    private XSSFRow getOrCreateRow(int rowIndex) {
        XSSFRow row = worksheet.getRow(rowIndex);

        if (row == null) {
            row = worksheet.createRow(rowIndex);
        }

        return row;
    }

    private XSSFCell getOrCreateCell(int columnIndex, XSSFRow row) {
        XSSFCell cell = row.getCell(columnIndex);

        if (cell == null) {
            cell = row.createCell(columnIndex);
        }

        return cell;
    }

    private void writeCellValue(CellRange cell, XSSFCell xssfCell) {
        if (cell.getValue() instanceof String) {
            xssfCell.setCellValue((String) cell.getValue());
        } else if (cell.getValue() instanceof Integer) {
            xssfCell.setCellValue((Integer) cell.getValue());
        }
    }

    private String getTableName(String name) {
        return name.replace(" ", "").toLowerCase();
    }

    private String getTableDisplayName(String name) {
        return name.replace(" ", "_");
    }

    private String getSheetReference(CellRange.Range range) {
        return SPECIAL_NAME_DELIMITER + worksheet.getSheetName() + SPECIAL_NAME_DELIMITER + SHEET_NAME_DELIMITER + range.toRange();
    }

    //endregion
}
