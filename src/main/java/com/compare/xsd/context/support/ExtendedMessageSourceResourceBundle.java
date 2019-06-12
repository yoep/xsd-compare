package com.compare.xsd.context.support;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceResourceBundle;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Extends the {@link MessageSourceResourceBundle} as it doesn't look for the key in the parent resource bundle.
 * This extension also checks the parent resource bundle.
 */
public class ExtendedMessageSourceResourceBundle extends MessageSourceResourceBundle {
    private MessageSource source;
    private ResourceBundle parent;
    private Locale locale;

    public ExtendedMessageSourceResourceBundle(MessageSource source, Locale locale, ResourceBundle parent) {
        super(source, locale, parent);
        this.source = source;
        this.locale = locale;
        this.parent = parent;
    }

    @Override
    public boolean containsKey(String key) {
        boolean keyExists = false;

        try {
            this.source.getMessage(key, null, locale);
            keyExists = true;
        } catch (NoSuchMessageException ex) {
            // no-op
        }

        try {
            this.parent.getString(key);
            keyExists = true;
        } catch (MissingResourceException ex) {
            // no-op
        }

        return keyExists;
    }
}
