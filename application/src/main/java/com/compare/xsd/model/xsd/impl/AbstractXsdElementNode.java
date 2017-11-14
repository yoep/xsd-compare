package com.compare.xsd.model.xsd.impl;

import com.compare.xsd.model.xsd.NodeNotFoundException;
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
     * Find the direct child element by the given name.
     *
     * @param name Set the name of the element to search for.
     * @return Returns the found XSD element.
     * @throws NodeNotFoundException Is thrown when the given element name couldn't be found back.
     */
    public XsdElement findElement(String name) throws NodeNotFoundException {
        Assert.hasText(name, "name cannot be empty");

        return elements.stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new NodeNotFoundException(name));
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
}
