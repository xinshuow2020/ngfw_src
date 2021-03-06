Ext.define('Ung.apps.webmonitor.view.FlagSites', {
    extend: 'Ung.cmp.Grid',
    alias:  'widget.app-web-monitor-flagsites',
    itemId: 'flagsites',
    title:  'Flag Sites'.t(),

    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: [{
            xtype: 'tbtext',
            padding: '8 5',
            style: { fontSize: '12px', fontWeight: 600 },
            html: 'Flag access to specific sites.'.t()
        }]
    }, {
        xtype: 'toolbar',
        dock: 'top',
        items: ['@add', '->', '@import', '@export']
    }],

    recordActions: ['edit', 'delete'],

    listProperty: 'settings.blockedUrls.list',
    emptyRow: {
        string: '',
        blocked: false,
        flagged: true,
        description: '',
        javaClass: 'com.untangle.uvm.app.GenericRule'
    },

    bind: '{blockedUrls}',

    columns: [{
        header: 'Site'.t(),
        width: 200,
        dataIndex: 'string',
        editor: {
            xtype: 'textfield',
            emptyText: '[enter site]'.t(),
            allowBlank: false,
            validator: Util.urlValidator
        }
    }, {
        xtype: 'checkcolumn',
        width: 55,
        header: 'Flag'.t(),
        dataIndex: 'flagged',
        resizable: false,
        tooltip: 'Flag as Violation'.t()
    }, {
        header: 'Description'.t(),
        width: 200,
        flex: 1,
        dataIndex: 'description',
        editor: {
            xtype: 'textfield',
            emptyText: '[no description]'.t()
        }
    }],
    editorFields: [{
        xtype: 'textfield',
        bind: '{record.string}',
        fieldLabel: 'Site'.t(),
        emptyText: '[enter site]'.t(),
        allowBlank: false,
        width: 400,
        validator: Util.urlValidator
    }, {
        xtype: 'checkbox',
        bind: '{record.flagged}',
        fieldLabel: 'Flag'.t(),
        tooltip: 'Flag as Violation'.t()
    }, {
        xtype: 'textarea',
        bind: '{record.description}',
        fieldLabel: 'Description'.t(),
        emptyText: '[no description]'.t(),
        width: 400,
        height: 60
    }]
});
