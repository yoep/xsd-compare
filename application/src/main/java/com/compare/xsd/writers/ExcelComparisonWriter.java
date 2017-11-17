package com.compare.xsd.writers;

import com.compare.xsd.compare.XsdComparer;
import com.compare.xsd.excel.CellRange;
import com.compare.xsd.excel.Workbook;
import com.compare.xsd.excel.Worksheet;
import com.compare.xsd.managers.ViewManager;
import com.compare.xsd.model.xsd.XsdNode;
import com.compare.xsd.model.xsd.impl.XsdDocument;
import com.compare.xsd.model.xsd.impl.XsdEmptyAttributeNode;
import com.compare.xsd.model.xsd.impl.XsdEmptyElementNode;
import javafx.stage.FileChooser;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static java.util.Arrays.asList;

@Log
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
     * Save the given comparer to an Excel file.
     *
     * @param comparer Set the comparer to save.
     */
    public void save(XsdComparer comparer) {
        Assert.notNull(comparer, "comparer cannot be null");
        File file = showSaveDialog();

        try {
            if (file != null) {
                Workbook workbook = new Workbook(file);

                writeXsdOverview(comparer.getOriginalDocument(), "Original document", workbook);
                writeXsdOverview(comparer.getNewDocument(), "New document", workbook);
                writeXsdComparison(comparer, workbook);

                workbook.save();
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    //endregion

    //region Functions

    private File showSaveDialog() {
        File file = chooser.showSaveDialog(viewManager.getStage());
        String extension = EXTENSION.substring(1);

        if (!file.getName().contains(extension)) {
            file = new File(file.getAbsolutePath() + extension);
        }

        return file;
    }

    private void writeXsdOverview(XsdDocument document, String name, Workbook workbook) {
        Worksheet worksheet = workbook.deleteAndCreateWorksheet(name, true);
        int rowIndex = 3;

        writeDocumentInformation(document, worksheet);
        TableHeader tableHeader = createTableHeader(LEVEL_COLUMN_START_INDEX, LEVEL_LIMIT, 3);

        tableHeader.writeHeader(worksheet);

        for (XsdNode node : document.getElements()) {
            rowIndex = writeXsdNode(node, tableHeader, 0, rowIndex + 1, worksheet, false);
        }
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

    private int writeXsdNode(XsdNode node, TableHeader tableHeader, int levelIndex, int rowIndex, Worksheet worksheet, boolean allowEmptyChildren) {
        if (levelIndex <= tableHeader.getLevelColumns().size() - 1) {
            CellRange name = CellRange.builder()
                    .range(new CellRange.Range(tableHeader.getLevelColumns().get(levelIndex).getRange().getColumnEndIndex(), rowIndex))
                    .value(node.getName())
                    .build();
            CellRange type = CellRange.builder()
                    .range(new CellRange.Range(tableHeader.getTypeColumn().getRange().getColumnEndIndex(), rowIndex))
                    .autoSizeColumn(true)
                    .value(node.getType())
                    .build();
            CellRange cardinality = CellRange.builder()
                    .range(new CellRange.Range(tableHeader.getCardinalityColumn().getRange().getColumnEndIndex(), rowIndex))
                    .autoSizeColumn(true)
                    .value(node.getCardinality())
                    .build();
            CellRange fixedValue = CellRange.builder()
                    .range(new CellRange.Range(tableHeader.getFixedValueColumn().getRange().getColumnEndIndex(), rowIndex))
                    .autoSizeColumn(true)
                    .value(node.getFixedValue())
                    .build();
            CellRange pattern = CellRange.builder()
                    .range(new CellRange.Range(tableHeader.getPatternColumn().getRange().getColumnEndIndex(), rowIndex))
                    .autoSizeColumn(true)
                    .value(node.getPattern())
                    .build();
            CellRange enumeration = CellRange.builder()
                    .range(new CellRange.Range(tableHeader.getEnumerationColumn().getRange().getColumnEndIndex(), rowIndex))
                    .autoSizeColumn(true)
                    .value(node.getEnumeration())
                    .build();

            worksheet.write(asList(name, type, cardinality, fixedValue, pattern, enumeration));

            for (XsdNode childNode : node.getNodes()) {
                if (notEmptyNode(childNode) || allowEmptyChildren) {
                    rowIndex = writeXsdNode(childNode, tableHeader, levelIndex + 1, rowIndex + 1, worksheet, allowEmptyChildren);
                }
            }
        } else {
            log.warning("Exceeding max level " + LEVEL_LIMIT + " for node " + node);
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

        for (XsdNode node : comparer.getOriginalDocument().getElements()) {
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
                .value(comparer.getOriginalDocument().getName())
                .build(), CellRange.builder()
                .range(new CellRange.Range(1, 1))
                .value(comparer.getOriginalDocument().getFile().getAbsolutePath())
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

    private boolean notEmptyNode(XsdNode node) {
        return !(node instanceof XsdEmptyElementNode) && !(node instanceof XsdEmptyAttributeNode);
    }

    //endregion
}
