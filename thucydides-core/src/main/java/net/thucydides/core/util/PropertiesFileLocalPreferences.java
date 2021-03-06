package net.thucydides.core.util;

import com.google.inject.Inject;
import net.thucydides.core.ThucydidesSystemProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Thucydides options can be loaded from the thucydides.properties file in the home directory.
 * <p/>
 * User: johnsmart
 * Date: 24/12/11
 * Time: 11:15 AM
 */
public class PropertiesFileLocalPreferences implements LocalPreferences {

    private File homeDirectory;
    private final EnvironmentVariables environmentVariables;

    @Inject
    public PropertiesFileLocalPreferences(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
        this.homeDirectory = new File(System.getProperty("user.home"));
    }

    public File getHomeDirectory() {
        return homeDirectory;
    }

    public void setHomeDirectory(File homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    @Override
    public void loadPreferences() throws IOException {
        File localPreferencesFile = getLocalPreferencesFile();
        if (localPreferencesFile.exists()) {
            Properties localPreferences = new Properties();
            localPreferences.load(new FileInputStream(localPreferencesFile));
            setUndefinedSystemPropertiesFrom(localPreferences);
        }

    }

    private void setUndefinedSystemPropertiesFrom(Properties localPreferences) {
        for (ThucydidesSystemProperty thucydidesProperty : ThucydidesSystemProperty.values()) {
            String propertyName = thucydidesProperty.getPropertyName();
            String localPropertyValue = localPreferences.getProperty(propertyName);
            String currentPropertyValue = environmentVariables.getProperty(propertyName);

            if ((currentPropertyValue == null) && (localPropertyValue != null)) {
                environmentVariables.setProperty(propertyName, localPropertyValue);
            }
        }
    }

    private File getLocalPreferencesFile() {
        return new File(homeDirectory, "thucydides.properties");

    }
}
