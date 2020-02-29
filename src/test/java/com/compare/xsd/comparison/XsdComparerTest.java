package com.compare.xsd.comparison;

import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;
import com.github.spring.boot.javafx.view.ViewManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class XsdComparerTest {
    @Mock
    private ViewManager viewManager;

    private XsdLoader xsdLoader;

    @Before
    public void setup() {
        xsdLoader = new XsdLoader(viewManager);
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
}