package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.NodeNotFoundException;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.xs.XSTypeDefinition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class XsdElementTest {
    @Mock
    private XSElementDecl elementDecl;
    @Mock
    private XsdDocument document;
    @Mock
    private XSTypeDefinition typeDefinition;

    private XsdElement element;

    @BeforeEach
    public void setup() {
        element = new XsdEmptyElementNode(document);
    }

    @Test
    @Disabled
    public void testFindAttributeByName_shouldThrowIllegalArgumentException_whenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> element.findAttributeByName(null), "name cannot be null");
    }

    @Test
    @Disabled
    public void testFindAttributeByName_shouldThrowNodeNotFoundException_whenAttributeDoesNotExist() {
        String name = "attribute985654";

        assertThrows(NodeNotFoundException.class, () -> element.findAttributeByName(name), "Node couldn't be found with name '" + name + "'");
    }
}
