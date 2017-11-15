package com.compare.xsd.writers;

import com.compare.xsd.compare.XsdComparer;
import com.compare.xsd.excel.Cell;
import com.compare.xsd.excel.Workbook;
import com.compare.xsd.excel.Worksheet;
import com.compare.xsd.managers.ViewManager;
import javafx.stage.FileChooser;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

@Log
@Component
public class ExcelComparisonWriter {
    private static final String EXTENSION_DESCRIPTION = "Excel file";
    private static final String EXTENSION = "*.xlsx";

    private final ViewManager viewManager;
    private final FileChooser chooser;

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
                Worksheet worksheet = workbook.getOrCreateWorksheet(comparer.getOriginalDocument().getName(), true);

                worksheet.writeCell(Cell.builder()
                        .column(0)
                        .row(0)
                        .value("Original XSD:")
                        .autoSizeColumn(true)
                        .build());
                worksheet.writeCell(Cell.builder()
                        .column(1)
                        .row(0)
                        .value(comparer.getOriginalDocument().getName())
                        .build());

                workbook.save();
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private File showSaveDialog() {
        return chooser.showSaveDialog(viewManager.getStage());
    }
}
