Ext.define('Ung.config.administration.MainModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.config.administration',

    data: {
        title: 'Administration'.t(),
        iconName: 'icon_config_admin',

        adminSettings: null,
        systemSettings: null,
        skinSettings: null,

        serverCertificates: null,
        rootCertificateInformation: null,
        serverCertificateVerification: null,
        skinsList: null
    },
    stores: {
        accounts: { data: '{adminSettings.users.list}' },
        certificates: { data: '{serverCertificates.list}' },
        skins: { data: '{skinsList.list}' }
    }
});
