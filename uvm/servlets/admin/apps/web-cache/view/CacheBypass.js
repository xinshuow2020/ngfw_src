Ext.define('Ung.apps.webcache.view.CacheBypass', {
    extend: 'Ung.cmp.Grid',
    alias: 'widget.app-web-cache-cachebypass',
    itemId: 'cachebypass',
    title: 'Cache Bypass'.t(),


    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: [{
            xtype: 'tbtext',
            padding: '8 5',
            style: { fontSize: '12px', fontWeight: 600 },
            html: 'The Web Cache Bypass List contains host or domain names that should never be cached.'.t()
        }]
    }, {
        xtype: 'toolbar',
        dock: 'top',
        items: ['@add', '->', '@import', '@export']
    }],

    recordActions: ['edit', 'delete'],

    listProperty: 'settings.rules.list',
    emptyRow: {
        live: true,
        hostname: '',
        javaClass: 'com.untangle.app.web_cache.WebCacheRule'
    },

    bind: '{rules}',

    columns: [{
        xtype: 'checkcolumn',
        width: 70,
        header: 'Enable'.t(),
        dataIndex: 'live',
        resizable: false
    }, {
        header: 'Hostname'.t(),
        width: 200,
        flex: 1,
        dataIndex: 'hostname',
        editor: {
            xtype: 'textfield',
            emptyText: '[enter hostname]'.t(),
            allowBlank: false
        }
    }],
    editorFields: [{
        xtype: 'checkbox',
        bind: '{record.live}',
        fieldLabel: 'Enable'.t()
    }, {
        xtype: 'textfield',
        bind: '{record.hostname}',
        fieldLabel: 'Hostname'.t(),
        allowBlank: false,
        emptyText: '[enter hostname]'.t(),
        width: 400
    }]

});
