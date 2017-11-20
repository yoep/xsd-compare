package com.compare.xsd.model.xsd.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class XsdDocumentTest {
    @Mock
    private File file;

    @Before
    public void setUp() {
        when(file.getAbsolutePath()).thenReturn(getClass().getResource("/xsd/simple_example.xsd").getFile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenFileIsNull() {
        //WHEN
        new XsdDocument(null);
    }

    @Test
    public void shouldCallGetNameOnFileWhenLoaded() {
        //WHEN
        new XsdDocument(file);

        //THEN
        verify(file).getName();
    }

    @Test
    public void shouldContainExpectedRootElementWhenLoaded() {
        //GIVEN
        final String name = "MyRootElement";

        //WHEN
        XsdDocument result = new XsdDocument(file);

        //THEN
        assertEquals(1, result.getElements().size());
        assertEquals(name, result.getElements().get(0).getName());
    }

    @Test
    public void shouldRootElementContainExpectedChildElementsWhenLoaded() {
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
        final String expectedResult = "//";

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
        assertNull(result.getType());
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
}