/**
 * Dashboard view which holds the widgets and manager
 */
Ext.define('Ung.view.dashboard.Dashboard', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.ung-dashboard',
    itemId: 'dashboard',

    /* requires-start */
    requires: [
        'Ung.view.dashboard.DashboardController',
        'Ung.view.dashboard.Queue',
        'Ung.widget.Report',

        'Ung.widget.WidgetController',
        'Ung.widget.Information',
        'Ung.widget.Resources',
        'Ung.widget.CpuLoad',
        'Ung.widget.NetworkInformation',
        'Ung.widget.NetworkLayout',
        'Ung.widget.MapDistribution'
    ],
    /* requires-end */
    controller: 'dashboard',
    viewModel: {
        data: {
            managerVisible: false
        }
    },

    dockedItems: [{
        xtype: 'toolbar',
        ui: 'navigation',
        dock: 'top',
        border: false,
        style: {
            background: '#333435',
            zIndex: 9997
        },
        defaults: {
            xtype: 'button',
            border: false,
            hrefTarget: '_self'
        },
        items: Ext.Array.insert(Ext.clone(Util.subNav), 0, [{
            text: 'Manage Widgets'.t(),
            iconCls: 'fa fa-cog',
            hidden: true,
            bind: {
                userCls: '{managerVisible ? "pressed" : ""}',
                // iconCls: 'fa {!managerVisible ? "fa-arrow-left" : "fa-arrow-down" }',
                hidden: '{managerVisible}'
            },
            handler: 'toggleManager'
        }])
    }],

    config: {
        settings: null // the dashboard settings object
    },

    layout: 'border',

    defaults: {
        border: false
    },

    items: [{
        region: 'west',
        // title: 'Manage Widgets'.t(),

        dockedItems: [{
            xtype: 'toolbar',
            border: false,
            dock: 'top',
            cls: 'report-header',
            // height: 53,
            padding: '10 5',
            items: [{
                xtype: 'component',
                html: '<h2>' + 'Manage Widgets'.t() + '</h2>'
            }, '->', {
                xtype: 'tool',
                type: 'close',
                callback: 'toggleManager'
            }]
        }, {
            xtype: 'toolbar',
            dock: 'top',
            ui: 'footer',
            items: [{
                itemId: 'addWidgetBtn',
                text: 'Add'.t(),
                iconCls: 'fa fa-plus-circle',
                handler: 'addWidget'
                // menu: Ext.create('Ext.menu.Menu', {
                //     mouseLeaveDelay: 0
                // })
            }, '->', {
                // text: 'Import'.t(),
                iconCls: 'fa fa-download',
                tooltip: 'Import'.t()
                // handler: 'applyChanges'
            }, {
                // text: 'Export'.t(),
                iconCls: 'fa fa-upload',
                tooltip: 'Export'.t()
                //handler: 'applyChanges'
            }]
        }],

        // tools: [{
        //     type: 'close',
        //     callback: 'toggleManager'
        // }],

        itemId: 'dashboardManager',
        reference: 'dashboardManager',
        // weight: 30,
        width: 250,
        minWidth: 250,
        maxWidth: 350,
        // collapsible: true,
        bodyBorder: false,
        // shadow: false,
        animCollapse: false,
        // collapsed: true,
        // collapseMode: 'mini',
        // titleCollapse: true,
        floatable: false,
        cls: 'widget-manager',
        split: true,
        xtype: 'grid',
        // forceFit: true,
        hideHeaders: true,
        // disableSelection: true,
        // trackMouseOver: false,
        hidden: true,
        bind: {
            hidden: '{!managerVisible}'
        },
        store: 'widgets',

        // bodyStyle: {
        //     border: 0
        // },
        viewConfig: {
            plugins: {
                ptype: 'gridviewdragdrop',
                dragText: 'Drag and drop to reorganize'.t(),
                // dragZone: {
                //     onBeforeDrag: function (data, e) {
                //         return Ext.get(e.target).hasCls('drag-handle');
                //     }
                // }
            },
            stripeRows: false,
            getRowClass: function (record) {
                return !record.get('enabled') ? 'disabled' : '';
            },
            listeners: {
                drop: 'onDrop'
            }
        },
        columns: [
        //     {
        //     xtype: 'actioncolumn',
        //     iconCls: 'fa fa-arrows',
        //     width: 14,
        //     align: 'center',
        //     sortable: false,
        //     hideable: false,
        //     resizable: false,
        //     menuDisabled: true,
        //     // tdCls: 'drag-handle'
        // },
        {
            width: 25,
            align: 'center',
            sortable: false,
            hideable: false,
            resizable: false,
            menuDisabled: true,
            //handler: 'toggleWidgetEnabled',
            dataIndex: 'enabled',
            renderer: 'enableRenderer'
        }, {
            dataIndex: 'entryId',
            renderer: 'widgetTitleRenderer',
            flex: 1
        }, {
            xtype: 'actioncolumn',
            iconCls: 'fa fa-minus-circle',
            width: 30,
            align: 'center',
            sortable: false,
            hideable: false,
            resizable: false,
            menuDisabled: true,
            handler: 'removeWidget',
            isDisabled: function (view, rowIndex, colIndex, item, record) {
                return record.get('type') !== 'ReportEntry';
            }
            // renderer: function (value, meta, record) {
            //     return '<i class="fa fa-times" style="color: #999;"></i>';
            // }
        }/*, {
            xtype: 'actioncolumn',
            align: 'center',
            width: 25,
            sortable: false,
            hideable: false,
            resizable: false,
            menuDisabled: true,
            renderer: function (value, meta, record) {
                if (record.get('type') !== 'ReportEntry') {
                    return '';
                }
                return '<i style="font-size: 16px; color: #777; padding-top: 4px;" class="material-icons">settings</i>';
            }
        }*/],
        listeners: {
            itemmouseleave : 'onItemLeave',
            cellclick: 'onItemClick'
        },
        fbar: [{
            text: 'Reset'.t(),
            iconCls: 'fa fa-rotate-left',
            handler: 'resetDashboard'
        }, '->', {
            text: 'Apply'.t(),
            iconCls: 'fa fa-floppy-o',
            handler: 'applyChanges'
        }]
    }, {
        // xtype: 'container',
        region: 'center',
        reference: 'dashboard',
        itemId: 'widgetsCmp',
        bodyCls: 'dashboard',
        bodyPadding: 8,
        border: false,
        bodyBorder: false,
        scrollable: true,
    }],
    listeners: {
        //afterrender: 'onAfterRender',
        showwidgeteditor: 'showWidgetEditor'
    }
});
