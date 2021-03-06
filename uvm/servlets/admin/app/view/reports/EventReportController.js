Ext.define('Ung.view.reports.EventReportController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.eventreport',

    control: {
        '#': {
            afterrender: 'onAfterRender',
            deactivate: 'onDeactivate'
        }
    },

    onAfterRender: function () {
        var me = this, vm = this.getViewModel(), i;

        me.modFields = { uniqueId: null };

        // remove property grid if in dashboard
        if (me.getView().up('#dashboard')) {
            me.getView().remove('properties');
        }


        vm.bind('{entry}', function (entry) {

            if (entry.get('type') !== 'EVENT_LIST') { return; }

            if (me.modFields.uniqueId !== entry.get('uniqueId')) {
                me.modFields = {
                    uniqueId: entry.get('uniqueId'),
                    defaultColumns: entry.get('defaultColumns')
                };

                me.tableConfig = Ext.clone(TableConfig.getConfig(entry.get('table')));
                me.defaultColumns = vm.get('widget.displayColumns') || entry.get('defaultColumns'); // widget or report columns

                Ext.Array.each(me.tableConfig.columns, function (col) {
                    col.hidden = Ext.Array.indexOf(me.defaultColumns, col.dataIndex) < 0;
                });

                me.getView().down('grid').setColumns(me.tableConfig.columns);

                if (!me.getView().up('reportwidget')) {
                    me.fetchData();
                } else {
                    me.isWidget = true;
                }
                return;
            }

            if (!Ext.Array.equals(me.modFields.defaultColumns, entry.get('defaultColumns'))) {
                me.modFields.defaultColumns = entry.get('defaultColumns');
                Ext.Array.each(me.getView().down('grid').getColumns(), function (col) {
                    col.setHidden(Ext.Array.indexOf(entry.get('defaultColumns'), col.dataIndex) < 0);
                });
            }

        }, me, { deep: true });

        // clear grid selection (hide event side data) when settings are open
        vm.bind('{settingsBtn.pressed}', function (pressed) {
            if (pressed) {
                me.getView().down('grid').getSelectionModel().deselectAll();
            }
        });
    },

    onDeactivate: function () {
        this.modFields = { uniqueId: null };
        this.getViewModel().set('eventsData', []);
        this.getView().down('grid').getSelectionModel().deselectAll();
    },

    fetchData: function (reset, cb) {
        var me = this, vm = this.getViewModel();
        me.entry = vm.get('entry');

        me.getViewModel().set('eventsData', []);
        me.getView().setLoading(true);
        Rpc.asyncData('rpc.reportsManager.getEventsForDateRangeResultSet',
                        vm.get('entry').getData(), // entry
                        vm.get('sqlFilterData'), // etra conditions
                        1000, // limit
                        vm.get('startDate'), // start date
                        vm.get('tillNow') ? null : vm.get('endDate')) // end date
            .then(function(result) {
                if (me.getView().up('reports-entry')) {
                    me.getView().up('reports-entry').down('#currentData').setLoading(false);
                }
                me.getView().setLoading(false);

                // update columns
                me.defaultColumns = vm.get('widget.displayColumns') || me.entry.get('defaultColumns'); // widget or report columns
                Ext.Array.each(me.getView().down('grid').getColumns(), function (col) {
                    col.setHidden(Ext.Array.indexOf(me.defaultColumns, col.dataIndex) < 0);
                });

                me.loadResultSet(result);
                if (cb) { cb(); }
            });
    },

    loadResultSet: function (reader) {
        this.getView().setLoading(true);
        reader.getNextChunk(Ext.bind(this.nextChunkCallback, this), 1000);
    },

    nextChunkCallback: function (result, ex) {
        var vm = this.getViewModel();
        vm.set('eventsData', result.list);
        this.getView().setLoading(false);
    },

    onEventSelect: function (el, record) {
        var me = this, vm = this.getViewModel(), propsData = [];

        if (me.isWidget) { return; }

        Ext.Array.each(me.tableConfig.columns, function (column) {
            propsData.push({
                name: column.header,
                value: record.get(column.dataIndex)
            });
        });

        vm.set('propsData', propsData);
        // when selecting an event hide Settings if open
        me.getView().up('reports-entry').lookupReference('settingsBtn').setPressed(false);

    }

});
