package com.compare.xsd.settings;

import com.compare.xsd.XsdCompareApplication;
import com.compare.xsd.settings.model.ApplicationSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {
    private final ObjectMapper objectMapper;

    private ApplicationSettings currentSettings;

    //region Getters

    /**
     * Get the current application settings.
     *
     * @return Returns the application settings.
     */
    public ApplicationSettings getSettings() {
        return currentSettings;
    }

    //endregion

    //region Methods

    /**
     * Save the current application settings to the settings file.
     */
    public void save() {
        save(currentSettings);
    }

    /**
     * Save the given user settings.
     *
     * @param settings Set the user settings to save.
     * @throws SettingsException Is thrown when an error occurs during writing.
     */
    public void save(ApplicationSettings settings) throws SettingsException {
        Assert.notNull(settings, "settings cannot be null");
        var settingsFile = getSettingsFile();

        try {
            log.info("Saving user settings to " + settingsFile.getAbsolutePath());
            FileUtils.writeStringToFile(settingsFile, objectMapper.writeValueAsString(settings), Charset.defaultCharset());
            currentSettings = settings;
        } catch (IOException ex) {
            throw new SettingsException("Unable to write settings to " + settingsFile.getAbsolutePath(), ex);
        }
    }

    //endregion

    //region PostConstruct

    @PostConstruct
    private void init() {
        initializeSettingsDirectory();
        initializeSettings();
    }

    void initializeSettingsDirectory() {
        var appDir = new File(XsdCompareApplication.APP_DIR);

        if (!appDir.exists()) {
            if (!appDir.mkdirs()) {
                log.error("Unable to create application directory in " + appDir.getAbsolutePath());
            }
        }
    }

    void initializeSettings() {
        currentSettings = loadUserSettingsFromFile()
                .orElseGet(() -> ApplicationSettings.builder().build());
    }

    //endregion

    //region PreDestroy

    @PreDestroy
    private void onDestroy() {
        save();
    }

    //endregion

    //region Functions

    private Optional<ApplicationSettings> loadUserSettingsFromFile() {
        var settingsFile = getSettingsFile();

        if (settingsFile.exists()) {
            try {
                log.info("Loading user settings from " + settingsFile.getAbsolutePath());

                var applicationSettings = objectMapper.readValue(settingsFile, ApplicationSettings.class);

                return Optional.of(applicationSettings);
            } catch (IOException ex) {
                throw new SettingsException("Unable to read settings file at " + settingsFile.getAbsolutePath(), ex);
            }
        } else {
            return Optional.empty();
        }
    }

    private File getSettingsFile() {
        return new File(XsdCompareApplication.APP_DIR + "settings.json");
    }

    //endregion
}
