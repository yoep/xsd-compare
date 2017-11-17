package com.compare.xsd.writers;

import com.compare.xsd.compare.XsdComparer;
import com.compare.xsd.excel.Cell;
import com.compare.xsd.excel.Workbook;
import com.compare.xsd.excel.Worksheet;
import com.compare.xsd.managers.ViewManager;
import com.compare.xsd.model.xsd.XsdNode;
import com.compare.xsd.model.xsd.impl.XsdDocument;
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
    private static final int LEVEL_ROW_START_INDEX = 3;
    private static final int LEVEL_LIMIT = 15;

    private final ViewManager viewManager;
    private final FileChooser chooser;

    private List<Cell> levelColumns;
    private Cell typeColumn;
    private Cell cardinalityColumn;
    private Cell fixedValueColumn;
    private Cell patternColumn;
    private Cell enumerationColumn;

    /**
     * Initialize a new instance of {@link ExcelComparisonWriter}.
     *
     * @param viewManager Set the view manager.
     */
    public ExcelComparisonWriter(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.chooser = new FileChooser();
    }

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

                clear();
                writeXsdOverview(comparer.getOriginalDocument(), "Original document", workbook);
                writeXsdOverview(comparer.getNewDocument(), "New document", workbook);

                workbook.save();
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * Clear the writer information.
     */
    public void clear() {
        this.levelColumns = new ArrayList<>();
        this.typeColumn = null;
    }

    private File showSaveDialog() {
        File file = chooser.showSaveDialog(viewManager.getStage());
        String extension = EXTENSION.substring(1);

        if (!file.getName().contains(extension)) {
            file = new File(file.getAbsolutePath() + extension);
        }

        return file;
    }

    private void writeXsdOverview(XsdDocument document, String name, Workbook workbook) {
        Worksheet worksheet = workbook.getOrCreateWorksheet(name, true);

        writeDocumentInformation(document, worksheet);
        createColumns(worksheet);

        document.getElements().forEach(node -> writeXsdNode(node, 0, worksheet));
    }


    private void createColumns(Worksheet worksheet) {
        int levelIndex = 1;
        Cell lastLevelColumn = null;

        for (int columnIndex = LEVEL_COLUMN_START_INDEX; columnIndex < LEVEL_COLUMN_START_INDEX + LEVEL_LIMIT; columnIndex++) {
            lastLevelColumn = Cell.builder()
                    .value("L" + levelIndex)
                    .row(LEVEL_ROW_START_INDEX)
                    .column(columnIndex)
                    .build();

            this.levelColumns.add(lastLevelColumn);
            levelIndex++;
        }

        this.typeColumn = Cell.builder()
                .row(LEVEL_ROW_START_INDEX)
                .column(lastLevelColumn.getColumn() + 1)
                .autoSizeColumn(true)
                .value("Type")
                .build();
        this.cardinalityColumn = Cell.builder()
                .row(LEVEL_ROW_START_INDEX)
                .column(typeColumn.getColumn() + 1)
                .autoSizeColumn(true)
                .value("Cardinality")
                .build();
        this.fixedValueColumn = Cell.builder()
                .row(LEVEL_ROW_START_INDEX)
                .column(cardinalityColumn.getColumn() + 1)
                .autoSizeColumn(true)
                .value("Fixed value")
                .build();
        this.patternColumn = Cell.builder()
                .row(LEVEL_ROW_START_INDEX)
                .column(fixedValueColumn.getColumn() + 1)
                .autoSizeColumn(true)
                .value("Pattern")
                .build();
        this.enumerationColumn = Cell.builder()
                .row(LEVEL_ROW_START_INDEX)
                .column(patternColumn.getColumn() + 1)
                .autoSizeColumn(true)
                .value("Enumeration")
                .build();

        worksheet.write(this.levelColumns);
        worksheet.write(this.typeColumn);
        worksheet.write(this.cardinalityColumn);
        worksheet.write(this.fixedValueColumn);
        worksheet.write(this.patternColumn);
        worksheet.write(this.enumerationColumn);
    }

    private void writeDocumentInformation(XsdDocument document, Worksheet worksheet) {
        List<Cell> propertyCells = asList(Cell.builder()
                .column(0)
                .row(0)
                .value("Filename:")
                .autoSizeColumn(true)
                .build(), Cell.builder()
                .column(0)
                .row(1)
                .value("Location:")
                .autoSizeColumn(true)
                .build());
        List<Cell> valueCells = asList(Cell.builder()
                .column(1)
                .row(0)
                .value(document.getName())
                .build(), Cell.builder()
                .column(1)
                .row(1)
                .value(document.getFile().getAbsolutePath())
                .build());

        worksheet.write(propertyCells);
        worksheet.write(valueCells);
    }

    private void writeXsdNode(XsdNode node, int levelIndex, Worksheet worksheet) {
        if (levelIndex <= levelColumns.size() - 1) {
            int rowIndex = worksheet.getLastRowIndex() + 1;
            Cell name = Cell.builder()
                    .row(rowIndex)
                    .column(levelColumns.get(levelIndex).getColumn())
                    .value(node.getName())
                    .build();
            Cell type = Cell.builder()
                    .row(rowIndex)
                    .column(this.typeColumn.getColumn())
                    .autoSizeColumn(true)
                    .value(node.getType())
                    .build();
            Cell cardinality = Cell.builder()
                    .row(rowIndex)
                    .column(this.cardinalityColumn.getColumn())
                    .autoSizeColumn(true)
                    .value(node.getCardinality())
                    .build();
            Cell fixedValue = Cell.builder()
                    .row(rowIndex)
                    .column(this.fixedValueColumn.getColumn())
                    .autoSizeColumn(true)
                    .value(node.getFixedValue())
                    .build();
            Cell pattern = Cell.builder()
                    .row(rowIndex)
                    .column(this.patternColumn.getColumn())
                    .autoSizeColumn(true)
                    .value(node.getPattern())
                    .build();
            Cell enumeration = Cell.builder()
                    .row(rowIndex)
                    .column(this.enumerationColumn.getColumn())
                    .autoSizeColumn(true)
                    .value(node.getEnumeration())
                    .build();

            worksheet.write(asList(name, type, cardinality, fixedValue, pattern, enumeration));

            for (XsdNode childNode : node.getNodes()) {
                writeXsdNode(childNode, levelIndex + 1, worksheet);
            }
        } else {
            log.warning("Exceeding max level " + LEVEL_LIMIT + " for node " + node);
        }
    }
}
