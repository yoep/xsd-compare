package com.compare.xsd.excel;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;

public class Workbook {
    private final File file;

    private XSSFWorkbook workbook;

    /**
     * Initialize a new instance of {@link Workbook}.
     *
     * @param file Set the file of the workbook.
     * @throws ExcelLoadingException Is thrown when the given file couldn't be loaded.
     */
    public Workbook(File file) throws ExcelLoadingException {
        Assert.notNull(file, "file cannot be null");
        this.file = file;

        init();
    }

    /**
     * Create a new worksheet within the workbook.
     *
     * @param name Set the name of the workbook.
     */
    public void createWorksheet(String name) {
        Assert.hasText(name, "name cannot be empty");

        workbook.createSheet(name);
    }

    /**
     * Save the workbook.
     *
     * @throws IOException Is thrown when the workbook couldn't be saved.
     */
    public void save() throws IOException {
        workbook.write(FileUtils.openOutputStream(this.file));
    }

    private void init() throws ExcelLoadingException {
        try {
            if (this.file.exists()) {
                this.workbook = new XSSFWorkbook(this.file);
            } else {
                this.workbook = new XSSFWorkbook();
            }
        } catch (IOException | InvalidFormatException ex) {
            throw new ExcelLoadingException(this.file, ex);
        }
    }
}
