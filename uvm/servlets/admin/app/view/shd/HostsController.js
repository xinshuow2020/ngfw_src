Ext.define('Ung.view.shd.HostsController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.hosts',

    control: {
        '#': {
            deactivate: 'onDeactivate'
        },
        '#hostsgrid': {
            beforerender: 'onBeforeRenderHostsGrid'
        }
    },

    onDeactivate: function (view) {
        view.destroy();
    },

    onBeforeRenderHostsGrid: function (grid) {
        // this.getHosts();
    },

    getHosts: function () {
        console.log('get hosts');
        var me = this,
            grid = me.getView().down('#hostsgrid');
        grid.getView().setLoading(true);
        rpc.hostTable.getHosts(function (result, exception) {
            grid.getView().setLoading(false);
            if (exception) {
                Ung.Util.exceptionToast(exception);
                return;
            }
            Ext.getStore('hosts').loadData(result.list);
            // grid.getSelectionModel().select(0);
            // grid.getStore().setData(result.list);
        });
    }

});