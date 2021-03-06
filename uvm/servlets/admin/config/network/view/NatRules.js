Ext.define('Ung.config.network.view.NatRules', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.config.network.natrules',

    viewModel: true,

    title: 'NAT Rules'.t(),

    layout: 'fit',

    tbar: [{
        xtype: 'tbtext',
        padding: '8 5',
        style: { fontSize: '12px' },
        html: 'NAT Rules control the rewriting of the IP source address of traffic (Network Address Translation). The rules are evaluated in order.'.t()
    }],

    items: [{
        xtype: 'ungrid',
        flex: 3,

        tbar: ['@add'],
        recordActions: ['edit', 'delete', 'reorder'],

        listProperty: 'settings.natRules.list',
        ruleJavaClass: 'com.untangle.uvm.network.NatRuleCondition',

        conditions: [
            Condition.dstAddr,
            Condition.dstPort,
            Condition.dstIntf,
            Condition.srcAddr,
            Condition.srcPort,
            Condition.srcIntf,
            Condition.protocol([['TCP','TCP'],['UDP','UDP'],['ICMP','ICMP'],['GRE','GRE'],['ESP','ESP'],['AH','AH'],['SCTP','SCTP']])
        ],

        emptyRow: {
            ruleId: -1,
            enabled: true,
            auto: true,
            javaClass: 'com.untangle.uvm.network.NatRule',
            conditions: {
                javaClass: 'java.util.LinkedList',
                list: []
            },
            description: ''
        },

        bind: '{natRules}',

        columns: [{
            header: 'Rule Id'.t(),
            width: 70,
            align: 'right',
            resizable: false,
            dataIndex: 'ruleId',
            renderer: function(value) {
                return value < 0 ? 'new'.t() : value;
            }
        }, {
            xtype: 'checkcolumn',
            header: 'Enable'.t(),
            dataIndex: 'enabled',
            resizable: false,
            width: 70
        }, {
            header: 'Description',
            width: 200,
            dataIndex: 'description',
            renderer: function (value) {
                return value || '<em>no description<em>';
            }
        }, {
            header: 'Conditions'.t(),
            flex: 1,
            dataIndex: 'conditions',
            renderer: 'conditionsRenderer'
        }, {
            header: 'NAT Type'.t(),
            dataIndex: 'auto',
            width: 100,
            renderer: function (val) {
                return val ? 'Auto'.t() : 'Custom'.t();
            }
        }, {
            header: 'New Source'.t(),
            dataIndex: 'newSource',
            width: 120,
            renderer: function (value, metaData, record) {
                return record.get('auto') ? '' : value;
            }
        }],
        editorFields: [
            Field.enableRule('Enable NAT Rule'.t()),
            Field.description,
            Field.conditions,
            Field.natType,
            Field.natSource
        ]
    }]
});
