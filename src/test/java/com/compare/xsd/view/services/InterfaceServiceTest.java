package com.compare.xsd.view.services;

import com.compare.xsd.settings.SettingsService;
import com.compare.xsd.settings.model.ApplicationSettings;
import com.compare.xsd.settings.model.UserInterface;
import com.github.spring.boot.javafx.view.ViewLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Disabled;
import java.beans.PropertyChangeListener;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterfaceServiceTest {
    @Mock
    private SettingsService settingsService;
    @Mock
    private ViewLoader viewLoader;
    @InjectMocks
    private InterfaceService interfaceService;

    @Test
    @Disabled
    void testInitializeScale_whenInvoked_shouldSetTheInitialScale() {
        var scale = 2.5f;
        var settings = mock(ApplicationSettings.class);
        var interfaceSettings = UserInterface.builder()
                .scale(scale)
                .build();
        when(settingsService.getSettings()).thenReturn(settings);
        when(settings.getUserInterface()).thenReturn(interfaceSettings);

        interfaceService.initializeScale();

        verify(viewLoader).setScale(scale);
    }

    @Test
    @Disabled
    void testInitializeScale_whenInvoked_shouldRegisterListener() {
        var settings = mock(ApplicationSettings.class);
        var interfaceSettings = mock(UserInterface.class);
        when(settingsService.getSettings()).thenReturn(settings);
        when(settings.getUserInterface()).thenReturn(interfaceSettings);

        interfaceService.initializeScale();

        verify(interfaceSettings).addListener(isA(PropertyChangeListener.class));
    }
}
