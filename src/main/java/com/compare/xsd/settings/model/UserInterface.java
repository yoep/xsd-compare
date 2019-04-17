package com.compare.xsd.settings.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInterface {
    @Builder.Default
    private boolean maximized = true;
    @Builder.Default
    private float width = 800f;
    @Builder.Default
    private float height = 600f;
    @Builder.Default
    private float scale = 1f;
}
