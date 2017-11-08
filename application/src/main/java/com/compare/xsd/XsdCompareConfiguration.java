package com.compare.xsd;

import com.compare.xsd.managers.ViewManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XsdCompareConfiguration {
    @Bean
    public ViewManager viewManager() {
        return new ViewManager();
    }
}
