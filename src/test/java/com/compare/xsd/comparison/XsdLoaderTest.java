package com.compare.xsd.comparison;

import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;
import com.github.spring.boot.javafx.view.ViewManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class XsdLoaderTest {
    @Mock
    private ViewManager viewManager;
    @InjectMocks
    private XsdLoader xsdLoader;

    @Test
    @Disabled
    public void testLoad_shouldThrowIllegalArgumentExceptionWhenFileIsNull() {
        assertThrows(IllegalArgumentException.class, () -> xsdLoader.load(null), "file cannot be null");
    }

    @Test
    @Disabled
    public void testLoad_shouldThrowXsdLoadExceptionWhenFileDoesNotExist() {
        assertThrows(XsdLoadException.class, () -> xsdLoader.load(new File("randomFileNameThatDoesNotExist.xsd")), "Failed to load XSD file");
    }

    @Test
    @Disabled
    public void testLoad_shouldLoadXsdDocumentWhenFileExists() throws IOException {
        ClassPathResource resource = new ClassPathResource("xsd/simple_example.xsd");

        XsdDocument document = xsdLoader.load(resource.getFile());

        assertNotNull(document);
    }
}
