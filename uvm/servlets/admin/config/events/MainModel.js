Ext.define('Ung.config.events.MainModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.config.events',

    data: {
        title: 'Events'.t(),
        iconName: 'icon_config_events',

        settings: null

    },
    stores: {
        alertRules: { data: '{settings.alertRules.list}' },
        triggerRules: { data: '{settings.triggerRules.list}' },
        syslogRules: { data: '{settings.syslogRules.list}' }
    }
});
