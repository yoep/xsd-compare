package com.compare.xsd.excel;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.util.Assert;

public class Worksheet {
    private static final int NAME_LIMIT = 32;

    private final Workbook workbook;
    private final XSSFSheet worksheet;

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
     * Write the given cell in this worksheet.
     *
     * @param cell Set the cell to write.
     */
    public void writeCell(Cell cell) {
        Assert.notNull(cell, "cell cannot be null");
        XSSFRow row = getOrCreateRow(cell.getRow());
        XSSFCell xssfCell = getOrCreateCell(cell.getColumn(), row);

        writeCellValue(cell, xssfCell);

        if (cell.isAutoSizeColumn()) {
            worksheet.autoSizeColumn(cell.getColumn());
        }
    }

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

    private void writeCellValue(Cell cell, XSSFCell xssfCell) {
        if (cell.getValue() instanceof String) {
            xssfCell.setCellValue((String) cell.getValue());
        } else if (cell.getValue() instanceof Integer) {
            xssfCell.setCellValue((Integer) cell.getValue());
        }
    }
}
