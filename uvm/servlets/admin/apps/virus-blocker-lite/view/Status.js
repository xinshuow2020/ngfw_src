Ext.define('Ung.apps.virusblockerlite.view.Status', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.app-virus-blocker-lite-status',
    itemId: 'status',
    title: 'Status'.t(),

    viewModel: true,

    layout: 'border',
    items: [{
        region: 'center',
        border: false,
        bodyPadding: 10,
        scrollable: 'y',
        items: [{
            xtype: 'component',
            cls: 'app-desc',
            html: '<img src="/skins/modern-rack/images/admin/apps/virus-blocker-lite_80x80.png" width="80" height="80"/>' +
                '<h3>Virus Blocker Lite</h3>' +
                '<p>' + 'Virus Blocker detects and blocks malware before it reaches users\' desktops or mailboxes.'.t() + '</p>'
        }, {
            xtype: 'appstate',
        }, {
            xtype: 'appreports'
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
