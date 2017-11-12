package com.compare.xsd.managers;

import com.compare.xsd.renderers.PropertyViewRender;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class PropertyViewManager {
    private PropertyViewRender leftProperties;
    private PropertyViewRender rightProperties;
}
