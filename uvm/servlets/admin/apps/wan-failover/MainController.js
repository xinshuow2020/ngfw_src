Ext.define('Ung.apps.wanfailover.MainController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.app-wan-failover',

    control: {
        '#': {
            beforerender: 'getSettings'
        },
        '#wanStatus': {
            afterrender: 'getWanStatus'
        }
    },

    getSettings: function () {
        var vm = this.getViewModel();
        this.getView().appManager.getSettings(function (result, ex) {
            if (ex) { Util.exceptionToast(ex); return; }
            console.log(result);
            vm.set('settings', result);
            // me.getWanStatus();
        });
    },

    setSettings: function () {
        var me = this, v = this.getView(), vm = this.getViewModel();

        v.query('ungrid').forEach(function (grid) {
            var store = grid.getStore();
            if (store.getModifiedRecords().length > 0 ||
                store.getNewRecords().length > 0 ||
                store.getRemovedRecords().length > 0 ||
                store.isReordered) {
                store.each(function (record) {
                    if (record.get('markedForDelete')) {
                        record.drop();
                    }
                });
                store.isReordered = undefined;
                vm.set(grid.listProperty, Ext.Array.pluck(store.getRange(), 'data'));
            }
        });

        v.setLoading(true);
        v.appManager.setSettings(function (result, ex) {
            v.setLoading(false);
            if (ex) { Util.exceptionToast(ex); return; }
            Util.successToast('Settings saved');
            me.getSettings();
        }, vm.get('settings'));
    },

    getWanStatus: function (cmp) {
        var vm = this.getViewModel(),
            grid = (cmp.getXType() === 'gridpanel') ? cmp : cmp.up('grid');
        grid.setLoading(true);
        this.getView().appManager.getWanStatus(function (result, ex) {
            grid.setLoading(false);
            if (ex) { Util.exceptionToast(ex); return; }
            vm.set('wans', result.list);

            var wanWarnings = [],
                tests = vm.get('settings.tests.list');

            Ext.Array.each(result.list, function (wan) {
                if (tests.length === 0 || Ext.Array.findBy(tests, function (test) {
                    return test.enabled && (wan.interfaceId === test.interfaceId);
                })) {
                    wanWarnings.push('<li>'  + Ext.String.format('Warning: The <i>{0}</i> needs a test configured!'.t(), wan.interfaceName) + '</li>');
                }
            });
            vm.set('wanWarnings', wanWarnings.join('<br/>'));
        });
    }
});
