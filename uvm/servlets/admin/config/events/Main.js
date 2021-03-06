Ext.define('Ung.config.events.Main', {
    extend: 'Ung.cmp.ConfigPanel',
    alias: 'widget.config.events',

    /* requires-start */
    requires: [
        'Ung.config.events.MainController',
        'Ung.config.events.MainModel'
    ],
    /* requires-end */

    controller: 'config.events',

    viewModel: {
        type: 'config.events'
    },

    items: [{
        xtype: 'config.events.alerts'
    }, {
        xtype: 'config.events.triggers'
    }, {
        xtype: 'config.events.syslog'
    }]

});
