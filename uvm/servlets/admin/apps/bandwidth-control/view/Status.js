Ext.define('Ung.apps.bandwidthcontrol.view.Status', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.app-bandwidth-control-status',
    itemId: 'status',
    title: 'Status'.t(),

    viewModel: true,

    layout: 'border',
    items: [{
        region: 'center',
        border: false,
        bodyPadding: 10,
        scrollable: 'y',

        defaults: {
            xtype: 'fieldset',
        },

        items: [{
            xtype: 'component',
            cls: 'app-desc',
            html: '<img src="/skins/modern-rack/images/admin/apps/bandwidth-control_80x80.png" width="80" height="80"/>' +
                '<h3>Bandwidth Control</h3>' +
                '<p>' + 'Bandwidth Control monitors, manages, and shapes bandwidth usage on the network'.t() + '</p>'
        }, {
            xtype: 'appstate',
            hidden: true,
            bind: {
                hidden: '{!isConfigured}'
            },
        }, {
            title: '<i class="fa fa-cog"></i> ' + 'Configuration'.t(),
            padding: 10,
            margin: '20 0',
            cls: 'app-section',
            items: [{
                xtype: 'component',
                html: 'Bandwidth Control is unconfigured. Use the Wizard to configure Bandwidth Control.'.t(),
                hidden: true,
                bind: {
                    hidden: '{isConfigured}'
                }
            }, {
                xtype: 'component',
                html: 'Bandwidth Control is configured'.t(),
                hidden: true,
                bind: {
                    hidden: '{!isConfigured}'
                }
            }, {
                xtype: 'component',
                html: 'Bandwidth Control is enabled, but QoS is not enabled. Bandwidth Control requires QoS to be enabled.'.t(),
                hidden: true,
                bind: {
                    hidden: '{qosEnabled}'
                }
            }, {
                xtype: 'button',
                margin: '10 0 0 0',
                text: 'Run Bandwidth Control Setup Wizard'.t(),
                iconCls: 'fa fa-magic',
                handler: 'runWizard'
            }]
        }, {
            xtype: 'appreports',
            hidden: true,
            bind: {
                hidden: '{!isConfigured}'
            },
        }]
    }, {
        region: 'west',
        border: false,
        width: 350,
        minWidth: 300,
        split: true,
        layout: 'border',
        items: [{
            xtype: 'appsessions',
            region: 'north',
            height: 200,
            split: true,
        }, {
            xtype: 'appmetrics',
            region: 'center'
        }],
        bbar: [{
            xtype: 'appremove',
            width: '100%'
        }]
    }]
});
