package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.NodeNotFoundException;
import com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class XsdElementTest {
    @Mock
    private XSElementDecl elementDecl;
    @Mock
    private XsdDocument document;
    @Mock
    private XSTypeDefinition typeDefinition;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private XsdElement element;

    @Before
    public void setup() {
        when(elementDecl.getTypeDefinition()).thenReturn(typeDefinition);

        element = new XsdElement(elementDecl, document);
    }

    @Test
    public void testFindAttributeByName_shouldThrowIllegalArgumentException_whenNameIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("name cannot be null");

        new XsdElement().findAttributeByName(null);
    }

    @Test
    public void testFindAttributeByName_shouldThrowNodeNotFoundException_whenAttributeDoesNotExist() {
        String name = "attribute985654";
        expectedException.expect(NodeNotFoundException.class);
        expectedException.expectMessage("Node couldn't be found with name '" + name + "'");

        assertNotNull(element.findAttributeByName(name));
    }
}