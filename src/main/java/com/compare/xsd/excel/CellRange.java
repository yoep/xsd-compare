package com.compare.xsd.excel;

import com.compare.xsd.excel.exceptions.InvalidCellRangeException;
import com.compare.xsd.excel.exceptions.RangeOutOfBoundsException;
import lombok.*;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontFamily;
import org.springframework.util.Assert;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an Excel cell range.
 */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CellRange {
    @NonNull
    private final Range range;

    private Object value;

    private boolean autoSizeColumn;
    private boolean bold;
    private boolean italic;

    @Builder.Default
    private FontFamily fontFamily = FontFamily.MODERN;
    @Builder.Default
private Color fontColor = Color.BLACK;
    @Builder.Default
    private Color backgroundColor = new Color(255, 255, 255, 0);
    @Builder.Default
    private FillPatternType fillPattern = FillPatternType.NO_FILL;

    /**
     * Initialize a new instance of {@link CellRange}.
     *
     * @param range Set the cell range.
     */
    public CellRange(Range range) {
        this.range = range;
    }

    @EqualsAndHashCode
    @ToString
    @Getter
    public static class Range {
        private static int LAST_ROW_INDEX = SpreadsheetVersion.EXCEL2007.getLastRowIndex();
        private static final char CELL_DELIMITER = ':';
        private static final String PATTERN = "([a-zA-Z])$([0-9]+)$:([a-zA-Z])$([0-9]+)$";

        private String columnStart;
        private String columnEnd;
        private int rowStartIndex;
        private int rowEndIndex;

        //region Constructors

        /**
         * Initialize a new instance of {@link Range}.
         *
         * @param excelRange Set the excel range as "A1:B3".
         */
        public Range(String excelRange) {
            Assert.hasText(excelRange, "excelRange cannot be null");
            Pattern pattern = Pattern.compile(PATTERN);
            Matcher matcher = pattern.matcher(excelRange);

            if (matcher.matches()) {
                initialize(matcher.group(0), matcher.group(2), Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(3)));
            } else {
                throw new InvalidCellRangeException(excelRange);
            }
        }

        /**
         * Initialize a new instance of {@link Range}.
         *
         * @param column   Set the column index of the range.
         * @param rowIndex Set the row index of the range (zero-based index).
         */
        public Range(String column, int rowIndex) {
            initialize(column, column, rowIndex, rowIndex);
        }

        /**
         * Initialize a new instance of {@link Range}.
         *
         * @param columnIndex Set the column index of the range (zero-based index).
         * @param rowIndex    Set the row index of the range (zero-based index).
         */
        public Range(int columnIndex, int rowIndex) {
            String column = indexToLetter(columnIndex + 1);
            initialize(column, column, rowIndex, rowIndex);
        }

        /**
         * Initialize a new instance of {@link Range}.
         *
         * @param columnStartIndex Set the column start index (zero-based index).
         * @param columnEndIndex   Set the column end index (zero-based index).
         * @param rowStartIndex    Set the row start index (zero-based index).
         * @param rowEndIndex      Set the row end index (zero-based index).
         */
        public Range(int columnStartIndex, int columnEndIndex, int rowStartIndex, int rowEndIndex) {
            String columnStart = indexToLetter(columnStartIndex + 1);
            String columnEnd = indexToLetter(columnEndIndex + 1);
            initialize(columnStart, columnEnd, rowStartIndex, rowEndIndex);
        }

        //endregion

        //region Getters

        /**
         * Get the index of the start column.
         *
         * @return Returns the index (zero-based index).
         */
        public int getColumnStartIndex() {
            return Range.letterToIndex(columnStart) - 1;
        }

        /**
         * Get the index of the end column.
         *
         * @return Returns the index (zero-based index).
         */
        public int getColumnEndIndex() {
            return Range.letterToIndex(columnEnd) - 1;
        }

        //endregion

        //region Methods

        /**
         * Get the excel range indication.
         * example: A3:B5
         *
         * @return Returns the excel range indication.
         */
        public String toRange() {
            return columnStart + (rowStartIndex + 1) + Character.toString(CELL_DELIMITER) + columnEnd + (rowEndIndex + 1);
        }

        /**
         * Get the index of the given letter.
         *
         * @param letter Set the column letter.
         * @return Returns the index of the letter.
         */
        public static int letterToIndex(String letter) {
            int number = 0;

            for (int i = 0; i < letter.length(); i++) {
                number = number * 26 + (letter.charAt(i) - ('A' - 1));
            }

            return number;
        }

        /**
         * Get the letter for the given index.
         *
         * @param number Set the column index.
         * @return Returns the column letter.
         */
        public static String indexToLetter(int number) {
            StringBuilder sb = new StringBuilder();

            while (number-- > 0) {
                sb.append((char) ('A' + (number % 26)));
                number /= 26;
            }

            return sb.reverse().toString();
        }

        //endregion

        //region Functions

        private void initialize(String columnStart, String columnEnd, int rowStart, int rowEnd) throws RangeOutOfBoundsException {
            this.columnStart = columnStart.toUpperCase();
            this.columnEnd = columnEnd.toUpperCase();
            this.rowStartIndex = rowStart;
            this.rowEndIndex = rowEnd;

            if (rowStartIndex < 0 || rowEndIndex > LAST_ROW_INDEX) {
                throw new RangeOutOfBoundsException(toRange());
            }
        }

        //endregion
    }
}


