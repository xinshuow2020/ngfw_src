/**
 * $Id: DashboardManagerImpl.java,v 1.00 2015/11/10 14:31:00 dmorris Exp $
 */
package com.untangle.uvm;

import org.apache.log4j.Logger;

import com.untangle.uvm.UvmContextFactory;
import com.untangle.uvm.SettingsManager;
import com.untangle.uvm.DashboardSettings;


/**
 * The Manager for the dashboard
 */
public class DashboardManagerImpl implements DashboardManager
{
    private static final Logger logger = Logger.getLogger(DashboardManagerImpl.class);

    private DashboardSettings settings = null; 
    
    protected DashboardManagerImpl()
    {
        SettingsManager settingsManager = UvmContextFactory.context().settingsManager();
        DashboardSettings readSettings = null;
        String settingsFileName = System.getProperty("uvm.settings.dir") + "/untangle-vm/" + "dashboard.js";

        try {
            readSettings = settingsManager.load( DashboardSettings.class, settingsFileName );
        } catch (SettingsManager.SettingsException e) {
            logger.warn("Failed to load settings:",e);
        }

        /**
         * If there are still no settings, just initialize
         */
        if (readSettings == null) {
            logger.warn("No settings found - Initializing new settings.");
            this.setSettings(defaultSettings());
        }
        else {
            this.settings = readSettings;
            logger.debug("Loading Settings: " + this.settings.toJSONString());
        }

        logger.info("Initialized DashboardManager");
    }

    public DashboardSettings getSettings()
    {
        return this.settings;
    }

    public void setSettings(final DashboardSettings newSettings)
    {
        /**
         * Save the settings
         */
        SettingsManager settingsManager = UvmContextFactory.context().settingsManager();
        try {
            settingsManager.save( System.getProperty("uvm.settings.dir") + "/" + "untangle-vm/" + "dashboard.js", newSettings );
        } catch (SettingsManager.SettingsException e) {
            logger.warn("Failed to save settings.",e);
            return;
        }

        /**
         * Change current settings
         */
        this.settings = newSettings;
        try {logger.debug("New Settings: \n" + new org.json.JSONObject(this.settings).toString(2));} catch (Exception e) {}
    }

    private DashboardSettings defaultSettings()
    {
        return new DashboardSettings();
    }
}