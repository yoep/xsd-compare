package com.compare.xsd.writers;

import com.compare.xsd.comparison.XsdComparer;
import com.compare.xsd.comparison.model.Change;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;
import com.compare.xsd.comparison.model.xsd.impl.XsdEmptyAttributeNode;
import com.compare.xsd.comparison.model.xsd.impl.XsdEmptyElementNode;
import com.compare.xsd.excel.CellRange;
import com.compare.xsd.excel.Workbook;
import com.compare.xsd.excel.Worksheet;
import com.github.spring.boot.javafx.view.ViewManager;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import jakarta.annotation.PostConstruct;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class ExcelComparisonWriter {
    private static final String EXTENSION_DESCRIPTION = "Excel file";
    private static final String EXTENSION = "*.xlsx";
    private static final int LEVEL_COLUMN_START_INDEX = 1;
    private static final int LEVEL_LIMIT = 15;

    private final ViewManager viewManager;
    private final FileChooser chooser;

    //region Constructors

    /**
     * Initialize a new instance of {@link ExcelComparisonWriter}.
     *
     * @param viewManager Set the view manager.
     */
    public ExcelComparisonWriter(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.chooser = new FileChooser();
    }

    //endregion

    //region Methods

    @PostConstruct
    public void init() {
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(EXTENSION_DESCRIPTION, EXTENSION);

        chooser.setTitle("Select save location");
        chooser.getExtensionFilters().add(extensionFilter);
    }

    /**
     * Show the save dialog.
     *
     * @return Returns the selected file.
     */
    public File showSaveDialog() {
        String extension = EXTENSION.substring(1);
        Optional<Stage> stage = viewManager.getPrimaryStage();
        File file;

        if (!stage.isPresent()) {
            log.error("Failed to open save dialog, primary stage is missing");
            return null;
        }

        file = chooser.showSaveDialog(stage.get());

        if (file != null) {
            if (!file.getName().contains(extension)) {
                file = new File(file.getAbsolutePath() + extension);
            }
        }

        return file;
    }

    /**
     * Save the given comparer to an Excel file.
     *
     * @param comparer Set the comparer to save.
     * @return Returns true if the comparer was saved with success, else false.
     */
    @Async
    public CompletableFuture<Boolean> save(XsdComparer comparer, File file) {
        Assert.notNull(comparer, "comparer cannot be null");

        try {
            if (file != null) {
                Workbook workbook = new Workbook(file);

                writeXsdOverview(comparer.getOldDocument(), "Original document", workbook);
                writeXsdOverview(comparer.getNewDocument(), "New document", workbook);
                writeXsdComparison(comparer, workbook);

                workbook.save();
                return CompletableFuture.completedFuture(Boolean.TRUE);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return CompletableFuture.completedFuture(Boolean.FALSE);
    }

    //endregion

    //region Functions

    private void writeXsdOverview(XsdDocument document, String name, Workbook workbook) {
        final int rowStartIndex = 3;
        int rowIndex = rowStartIndex;
        Worksheet worksheet = workbook.deleteAndCreateWorksheet(name, true);

        writeDocumentInformation(document, worksheet);
        TableHeader tableHeader = createTableHeader(LEVEL_COLUMN_START_INDEX, LEVEL_LIMIT, rowStartIndex);

        tableHeader.writeHeader(worksheet);

        for (XsdNode node : document.getElements()) {
            rowIndex = writeXsdNode(node, tableHeader, 0, rowIndex + 1, worksheet, false);
        }

        worksheet.createTable(name, new CellRange.Range(LEVEL_COLUMN_START_INDEX, tableHeader.getColumnEndIndex(), rowStartIndex, rowIndex));
    }


    private TableHeader createTableHeader(int columnStartIndex, int levelLimit, int rowStartIndex) {
        CellRange lastLevelColumn = null;
        TableHeader tableHeader = new TableHeader();
        List<CellRange> levelColumns = new ArrayList<>();
        int levelIndex = 1;

        for (int columnIndex = columnStartIndex; columnIndex < columnStartIndex + levelLimit; columnIndex++) {
            lastLevelColumn = CellRange.builder()
                    .range(new CellRange.Range(columnIndex, rowStartIndex))
                    .value("L" + levelIndex)
                    .build();

            levelColumns.add(lastLevelColumn);
            levelIndex++;
        }

        tableHeader.setLevelColumns(levelColumns);
        tableHeader.setTypeColumn(CellRange.builder()
                .range(new CellRange.Range(lastLevelColumn.getRange().getColumnEndIndex() + 1, rowStartIndex))
                .value("Type")
                .build());
        tableHeader.setCardinalityColumn(CellRange.builder()
                .range(new CellRange.Range(tableHeader.getTypeColumn().getRange().getColumnEndIndex() + 1, rowStartIndex))
                .value("Cardinality")
                .build());
        tableHeader.setFixedValueColumn(CellRange.builder()
                .range(new CellRange.Range(tableHeader.getCardinalityColumn().getRange().getColumnEndIndex() + 1, rowStartIndex))
                .value("Fixed value")
                .build());
        tableHeader.setPatternColumn(CellRange.builder()
                .range(new CellRange.Range(tableHeader.getFixedValueColumn().getRange().getColumnEndIndex() + 1, rowStartIndex))
                .value("Pattern")
                .build());
        tableHeader.setEnumerationColumn(CellRange.builder()
                .range(new CellRange.Range(tableHeader.getPatternColumn().getRange().getColumnEndIndex() + 1, rowStartIndex))
                .value("Enumeration")
                .build());

        return tableHeader;
    }

    private void writeDocumentInformation(XsdDocument document, Worksheet worksheet) {
        List<CellRange> propertyCells = asList(CellRange.builder()
                .range(new CellRange.Range(0, 0))
                .value("Filename:")
                .autoSizeColumn(true)
                .bold(true)
                .build(), CellRange.builder()
                .range(new CellRange.Range(0, 1))
                .value("Location:")
                .autoSizeColumn(true)
                .bold(true)
                .build());
        List<CellRange> valueCells = asList(CellRange.builder()
                .range(new CellRange.Range(1, 0))
                .value(document.getName())
                .build(), CellRange.builder()
                .range(new CellRange.Range(1, 1))
                .value(document.getFile().getAbsolutePath())
                .build());

        worksheet.write(propertyCells);
        worksheet.write(valueCells);
    }

    private int writeXsdNode(XsdNode node, TableHeader tableHeader, int levelIndex, int rowIndex, Worksheet worksheet, boolean isComparison) {
        if (levelIndex <= tableHeader.getLevelColumns().size() - 1) {
            Change modification = ofNullable(node.getChange()).orElse(new Change());
            Color backgroundColor = getBackgroundColor(modification);
            Color transparent = new Color(255, 255, 255, 0);
            List<CellRange> nameCells = new ArrayList<>();
            CellRange type = CellRange.builder()
                    .range(new CellRange.Range(tableHeader.getTypeColumn().getRange().getColumnEndIndex(), rowIndex))
                    .autoSizeColumn(true)
                    .value(node.getTypeName())
                    .fontColor(isComparison && modification.isTypeChanged() ? Color.RED : Color.BLACK)
                    .backgroundColor(isComparison ? backgroundColor : transparent)
                    .fillPattern(isComparison ? FillPatternType.SOLID_FOREGROUND : FillPatternType.NO_FILL)
                    .build();
            CellRange cardinality = CellRange.builder()
                    .range(new CellRange.Range(tableHeader.getCardinalityColumn().getRange().getColumnEndIndex(), rowIndex))
                    .autoSizeColumn(true)
                    .value(node.getCardinality())
                    .fontColor(isComparison && modification.isCardinalityChanged() ? Color.RED : Color.BLACK)
                    .backgroundColor(isComparison ? backgroundColor : transparent)
                    .fillPattern(isComparison ? FillPatternType.SOLID_FOREGROUND : FillPatternType.NO_FILL)
                    .build();
            CellRange fixedValue = CellRange.builder()
                    .range(new CellRange.Range(tableHeader.getFixedValueColumn().getRange().getColumnEndIndex(), rowIndex))
                    .autoSizeColumn(true)
                    .value(node.getFixedValue())
                    .fontColor(isComparison && modification.isFixedDefaultChanged() ? Color.RED : Color.BLACK)
                    .backgroundColor(isComparison ? backgroundColor : transparent)
                    .fillPattern(isComparison ? FillPatternType.SOLID_FOREGROUND : FillPatternType.NO_FILL)
                    .build();
            CellRange pattern = CellRange.builder()
                    .range(new CellRange.Range(tableHeader.getPatternColumn().getRange().getColumnEndIndex(), rowIndex))
                    .autoSizeColumn(true)
                    .value(node.getPattern())
                    .fontColor(isComparison && modification.isPatternChanged() ? Color.RED : Color.BLACK)
                    .backgroundColor(isComparison ? backgroundColor : transparent)
                    .fillPattern(isComparison ? FillPatternType.SOLID_FOREGROUND : FillPatternType.NO_FILL)
                    .build();
            CellRange enumeration = CellRange.builder()
                    .range(new CellRange.Range(tableHeader.getEnumerationColumn().getRange().getColumnEndIndex(), rowIndex))
                    .autoSizeColumn(true)
                    .value(node.getEnumeration())
                    .fontColor(isComparison && modification.isEnumerationChanged() ? Color.RED : Color.BLACK)
                    .backgroundColor(isComparison ? backgroundColor : transparent)
                    .fillPattern(isComparison ? FillPatternType.SOLID_FOREGROUND : FillPatternType.NO_FILL)
                    .build();

            for (CellRange column : tableHeader.getLevelColumns()) {
                CellRange cell = CellRange.builder()
                        .range(new CellRange.Range(column.getRange().getColumnStart(), rowIndex))
                        .fontColor(isComparison && modification.isNameChanged() ? Color.RED : Color.BLACK)
                        .backgroundColor(isComparison ? backgroundColor : transparent)
                        .fillPattern(isComparison ? FillPatternType.SOLID_FOREGROUND : FillPatternType.NO_FILL)
                        .build();

                if (tableHeader.getLevelColumns().indexOf(column) == levelIndex) {
                    cell.setValue(node.getName());
                }

                nameCells.add(cell);
            }

            worksheet.write(ListUtils.union(nameCells, asList(type, cardinality, fixedValue, pattern, enumeration)));

            for (XsdNode childNode : node.getNodes()) {
                if (notEmptyNode(childNode) || isComparison) {
                    rowIndex = writeXsdNode(childNode, tableHeader, levelIndex + 1, rowIndex + 1, worksheet, isComparison);
                }
            }
        } else {
            log.warn("Exceeding max level " + LEVEL_LIMIT + " for node " + node);
        }

        return rowIndex;
    }

    private void writeXsdComparison(XsdComparer comparer, Workbook workbook) {
        Worksheet worksheet = workbook.deleteAndCreateWorksheet("Comparison", true);
        int rowIndex = 5;

        writeCompareInformation(comparer, worksheet);
        TableHeader tableHeaderOriginal = createTableHeader(LEVEL_COLUMN_START_INDEX, LEVEL_LIMIT, 5);
        TableHeader tableHeaderNew = createTableHeader(tableHeaderOriginal.getEnumerationColumn().getRange().getColumnEndIndex() + 2, LEVEL_LIMIT, 5);

        tableHeaderOriginal.writeHeader(worksheet);
        tableHeaderNew.writeHeader(worksheet);

        for (XsdNode node : comparer.getOldDocument().getElements()) {
            rowIndex = writeXsdNode(node, tableHeaderOriginal, 0, rowIndex + 1, worksheet, true);
        }

        rowIndex = 5;

        for (XsdNode node : comparer.getNewDocument().getElements()) {
            rowIndex = writeXsdNode(node, tableHeaderNew, 0, rowIndex + 1, worksheet, true);
        }
    }

    private void writeCompareInformation(XsdComparer comparer, Worksheet worksheet) {
        List<CellRange> propertyCells = asList(CellRange.builder()
                .range(new CellRange.Range(0, 0))
                .autoSizeColumn(true)
                .bold(true)
                .value("Original document:")
                .build(), CellRange.builder()
                .range(new CellRange.Range(0, 1))
                .value("Original document file:")
                .autoSizeColumn(true)
                .bold(true)
                .build(), CellRange.builder()
                .range(new CellRange.Range(0, 2))
                .autoSizeColumn(true)
                .bold(true)
                .value("New document:")
                .build(), CellRange.builder()
                .range(new CellRange.Range(0, 3))
                .value("New document file:")
                .autoSizeColumn(true)
                .bold(true)
                .build());
        List<CellRange> informationCells = asList(CellRange.builder()
                .range(new CellRange.Range(1, 0))
                .value(comparer.getOldDocument().getName())
                .build(), CellRange.builder()
                .range(new CellRange.Range(1, 1))
                .value(comparer.getOldDocument().getFile().getAbsolutePath())
                .build(), CellRange.builder()
                .range(new CellRange.Range(1, 2))
                .value(comparer.getNewDocument().getName())
                .build(), CellRange.builder()
                .range(new CellRange.Range(1, 3))
                .value(comparer.getNewDocument().getFile().getAbsolutePath())
                .build());

        worksheet.write(propertyCells);
        worksheet.write(informationCells);
    }

    private Color getBackgroundColor(Change modification) {
        Color transparent = new Color(255, 255, 255, 0);

        if (modification != null && modification.getType() != null) {
            switch (modification.getType()) {
                case ADDED:
                    return new Color(170, 255, 201);
                case REMOVED:
                    return new Color(255, 170, 170);
                case MODIFIED:
                    return new Color(255, 209, 170);
                case MOVED:
                    return new Color(255, 253, 170);
                default:
                    return transparent;
            }
        }

        return transparent;
    }

    private boolean notEmptyNode(XsdNode node) {
        return !(node instanceof XsdEmptyElementNode) && !(node instanceof XsdEmptyAttributeNode);
    }

    //endregion
}
