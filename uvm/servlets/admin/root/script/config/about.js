Ext.define('Ung.config.about.About', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.config.about',
    requires: [
        'Ung.config.about.AboutController'
    ],
    controller: 'config.about',
    viewModel: {
        kernelVersion: '',
        modificationState: '',
        rebootCount: '',
        activeSize: '',
        maxActiveSize: ''
    },
    dockedItems: [{
        xtype: 'toolbar',
        weight: -10,
        border: false,
        items: [{
            text: 'Back',
            iconCls: 'fa fa-arrow-circle-left fa-lg',
            hrefTarget: '_self',
            href: '#config'
        }, '-', {
            xtype: 'tbtext',
            html: '<strong>' + 'About'.t() + '</strong>'
        }],
    }],
    items: [{
        xtype: 'config.about.server'
    }, {
        xtype: 'config.about.licenses'
    }, {
        xtype: 'config.about.licenseagreement'
    }]
});
Ext.define('Ung.config.about.AboutController', {
Ext.define('Ung.config.about.view.LicenseAgreement', {
Ext.define('Ung.config.about.view.Licenses', {
Ext.define('Ung.config.about.view.Server', {