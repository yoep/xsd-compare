package com.compare.xsd.comparison;

import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;
import com.github.spring.boot.javafx.view.ViewManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class XsdLoaderTest {
    @Mock
    private ViewManager viewManager;
    @InjectMocks
    private XsdLoader xsdLoader;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testLoad_shouldThrowIllegalArgumentExceptionWhenFileIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("file cannot be null");

        xsdLoader.load(null);
    }

    @Test
    public void testLoad_shouldThrowXsdLoadExceptionWhenFileDoesNotExist() {
        expectedException.expect(XsdLoadException.class);
        expectedException.expectMessage("Failed to load XSD file");

        xsdLoader.load(new File("randomFileNameThatDoesNotExist.xsd"));
    }

    @Test
    public void testLoad_shouldLoadXsdDocumentWhenFileExists() throws IOException {
        ClassPathResource resource = new ClassPathResource("xsd/simple_example.xsd");

        XsdDocument document = xsdLoader.load(resource.getFile());

        assertNotNull(document);
    }
}