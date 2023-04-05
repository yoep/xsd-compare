package com.compare.xsd.comparison;

import com.compare.xsd.comparison.model.ModificationType;
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

    private static final String REPORT_CII_D16D_WITH_D22B = "comparison-CII_D16B-with-D22B.txt";




    @BeforeEach
    public void setup() {
        xsdLoader = new XsdLoader(viewManager);
    }


    @Test
    public void testCompare_recursiveGrammar() throws IOException {
        File oldGrammarFile = new File(CII_D16B_XSD);
        File newGrammarFile = new File(CII_D22B_XSD);
        
        XsdDocument oldGrammar = xsdLoader.load(oldGrammarFile);
        log.debug("Finished loading original grammar!");
        XsdDocument newGrammar = xsdLoader.load(newGrammarFile);
        log.debug("Finished loading new grammar!");
        XsdComparer comparer = new XsdComparer(oldGrammar, newGrammar);
        String result = comparer.compareAsString();
        System.out.println(result);
        // if you change the programming, update the reference by copying new result as new reference!
        Files.writeString(Paths.get(new File(TARGET_DIR + REPORT_CII_D16D_WITH_D22B).toURI()), result, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        String resultReloaded = Files.readString(Paths.get(new File(TARGET_DIR + REPORT_CII_D16D_WITH_D22B).toURI()), Charset.forName("UTF-8"));
        String referenceResult = Files.readString(Paths.get(new File(REFERENCES_DIR + REPORT_CII_D16D_WITH_D22B).toURI()), Charset.forName("UTF-8"));
        assertTrue(resultReloaded.equals(referenceResult), "\nIf the test fails due to a new output (e.g. programming update) copy the new result over the old reference:\n\t" + TARGET_DIR + REPORT_CII_D16D_WITH_D22B +  "\n\t\tto" + "\n\t" + REFERENCES_DIR + REPORT_CII_D16D_WITH_D22B);

        int added = comparer.getAdded();
        log.debug("Added to grammar: " + added);
        int modified = comparer.getModified();
        log.debug("Modified in grammar: " + modified);
        int removed = comparer.getRemoved();
        log.debug("Removed from grammar: " + removed);
        log.debug(comparer.toString());
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

        assertEquals(ModificationType.ADDED, attribute.getModifications().getType());
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

        assertEquals(ModificationType.REMOVED, attribute.getModifications().getType());
    }
}
