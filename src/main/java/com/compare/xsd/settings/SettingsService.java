package com.compare.xsd.settings;

import com.compare.xsd.XsdCompareApplication;
import com.compare.xsd.common.ObservableWrapper;
import com.compare.xsd.settings.model.UserSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class SettingsService {
    private final ObjectMapper objectMapper;
    @Getter
    private final ObservableWrapper<UserSettings> userSettingsObservable = new ObservableWrapper<>();

    @PostConstruct
    public void init() {
        createApplicationSettingsDirectory();
    }

    @PreDestroy
    public void onDestroy() {
        save(getUserSettingsOrDefault());
    }

    /**
     * Save the given user settings.
     *
     * @param settings Set the user settings to save.
     * @throws SettingsException Is thrown when an error occurs during writing.
     */
    public void save(UserSettings settings) throws SettingsException {
        Assert.notNull(settings, "settings cannot be null");
        File settingsFile = getSettingsFile();

        try {
            log.info("Saving user settings to " + settingsFile.getAbsolutePath());
            FileUtils.writeStringToFile(settingsFile, objectMapper.writeValueAsString(settings), Charset.defaultCharset());
            userSettingsObservable.set(settings);
        } catch (IOException ex) {
            throw new SettingsException("Unable to write settings to " + settingsFile.getAbsolutePath(), ex);
        }
    }

    /**
     * Get the user settings if present.
     *
     * @return Returns the user settings.
     * @throws SettingsException Is thrown when the user settings exist but couldn't be read.
     */
    public Optional<UserSettings> getUserSettings() throws SettingsException {
        if (userSettingsObservable.get() != null) {
            return Optional.of(userSettingsObservable.get());
        } else {
            return loadUserSettingsFromFile();
        }
    }

    /**
     * Get the user settings or the default settings if they don't exist yet.
     *
     * @return Returns the user settings.
     * @throws SettingsException Is thrown when the user settings exist but couldn't be read.
     */
    public UserSettings getUserSettingsOrDefault() throws SettingsException {
        return getUserSettings().orElseGet(() -> {
            UserSettings defaultSettings = UserSettings.builder().build();
            userSettingsObservable.set(defaultSettings);
            return defaultSettings;
        });
    }

    private Optional<UserSettings> loadUserSettingsFromFile() {
        File settingsFile = getSettingsFile();

        if (settingsFile.exists()) {
            try {
                log.info("Loading user settings from " + settingsFile.getAbsolutePath());

                UserSettings userSettings = objectMapper.readValue(settingsFile, UserSettings.class);
                userSettingsObservable.set(userSettings);

                return Optional.of(userSettings);
            } catch (IOException ex) {
                throw new SettingsException("Unable to read settings file at " + settingsFile.getAbsolutePath(), ex);
            }
        } else {
            return Optional.empty();
        }
    }

    private void createApplicationSettingsDirectory() {
        File appDir = new File(XsdCompareApplication.APP_DIR);

        if (!appDir.exists()) {
            if (!appDir.mkdirs()) {
                log.error("Unable to create application directory in " + appDir.getAbsolutePath());
            }
        }
    }

    private File getSettingsFile() {
        return new File(XsdCompareApplication.APP_DIR + "settings.json");
    }
}
