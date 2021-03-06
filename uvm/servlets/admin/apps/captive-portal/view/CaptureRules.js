Ext.define('Ung.apps.captiveportal.view.CaptureRules', {
    extend: 'Ung.cmp.Grid',
    alias: 'widget.app-captive-portal-capturerules',
    itemId: 'capturerules',
    title: 'Capture Rules'.t(),

    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: [{
            xtype: 'tbtext',
            padding: '8 5',
            style: { fontSize: '12px', fontWeight: 600 },
            html: 'Network access is controlled based on the set of rules defined below. To learn more click on the <b>Help</b> button below.'.t()
        }]
    }, {
        xtype: 'toolbar',
        dock: 'top',
        items: ['@add', '->', '@import', '@export']
    }],

    recordActions: ['edit', 'delete', 'reorder'],

    listProperty: 'settings.captureRules.list',
    ruleJavaClass: 'com.untangle.app.captive_portal.CaptureRuleCondition',

    emptyRow: {
        ruleId: -1,
        enabled: true,
        description: '',
        capture: false,
        conditions: {
            javaClass: 'java.util.LinkedList',
            list: []
        },
        javaClass: 'com.untangle.app.captive_portal.CaptureRule'
    },

    bind: '{captureRules}',

    columns: [
        Column.ruleId,
        Column.enabled,
        Column.description,
        Column.conditions, {
            xtype: 'checkcolumn',
            header: 'Capture',
            dataIndex: 'capture',
            resizable: false,
            width: 70
        }

    ],

    // todo: continue this stuff
    editorFields: [
        Field.enableRule(),
        Field.description,
        Field.conditions, {
            xtype: 'combo',
            allowBlank: false,
            bind: '{record.capture}',
            fieldLabel: 'Action Type'.t(),
            editable: false,
            store: [[true, 'Capture'.t()], [false, 'Pass'.t()]],
            queryMode: 'local'
        }
    ],

    conditions: [
        { name: 'DST_ADDR', displayName: 'Destination Address'.t(), type: 'textfield', visible: true, vtype:'ipMatcher'},
        { name: 'DST_PORT', displayName: 'Destination Port'.t(), type: 'textfield',vtype:'portMatcher', visible: true},
        { name: 'DST_INTF', displayName: 'Destination Interface'.t(), type: 'checkgroup', values: Util.getInterfaceList(true, false), visible: true},
        { name: 'SRC_ADDR', displayName: 'Source Address'.t(), type: 'textfield', visible: true, vtype:'ipMatcher'},
        { name: 'SRC_PORT', displayName: 'Source Port'.t(), type: 'textfield',vtype:'portMatcher', visible: rpc.isExpertMode},
        { name: 'SRC_INTF', displayName: 'Source Interface'.t(), type: 'checkgroup', values: Util.getInterfaceList(true, false), visible: true},
        { name: 'PROTOCOL', displayName: 'Protocol'.t(), type: 'checkgroup', values: [['TCP','TCP'],['UDP','UDP'],['any', 'any'.t()]], visible: true},
        // { name: 'USERNAME', displayName: 'Username'.t(), type: 'editor', editor: Ext.create('Ung.UserEditorWindow',{}), visible: true},
        { name: 'CLIENT_HOSTNAME', displayName: 'Client Hostname'.t(), type: 'textfield', visible: true},
        { name: 'SERVER_HOSTNAME', displayName: 'Server Hostname'.t(), type: 'textfield', visible: rpc.isExpertMode},
        { name: 'SRC_MAC', displayName: 'Client MAC Address'.t(), type: 'textfield', visible: true },
        { name: 'DST_MAC', displayName: 'Server MAC Address'.t(), type: 'textfield', visible: true },
        { name: 'CLIENT_MAC_VENDOR', displayName: 'Client MAC Vendor'.t(), type: 'textfield', visible: true},
        { name: 'SERVER_MAC_VENDOR', displayName: 'Server MAC Vendor'.t(), type: 'textfield', visible: true},
        { name: 'CLIENT_IN_PENALTY_BOX', displayName: 'Client in Penalty Box'.t(), type: 'boolean', visible: true},
        { name: 'SERVER_IN_PENALTY_BOX', displayName: 'Server in Penalty Box'.t(), type: 'boolean', visible: true},
        { name: 'CLIENT_HAS_NO_QUOTA', displayName: 'Client has no Quota'.t(), type: 'boolean', visible: true},
        { name: 'SERVER_HAS_NO_QUOTA', displayName: 'Server has no Quota'.t(), type: 'boolean', visible: true},
        { name: 'CLIENT_QUOTA_EXCEEDED', displayName: 'Client has exceeded Quota'.t(), type: 'boolean', visible: true},
        { name: 'SERVER_QUOTA_EXCEEDED', displayName: 'Server has exceeded Quota'.t(), type: 'boolean', visible: true},
        { name: 'CLIENT_QUOTA_ATTAINMENT', displayName: 'Client Quota Attainment'.t(), type: 'textfield', visible: true},
        { name: 'SERVER_QUOTA_ATTAINMENT', displayName: 'Server Quota Attainment'.t(), type: 'textfield', visible: true},
        // { name: 'DIRECTORY_CONNECTOR_GROUP', displayName: 'Directory Connector: User in Group'.t(), type: 'editor', editor: Ext.create('Ung.GroupEditorWindow',{}), visible: true},
        { name: 'HTTP_HOST', displayName: 'HTTP: Hostname'.t(), type: 'textfield', visible: true},
        { name: 'HTTP_REFERER', displayName: 'HTTP: Referer'.t(), type: 'textfield', visible: true},
        { name: 'HTTP_USER_AGENT', displayName: 'HTTP: Client User Agent'.t(), type: 'textfield', visible: true},
        { name: 'HTTP_USER_AGENT_OS', displayName: 'HTTP: Client User OS'.t(), type: 'textfield', visible: false},
        // { name: 'CLIENT_COUNTRY', displayName: 'Client Country'.t(), type: 'editor', editor: Ext.create('Ung.CountryEditorWindow',{}), visible: true},
        // { name: 'SERVER_COUNTRY', displayName: 'Server Country'.t(), type: 'editor', editor: Ext.create('Ung.CountryEditorWindow',{}), visible: true}
    ]

});
