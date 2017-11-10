package com.compare.xsd.model.xsd.impl;

import com.compare.xsd.model.comparison.ModificationType;
import com.compare.xsd.model.comparison.Modifications;
import com.compare.xsd.model.xsd.ElementNotFoundException;
import com.compare.xsd.model.xsd.XsdNode;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractXsdElementNode extends AbstractXsdNode {
    protected final List<XsdElement> elements = new ArrayList<>();

    //region Constructors

    /**
     * Initialize a new instance of {@link AbstractXsdElementNode}.
     *
     * @param parent Set the parent of this node.
     */
    protected AbstractXsdElementNode(AbstractXsdNode parent) {
        super(parent);
    }

    //endregion

    //region Getters & Setters

    @Override
    public List<XsdNode> getNodes() {
        return new ArrayList<>(elements);
    }

    //endregion

    //region Methods

    /**
     * Compare this document against the given document for changes.
     *
     * @param compareNode Set the new document.
     */
    public void compare(AbstractXsdElementNode compareNode) {
        Assert.notNull(compareNode, "newDocument cannot be null");

        for (XsdElement element : elements) {
            try {
                XsdElement compareElement = compareNode.findElement(element.getName());

                element.compare(compareElement);
            } catch (ElementNotFoundException ex) {
                element.setModifications(new Modifications(ModificationType.Removed));
                insertEmptyNode(elements.indexOf(element), compareNode);
            }
        }
    }

    /**
     * Find the direct child element by the given name.
     *
     * @param name Set the name of the element to search for.
     * @return Returns the found XSD element.
     * @throws ElementNotFoundException Is thrown when the given element name couldn't be found back.
     */
    public XsdElement findElement(String name) throws ElementNotFoundException {
        Assert.hasText(name, "name cannot be empty");

        return elements.stream()
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new ElementNotFoundException(name));
    }

    /**
     * Insert the given element at the given index in this node.
     *
     * @param index   Set the index to add the element at.
     * @param element Set the element to add at the given index.
     */
    public void insertElementAt(int index, XsdElement element) {
        Assert.notNull(element, "element cannot be null");

        this.elements.add(index, element);
    }

    //endregion

    //region Functions

    private void insertEmptyNode(int index, AbstractXsdElementNode compareNode) {
        compareNode.insertElementAt(index, new XsdEmptyNode());
    }

    //endregion
}
