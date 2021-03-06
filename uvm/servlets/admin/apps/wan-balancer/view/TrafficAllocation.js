Ext.define('Ung.apps.wanbalancer.view.TrafficAllocation', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.app-wan-balancer-trafficallocation',
    itemId: 'trafficallocation',
    title: 'Traffic Allocation'.t(),
    viewModel: true,

    tbar: [{
        xtype: 'tbtext',
        padding: '8 5',
        style: { fontSize: '12px', fontWeight: 600 },
        html: 'Allocate traffic across WAN interfaces'.t()
    }],

    items: [{
        xtype: 'displayfield',
        padding: '10 20 0 20',
        value: 'Traffic allocation across WAN interfaces is controlled by assigning a relative weight (1-100) to each interface.'.t() + '<BR>' +
               'After entering the weight of each interface the resulting allocation is displayed.'.t() + '<BR>' +
               'If all WAN interfaces have the same bandwidth it is best to assign the same weight to all WAN interfaces.'.t() + '<BR>' +
               'If the WAN interfaces vary in bandwidth, enter numbers that correlate the relative available bandwidth.'.t() + '<BR>' +
               'For example: 15 for a 1.5Mbit/sec T1, 60 for a 6 mbit link, and 100 for a 10mbit link.'.t()
    },{
        xtype: 'app-wan-balancer-weight-grid',
        width: 800,
        height: 400,
        padding: '20 20 20 20',
        border: true,
    }]

});

Ext.define('Ung.apps.wanbalancer.view.WeightGrid', {
    extend: 'Ung.cmp.Grid',
    alias: 'widget.app-wan-balancer-weight-grid',
    itemId: 'weight-grid',
    title: 'Interface Weights'.t(),

//    listProperty: null,
//    bind: null,

    columns: [{
        header: 'Interface'.t(),
        width: 150,
    }, {
        header: 'Weight'.t(),
        width: 120,
    }, {
        header: 'Resulting Traffic Allocation'.t(),
        width: 150,
        flex: 1
    }]
});
