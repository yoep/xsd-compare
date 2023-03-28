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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class XsdComparerTest {
    @Mock
    private ViewManager viewManager;

    private XsdLoader xsdLoader;

    @BeforeEach
    public void setup() {
        xsdLoader = new XsdLoader(viewManager);
    }


    @Test
    public void testCompare_recursiveGrammar() throws IOException {
        ClassPathResource originalResource = new ClassPathResource("xsd/EN16931/data/standard/CrossIndustryInvoice_100pD16B.xsd");
        ClassPathResource newResource = new ClassPathResource("xsd/uncefact_22B_20230324/CrossIndustryInvoice_100pD22B.xsd");

        XsdDocument originalGrammar = xsdLoader.load(originalResource.getFile());
        log.debug("Finished loading original grammar!");
        XsdDocument newGrammar = xsdLoader.load(newResource.getFile());
        log.debug("Finished loading new grammar!");
        XsdComparer comparer = new XsdComparer(originalGrammar, newGrammar);
        boolean result = comparer.compare();
        assertTrue(result);
        int added = comparer.getAdded();
        log.debug("Added to grammar: " + added);
        int modified = comparer.getModified();
        log.debug("Modified in grammar: " + modified);
        int removed = comparer.getRemoved();
        log.debug("Removed from grammar: " + removed);
    }


    @Test
    public void testCompare_shouldReturnTrue() throws IOException {
        ClassPathResource baseResource = new ClassPathResource("xsd/example_base_attribute.xsd");
        ClassPathResource additionalResource = new ClassPathResource("xsd/example_additional_attribute.xsd");
        XsdDocument baseDocument = xsdLoader.load(baseResource.getFile());
        XsdDocument additionalDocument = xsdLoader.load(additionalResource.getFile());
        XsdComparer comparer = new XsdComparer(baseDocument, additionalDocument);

        boolean result = comparer.compare();

        assertTrue(result);
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
