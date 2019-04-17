package com.compare.xsd.comparison;

import com.compare.xsd.renderers.PropertyViewRender;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class PropertyViewManager extends AbstractScrollBarSynchronizeManager {
    private PropertyViewRender leftProperties;
    private PropertyViewRender rightProperties;

    public void synchronize() {
        this.synchronize(leftProperties, rightProperties);
    }

    /**
     * Clear all property rendering views.
     */
    public void clearAll() {
        leftProperties.clear();
        rightProperties.clear();
    }
}
