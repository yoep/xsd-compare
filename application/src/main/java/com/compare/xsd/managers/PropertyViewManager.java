package com.compare.xsd.managers;

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
}
