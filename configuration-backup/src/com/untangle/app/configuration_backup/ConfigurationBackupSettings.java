/**
 * $Id$
 */
package com.untangle.app.configuration_backup;

import java.io.Serializable;

/**
 * Settings for the ConfigurationBackup app.
 */
@SuppressWarnings("serial")
public class ConfigurationBackupSettings implements Serializable
{
    private int hourInDay;
    private int minuteInHour;
    private boolean googleDriveEnabled = true;
    private String  googleDriveDirectory = "Configuration Backups";
    
    public ConfigurationBackupSettings() { }

    public int getHourInDay() { return hourInDay; }
    public void setHourInDay( int newValue ) { this.hourInDay = newValue; }

    public int getMinuteInHour() { return minuteInHour; }
    public void setMinuteInHour( int newValue ) { this.minuteInHour = newValue; }

    public boolean getGoogleDriveEnabled() { return googleDriveEnabled; }
    public void setGoogleDriveEnabled( boolean newValue ) { this.googleDriveEnabled = newValue; }

    public String getGoogleDriveDirectory() { return googleDriveDirectory; }
    public void setGoogleDriveDirectory( String newValue ) { this.googleDriveDirectory = newValue; }
}
