package com.compare.xsd.comparison.model.xsd.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class XsdDocumentTest {
    @Mock
    private File file;

    @BeforeEach
    public void setUp() {
        lenient().when(file.getAbsolutePath()).thenReturn(getClass().getResource("/xsd/simple_example.xsd").getFile());
    }

    @Test
    public void testFileConstructor_shouldThrowIllegalArgumentExceptionWhenFileIsNull() {
        //WHEN
        assertThrows(IllegalArgumentException.class, () -> new XsdDocument(null));
    }

    @Test
    public void testFileConstructor_shouldCallGetNameOnFileWhenLoaded() {
        //WHEN
        new XsdDocument(file);

        //THEN
        verify(file).getName();
    }

    @Test
    public void testFileConstructor_shouldContainExpectedRootElementWhenLoaded() {
        //GIVEN
        final String name = "MyRootElement";

        //WHEN
        XsdDocument result = new XsdDocument(file);

        //THEN
        assertEquals(1, result.getElements().size());
        assertEquals(name, result.getElements().get(0).getName());
    }

    @Test
    public void testFileConstructor_shouldRootElementContainExpectedChildElementsWhenLoaded() {
        //GIVEN
        final String child1 = "FirstChildElement";
        final String child2 = "SecondChildElement";

        //WHEN
        XsdElement result = new XsdDocument(file).getElements().get(0);

        //THEN
        assertEquals(2, result.getElements().size());
        assertEquals(child1, result.getElements().get(0).getName());
        assertEquals(child2, result.getElements().get(1).getName());
    }

    @Test
    public void shouldReturnTheExpectedResultWhenGetXPathIsCalled() {
        //GIVEN
        final String expectedResult = "/";

        //WHEN
        XsdDocument result = new XsdDocument(file);

        //THEN
        assertEquals(expectedResult, result.getXPath());
    }

    @Test
    public void shouldReturnTypeNullWhenGetTypeIsCalled() {
        //WHEN
        XsdDocument result = new XsdDocument(file);

        //THEN
        assertNull(result.getTypeName());
    }

    @Test
    public void shouldReturnTypeNullWhenGetCardinalityIsCalled() {
        //WHEN
        XsdDocument result = new XsdDocument(file);

        //THEN
        assertNull(result.getCardinality());
    }

    @Test
    public void shouldNotReturnNullWhenGetIconIsCalled() {
        //WHEN
        XsdDocument result = new XsdDocument(file);

        //THEN
        assertNotNull(result.getIcon());
    }

    @Test
    public void testFindAttributeByName_shouldThrowUnsupportedException() {
        XsdDocument document = new XsdDocument(file);

        assertThrows(UnsupportedOperationException.class, () -> document.findAttributeByName("test"), "getAttributeByName is not supported for XsdDocument");
    }
}
