Ext.define('Webui.config.upgrade', {
    extend: 'Ung.ConfigWin',
    panelSettings: null,
    initComponent: function() {
        this.breadcrumbs = [{
            title: i18n._("Configuration"),
            action: Ext.bind(function() {
                this.cancelAction();
            }, this)
        }, {
            title: i18n._('Upgrade')
        }];
        this.buildSettings();
        // builds the tab panel with the tabs
        this.buildTabPanel([this.panelSettings]);
        this.callParent(arguments);
    },
    afterRender: function() {
        this.callParent(arguments);
        Ung.Util.clearDirty(this.panelSettings);
        var checkUpgradesProgressbar = this.panelSettings.down('[name="checkUpgradesProgressbar"]');
        checkUpgradesProgressbar.wait({
            text: i18n._("Checking for upgrades...")
        });
        rpc.systemManager.upgradesAvailable(Ext.bind(function(result, exception) {
            if(Ung.Util.handleException(exception)) return;
            if(!this || !this.isVisible()) {
                return;
            }
            checkUpgradesProgressbar.reset();
            checkUpgradesProgressbar.hide();
            var statusDescription = result? '<i><font color="green">' + i18n._("Upgrades are available!") + ' </font></i>' :
                '<font color="grey">' + i18n._("No upgrades available.") + ' </font>';
            var statusCmp = this.panelSettings.down('[name="statusMessage"]');
            statusCmp.update(statusDescription);
            statusCmp.show();
            if(result) {
                var upgradeButton = this.panelSettings.down('[name="upgradeButton"]');
                upgradeButton.show();
            }
        }, this));
    },
    getSystemSettings: function(forceReload) {
        if (forceReload || this.rpc.systemSettings === undefined) {
            try {
                this.rpc.systemSettings = rpc.systemManager.getSettings();
            } catch (e) {
                Ung.Util.rpcExHandler(e);
            }
        }
        return this.rpc.systemSettings;
    },
    getDownloadStatus: function() {
        if(!this.checkDownloadStatus) {
            return;
        }
        rpc.systemManager.getDownloadStatus(Ext.bind(function(result, exception) {
            if(Ung.Util.handleException(exception)) return;
            if(!this.checkDownloadStatus) {
                return;
            }
            var text=Ext.String.format(i18n._("Package: {0} of {1}<br/>Speed: {2}"),
                       result.downloadCurrentFileCount,
                       result.downloadTotalFileCount,
                       result.downloadCurrentFileRate);
            if(!Ext.MessageBox.isVisible() || Ext.MessageBox.title!=this.msgTitle) {
                Ext.MessageBox.progress(this.msgTitle, text);
            }
            var downloadCurrentFileProgress = 0;
            if(result.downloadCurrentFileProgress!=null && result.downloadCurrentFileProgress.length>0) {
                try {
                    downloadCurrentFileProgress = parseFloat(result.downloadCurrentFileProgress.replace("%",""))/100;
                } catch (e) {
                }
            }
            var downloadCurrentFileIndex = parseFloat(result.downloadCurrentFileCount);
            if(downloadCurrentFileIndex > 0) {
                downloadCurrentFileIndex = downloadCurrentFileIndex - 1;
            }
            var currentPercentComplete = (downloadCurrentFileIndex + downloadCurrentFileProgress) / parseFloat(result.downloadTotalFileCount != 0 ? result.downloadTotalFileCount: 1);
            //console.log("downloadCurrentFileProgress:", downloadCurrentFileProgress,"currentPercentComplete",currentPercentComplete,"downloadCurrentFileCount",result.downloadCurrentFileCount);
            var progressIndex = parseFloat(0.99 * currentPercentComplete);
            Ext.MessageBox.updateProgress(progressIndex, "", text);
            window.setTimeout( Ext.bind(this.getDownloadStatus, this), 500 );
        }, this));
    },
    downloadUpgrades: function() {
        this.checkDownloadStatus=true;
        this.msgTitle=i18n._("Downloading packages... Please wait");
        Ext.MessageBox.progress(this.msgTitle, ".");
        rpc.systemManager.downloadUpgrades(Ext.bind(function(result, exception) {
            if(Ung.Util.handleException(exception)) return;
            this.checkDownloadStatus=false;
            //console.log("rpc.systemManager.downloadUpgrades", result);
            Ext.MessageBox.hide();
            if(result) {
                Ung.Main.upgrade();
            } else {
                Ext.MessageBox.alert(i18n._("Warning"), i18n._("Downloading upgrades failed."));
            }
        }, this));
        this.getDownloadStatus();
    },
    buildSettings: function() {
        var upgradeTime=new Date();
        upgradeTime.setTime(0);
        upgradeTime.setHours(this.getSystemSettings().autoUpgradeHour);
        upgradeTime.setMinutes(this.getSystemSettings().autoUpgradeMinute);
        this.panelSettings = Ext.create('Ext.panel.Panel',{
            name: 'Upgrade Settings',
            helpSource: 'upgrade_upgrade_settings',
            title: i18n._('Upgrade Settings'),
            cls: 'ung-panel',
            autoScroll: true,
            defaults: {
                xtype: 'fieldset'
            },
            items: [{
                title: i18n._('Status'),
                items: [{
                    xtype: 'progressbar',
                    name: 'checkUpgradesProgressbar',
                    width: 300
                }, {
                    xtype: 'component',
                    name: "statusMessage",
                    html: "<i>" + i18n._("Checking for upgrades...") + "</i>",
                    hidden: true,
                    margin: '0 25 10 0'
                }, {
                    xtype: "button",
                    name: 'upgradeButton',
                    hidden: true,
                    text: i18n._("Upgrade"),
                    iconCls: "icon-upgrade",
                    handler: Ext.bind(function() {
                        this.downloadUpgrades();
                    }, this)
                }]
            }, {
                title: i18n._('Automatic Upgrade'),
                items: [{
                    xtype: 'radio',
                    boxLabel: i18n._('Automatically Install Upgrades'),
                    hideLabel: true,
                    name: 'Automatically Install Upgrades',
                    checked: this.getSystemSettings().autoUpgrade,
                    listeners: {
                        "change": {
                            fn: Ext.bind(function(elem, checked) {
                                this.getSystemSettings().autoUpgrade = checked;
                            }, this)
                        }
                    }
                }, {
                    xtype: 'component',
                    margin: '0 0 10 20',
                    html: i18n._("If new upgrades are available at the specified upgrade time they will be automatically downloaded and installed. During the install the system may be rebooted resulting in momentary loss of connectivicty.")
                }, {
                    xtype: 'radio',
                    boxLabel: i18n._('Do Not Automatically Install Upgrades'),
                    hideLabel: true,
                    name: 'Automatically Install Upgrades',
                    checked: !this.getSystemSettings().autoUpgrade,
                    listeners: {
                        "change": {
                            fn: Ext.bind(function(elem, checked) {
                                this.getSystemSettings().autoUpgrade = !checked;
                            }, this)
                        }
                    }
                }, {
                    xtype: 'component',
                    margin: '0 0 10 20',
                    html: i18n._("If new upgrades are available at the specified upgrade time they will be not be installed. All upgrades must be manually installed using the button on the Upgrade tab.")
                }, {
                    xtype: 'component',
                    margin: '0 0 10 20',
                    html: "<i>" + i18n._("Note: Turning off Automatic Upgrades does not disable signature & list updates") + "</i>"
                }]
            }, {
                title: i18n._('Automatic Upgrade Schedule'),
                items: [{
                    xtype: 'udayfield',
                    name: 'Upgrade Days',
                    i18n: i18n,
                    value: this.getSystemSettings().autoUpgradeDays,
                    listeners: {
                        "change": {
                            fn: Ext.bind(function(elem, newValue) {
                                this.getSystemSettings().autoUpgradeDays = elem.getValue();
                            }, this)
                        }
                    }
                }, {
                    xtype: 'timefield',
                    name: 'Upgrade Time',
                    width: 90,
                    hideLabel: true,
                    value: upgradeTime,
                    listeners: {
                        "change": {
                            fn: Ext.bind(function(elem, newValue) {
                                if (newValue && newValue instanceof Date) {
                                    this.getSystemSettings().autoUpgradeMinute = newValue.getMinutes();
                                    this.getSystemSettings().autoUpgradeHour = newValue.getHours();
                                }
                            }, this)
                        }
                    }
                }]
            }]
        });
    },
    save: function (isApply) {
        rpc.systemManager.setSettings(Ext.bind(function(result, exception) {
            Ext.MessageBox.hide();
            if(Ung.Util.handleException(exception)) return;
            if (!isApply) {
                this.closeWindow();
            } else {
                this.clearDirty();
            }
        }, this), this.getSystemSettings());
    }
 });
//# sourceURL=upgrade.js