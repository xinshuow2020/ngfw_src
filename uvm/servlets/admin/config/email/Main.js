Ext.define('Ung.config.email.Main', {
    extend: 'Ung.cmp.ConfigPanel',
    alias: 'widget.config.email',

    /* requires-start */
    requires: [
        'Ung.config.email.MainController',
        'Ung.config.email.EmailTest',

        'Ung.store.Rule',
        'Ung.model.Rule',
        'Ung.cmp.Grid'
    ],
    /* requires-end */

    controller: 'config.email',

    viewModel: {
        data: {
            title: 'Email'.t(),
            iconName: 'icon_config_email',

            globalSafeList: null,
        }
    },

    items: [
        { xtype: 'config.email.outgoingserver' },
        { xtype: 'config.email.safelist' },
        { xtype: 'config.email.quarantine' }
    ]
});
