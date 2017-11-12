package com.compare.xsd.model.xsd.impl;

import com.compare.xsd.model.comparison.ModificationType;
import com.compare.xsd.model.comparison.Modifications;
import com.compare.xsd.model.xsd.NodeNotFoundException;
import com.compare.xsd.model.xsd.XsdNode;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
     * Compare this node against the given node for changes.
     *
     * @param compareNode Set the new node.
     */
    public void compare(AbstractXsdElementNode compareNode) {
        Assert.notNull(compareNode, "compareNode cannot be null");
        List<XsdElement> elementsCopy = new ArrayList<>(elements); //take a copy as the actual list might be modified during comparison
        List<XsdElement> compareElementsCopy = new ArrayList<>(compareNode.getElements()); //take a copy as the actual list might be modified during comparison

        //check for removed nodes
        for (XsdElement element : elementsCopy) {
            try {
                if (StringUtils.isNoneEmpty(element.getName())) {
                    XsdElement compareElement = compareNode.findElement(element.getName());

                    element.compare(compareElement);
                }
            } catch (NodeNotFoundException ex) {
                element.setModifications(new Modifications(ModificationType.REMOVED));
                copyElementAsEmptyNode(elements.indexOf(element), element, compareNode);
            }
        }

        //check for added nodes
        for (XsdElement element : compareElementsCopy) {
            try {
                if (StringUtils.isNoneEmpty(element.getName())) {
                    this.findElement(element.getName());
                }
            } catch (NodeNotFoundException ex) {
                element.setModifications(new Modifications(ModificationType.ADDED));
                copyElementAsEmptyNode(compareNode.getElements().indexOf(element), element, this);
            }
        }

        this.compareProperties(compareNode);
    }

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

    //region Functions

    protected XsdEmptyElementNode deepCopyEmptyElementNodes(XsdNode toCopyNode) {
        XsdEmptyElementNode emptyNode = new XsdEmptyElementNode();

        for (XsdNode element : toCopyNode.getNodes()) {
            emptyNode.addNode(deepCopyEmptyElementNodes(element));
        }

        return emptyNode;
    }

    /**
     * Copy the element and inner elements of the given to copy node to the destination element at given index.
     *
     * @param index           Set the index to add the nodes at.
     * @param toCopyNode      Set the node to deep copy.
     * @param destinationNode Set the destination of the copied nodes.
     */
    private void copyElementAsEmptyNode(int index, AbstractXsdElementNode toCopyNode, AbstractXsdElementNode destinationNode) {
        destinationNode.insertElementAt(index, deepCopyEmptyElementNodes(toCopyNode));
    }

    //endregion
}
