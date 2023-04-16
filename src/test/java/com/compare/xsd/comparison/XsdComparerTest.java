package com.compare.xsd.comparison;

import com.compare.xsd.comparison.model.ChangeType;
import com.compare.xsd.comparison.model.xsd.XsdElementNode;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;
import com.github.spring.boot.javafx.view.ViewManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class XsdComparerTest {
    @Mock
    private ViewManager viewManager;

    private XsdLoader xsdLoader;

    /**
     * Via Maven pom.xml (surefire test plugin) received System variable of the absolute path of the
     * base directory
     *
     * <p>{@ref https://cwiki.apache.org/confluence/display/MAVEN/Maven+Properties+Guide}
     *
     * <p>The absolute path to the pom directory of this submodule, which is relative to project root
     * ./generator/schema2template
     */
    static final String BASE_DIR = System.getProperty("xsd.compare.base.dir") + File.separator;

    private static final String RESOURCES_DIR = BASE_DIR + "src" + File.separator
            + "test" + File.separator
            + "resources" + File.separator;

    private static final String TARGET_DIR = BASE_DIR + "target" + File.separator;
    private static final String REFERENCES_DIR = RESOURCES_DIR + "references" + File.separator;
    private static final String XSD_DIR = RESOURCES_DIR + "xsd" + File.separator;

    private static final String CII_D16B_XSD = XSD_DIR + "EN16931" + File.separator
            + "data" + File.separator
            + "standard" + File.separator
            + "CrossIndustryInvoice_100pD16B.xsd";

    private static final String CII_D22B_XSD = XSD_DIR + "uncefact_22B_20230324" + File.separator
            + "CrossIndustryInvoice_100pD22B.xsd";


    private static final String FACTUR_X_BASIC_WL = XSD_DIR + "factur-x" + File.separator + "FACTUR-X_BASIC-WL.xsd";
    private static final String FACTUR_X_BASIC = XSD_DIR + "factur-x" + File.separator + "FACTUR-X_BASIC.xsd";
    private static final String FACTUR_X_EN16931 = XSD_DIR + "factur-x" + File.separator + "FACTUR-X_EN16931.xsd";
    private static final String FACTUR_X_EXTENDED = XSD_DIR + "factur-x" + File.separator + "FACTUR-X_EXTENDED.xsd";
    private static final String FACTUR_X_MINIMUM = XSD_DIR + "factur-x" + File.separator + "FACTUR-X_MINIMUM.xsd";




    @BeforeEach
    public void setup() {
        xsdLoader = new XsdLoader(viewManager);
    }



    @Test
    public void recursiveGrammarsComparison() throws IOException {

        String simpleAnonymous1 = XSD_DIR + "simple_anonymous1.xsd";
        String simpleAnonymous2 = XSD_DIR + "simple_anonymous2.xsd";
        String facets1 = XSD_DIR + "facets1.xsd";
        String facets2 = XSD_DIR + "facets2.xsd";
        Boolean compareCorrect = Boolean.TRUE;
        // compareCorrect &= compareTwoXsdGrammars(facets1, facets2, TextReport.implementation.ONLY_EXTENSIONS);
        // compareTwoXsdGrammars(facets1, facets2, TextReport.implementation.MULTI_LINE_CHANGE);
        for(TextReport.implementation reportType : TextReport.implementation.values()){
            compareCorrect &= compareTwoXsdGrammars(CII_D16B_XSD, CII_D22B_XSD, reportType);

            compareCorrect &= compareTwoXsdGrammars(FACTUR_X_MINIMUM, CII_D22B_XSD, reportType);
            compareCorrect &= compareTwoXsdGrammars(FACTUR_X_BASIC_WL, CII_D22B_XSD, reportType);
            compareCorrect &= compareTwoXsdGrammars(FACTUR_X_BASIC, CII_D22B_XSD, reportType);
            compareCorrect &= compareTwoXsdGrammars(FACTUR_X_EN16931, CII_D22B_XSD, reportType);
            compareCorrect &= compareTwoXsdGrammars(FACTUR_X_EXTENDED, CII_D22B_XSD, reportType);

            compareCorrect &= compareTwoXsdGrammars(CII_D16B_XSD, FACTUR_X_MINIMUM, reportType);
            compareCorrect &= compareTwoXsdGrammars(CII_D16B_XSD, FACTUR_X_BASIC_WL, reportType);
            compareCorrect &= compareTwoXsdGrammars(CII_D16B_XSD, FACTUR_X_BASIC, reportType);
            compareCorrect &= compareTwoXsdGrammars(CII_D16B_XSD, FACTUR_X_EN16931, reportType);
            compareCorrect &= compareTwoXsdGrammars(CII_D16B_XSD, FACTUR_X_EXTENDED, reportType);

            compareCorrect &= compareTwoXsdGrammars(simpleAnonymous1, simpleAnonymous2, reportType);
            compareCorrect &= compareTwoXsdGrammars(facets1, facets2, reportType);
        }


        assertTrue(compareCorrect,"\nRegression test fails as reference was different!\nNote: If the test fails due to a new output (e.g. programming update) copy the new result over the old reference:\n\t" + TARGET_DIR + "\n\t\tto" + "\n\t" + REFERENCES_DIR);
    }


    /** Comparing two XSD grammar with a specific text report for the output*/
    private boolean compareTwoXsdGrammars(String newXsd, String oldXsd, TextReport.implementation reportType) throws IOException {
        Boolean compareCorrect = Boolean.TRUE;
        String reportName = getReportName(newXsd, oldXsd, reportType);
        XsdComparer comparer = new XsdComparer(newXsd, oldXsd, reportType);
        String result = comparer.compareAsString();
        System.out.println(result);
        // if you change the programming, update the reference by copying new result as new reference!
        Files.writeString(Paths.get(new File(TARGET_DIR + reportName).toURI()), result, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        String resultReloaded = Files.readString(Paths.get(new File(TARGET_DIR + reportName).toURI()), Charset.forName("UTF-8"));
        File refFile = new File(REFERENCES_DIR + reportName);
        if(refFile.exists()) {
            String referenceResult = Files.readString(Paths.get(refFile.toURI()), Charset.forName("UTF-8"));
            if(!resultReloaded.equals(referenceResult)){
                System.err.println("\nRegression test fails as reference was different!\nNote: If the test fails due to a new output (e.g. programming update) copy the new result over the old reference:\n\t" + TARGET_DIR + reportName + "\n\t\tto" + "\n\t" + REFERENCES_DIR + reportName);
                compareCorrect = Boolean.FALSE;
            }
        }
        int added = comparer.getAdded();
        log.debug("Added to grammar: " + added);
        int modified = comparer.getModified();
        log.debug("Modified in grammar: " + modified);
        int removed = comparer.getRemoved();
        log.debug("Removed from grammar: " + removed);
        log.debug(comparer.toString());
        return compareCorrect;
    }

    /** Creates an output file (the report) in relation with the inputfile of the grammar and the report type.
        adding "report_" prefix and ".txt" suffix always and in case of not the default multiLine reporter some reportType related prefix in front of ".txt".
     */
    private static String getReportName(String newXsdPath,  String oldXsdPath, TextReport.implementation reporterType) {
        String reportName = "report_" + newXsdPath.substring(newXsdPath.lastIndexOf(File.separator) + 1, newXsdPath.lastIndexOf('.'))
                + "_to_" + oldXsdPath.substring(oldXsdPath.lastIndexOf(File.separator) + 1, oldXsdPath.lastIndexOf('.'));
        if(reporterType == TextReport.implementation.SINGLE_LINE) {
            reportName = reportName  + "_singleLineReport.txt";
        }else if(reporterType == TextReport.implementation.MULTI_LINE_CHANGE){
            reportName = reportName + ".txt";
        }else if(reporterType == TextReport.implementation.ONLY_RESTRICTIONS){
            reportName = reportName + "_onlyRestrictionsReport.txt";
        }else if(reporterType == TextReport.implementation.ONLY_EXTENSIONS){
            reportName = reportName + "_onlyExtensionsReport.txt";
        }else{
            log.error("Could not find report type: " + reporterType.toString());
        }
        return reportName;
    }

    @Test
    public void testCompare_shouldReturnTrue() throws IOException {
        ClassPathResource baseResource = new ClassPathResource("xsd/example_base_attribute.xsd");
        ClassPathResource additionalResource = new ClassPathResource("xsd/example_additional_attribute.xsd");
        XsdDocument baseDocument = xsdLoader.load(baseResource.getFile());
        XsdDocument additionalDocument = xsdLoader.load(additionalResource.getFile());
        XsdComparer comparer = new XsdComparer(baseDocument, additionalDocument);

        boolean result = comparer.compare();
        assert(result);
    }

    @Test
    public void testCompare_shouldMarkAttribute2AsAdded() throws IOException {
        ClassPathResource originalResource = new ClassPathResource("xsd/example_base_attribute.xsd");
        ClassPathResource newResource = new ClassPathResource("xsd/example_additional_attribute.xsd");
        XsdDocument originalDocument = xsdLoader.load(originalResource.getFile());
        XsdDocument newDocument = xsdLoader.load(newResource.getFile());
        XsdComparer comparer = new XsdComparer(originalDocument, newDocument);

        assertTrue(comparer.compare());

        XsdElementNode element = newDocument.getElementByName("MyRootElement");
        XsdNode attribute = element.findAttributeByName("attribute2");

        assertEquals(ChangeType.ADDED, attribute.getChange().getType());
    }

    @Test
    public void testCompare_shouldMarkAttribute2AsRemoved() throws IOException {
        ClassPathResource originalResource = new ClassPathResource("xsd/example_additional_attribute.xsd");
        ClassPathResource newResource = new ClassPathResource("xsd/example_base_attribute.xsd");
        XsdDocument originalDocument = xsdLoader.load(originalResource.getFile());
        XsdDocument newDocument = xsdLoader.load(newResource.getFile());
        XsdComparer comparer = new XsdComparer(originalDocument, newDocument);

        assertTrue(comparer.compare());

        XsdElementNode element = originalDocument.getElementByName("MyRootElement");
        XsdNode attribute = element.findAttributeByName("attribute2");

        assertEquals(ChangeType.REMOVED, attribute.getChange().getType());
    }
}
