package com.compare.xsd.settings.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Contains generic functionality that is shared between the different setting options.
 * This includes handling of defaults and changes to the settings.
 */
public abstract class AbstractSettings implements Settings {
    @JsonIgnore
    protected final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    @Override
    public void addListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }
}
