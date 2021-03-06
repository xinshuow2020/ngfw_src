Ext.define('Ung.apps.captiveportal.view.UserAuthentication', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.app-captive-portal-userauthentication',
    itemId: 'userauthentication',
    title: 'User Authentication'.t(),

    viewModel: {
        formulas: {
            _idleTimeout: {
                get: function (get) {
                    return get('settings.idleTimeout') / 60;
                },
                set: function (value) {
                    this.set('settings.idleTimeout', value * 60);
                }
            },
            _userTimeout: {
                get: function (get) {
                    return get('settings.userTimeout') / 60;
                },
                set: function (value) {
                    this.set('settings.userTimeout', value * 60);
                }
            },
            _cookieTimeout: {
                get: function (get) {
                    return get('settings.sessionCookiesTimeout') / 60;
                },
                set: function (value) {
                    this.set('settings.sessionCookiesTimeout', value * 60);
                }
            },
            _btnConfigureDirectory: function (get) {
                switch (get('settings.authenticationType')) {
                case 'LOCAL_DIRECTORY': return 'Configure Local Directory'.t();
                case 'GOOGLE': return 'Configure Google'.t();
                case 'FACEBOOK': return 'Configure Facebook'.t();
                case 'RADIUS': return 'Configure RADIUS'.t();
                case 'ACTIVE_DIRECTORY': return 'Configure Active Directory'.t();
                default: return '';
                }
            }
        }
    },

    bodyPadding: 10,

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    defaults: {
        xtype: 'fieldset',
        padding: 10,
    },

    items: [{
        title: 'Authentication Method'.t(),
        items: [{
            xtype: 'radiogroup',
            bind: '{settings.authenticationType}',
            simpleValue: 'true',
            columns: 1,
            vertical: true,
            items: [
                { boxLabel: '<strong>' + 'None'.t() + '</strong>', inputValue: 'NONE' },
                { boxLabel: '<strong>' + 'Local Directory'.t() + '</strong>', inputValue: 'LOCAL_DIRECTORY' },
                { boxLabel: '<strong>' + 'Any Method'.t() + '</strong> (' + 'requires'.t() + ' Directory Connector)', inputValue: 'ANY' },
                { boxLabel: '<strong>' + 'Google'.t() + '</strong> (' + 'requires'.t() + ' Directory Connector)', inputValue: 'GOOGLE' },
                { boxLabel: '<strong>' + 'Facebook'.t() + '</strong> (' + 'requires'.t() + ' Directory Connector)', inputValue: 'FACEBOOK' },
                { boxLabel: '<strong>' + 'RADIUS'.t() + '</strong> (' + 'requires'.t() + ' Directory Connector)', inputValue: 'RADIUS' },
                { boxLabel: '<strong>' + 'Active Directory'.t() + '</strong> (' + 'requires'.t() + ' Directory Connector)', inputValue: 'ACTIVE_DIRECTORY' },
            ]
        }, {
            // todo: update this button later
            xtype: 'button',
            iconCls: 'fa fa-cog',
            disabled: true,
            hidden: true,
            bind: {
                text: '{_btnConfigureDirectory}',
                disabled: '{settings.authenticationType === "NONE" || settings.authenticationType === "ANY"}',
                hidden: '{settings.authenticationType === "NONE" || settings.authenticationType === "ANY"}'
            },
            handler: 'configureLocalDirectory'
        }]
    }, {
        title: 'Session Settings'.t(),
        defaults: {
            margin: '5 0'
        },
        items: [{
            xtype: 'container',
            layout: { type: 'hbox', align: 'middle' },
            items: [{
                xtype: 'numberfield',
                fieldLabel: 'Idle Timeout (minutes)'.t(),
                invalidText: 'The Idle Timeout must be 0 or greater.'.t(),
                labelWidth: 200,
                width: 300,
                allowBlank: false,
                minValue: 0,
                bind: '{_idleTimeout}'
            }, {
                xtype: 'label',
                margin: '0 0 0 10',
                style: {
                    fontSize: '11px',
                    color: '#555'
                },
                html: 'Clients will be unauthenticated after this amount of idle time. They may re-authenticate immediately.  Zero disables idle timeout.'.t()
            }]
        }, {
            xtype: 'container',
            layout: { type: 'hbox', align: 'middle' },
            items: [{
                xtype: 'numberfield',
                fieldLabel: 'Timeout (minutes)'.t(),
                invalidText: 'The Timeout must be more than 5 minutes and less than 525600 minutes.'.t(),
                labelWidth: 200,
                width: 300,
                allowBlank: false,
                maxValue: 525600,
                minValue: 5,
                bind: '{_userTimeout}'
            }, {
                xtype: 'label',
                margin: '0 0 0 10',
                style: {
                    fontSize: '11px',
                    color: '#555'
                },
                html: 'Clients will be unauthenticated after this amount of time regardless of activity. They may re-authenticate immediately.'.t()
            }]
        }, {
            xtype: 'checkbox',
            boxLabel: 'Allow Concurrent Logins'.t(),
            tooltip: 'This will allow multiple hosts to use the same username & password concurrently.'.t(),
            bind: '{settings.concurrentLoginsEnabled}',
        }, {
            xtype: 'checkbox',
            boxLabel: 'Allow Cookie-based authentication'.t(),
            tooltip: 'This will allow authenicated clients to continue to access even after session idle and timeout values.'.t(),
            bind: '{settings.sessionCookiesEnabled}'
        }, {
            xtype: 'checkbox',
            boxLabel: 'Track logins using MAC address'.t(),
            tooltip: 'This will allow client authentication to be tracked by MAC address instead of IP address.'.t(),
            bind: '{settings.useMacAddress}'
        }, {
            xtype: 'container',
            layout: { type: 'hbox', align: 'middle' },
            items: [{
                xtype: 'numberfield',
                fieldLabel: 'Cookie Timeout (minutes)'.t(),
                invalidText: 'The Cookie Timeout must be more than 5 minutes and less than 525600 minutes.'.t(),
                labelWidth: 200,
                width: 300,
                allowBlank: false,
                maxValue: 525600,
                minValue: 5,
                bind: '{_cookieTimeout}'
            }, {
                xtype: 'label',
                margin: '0 0 0 10',
                style: {
                    fontSize: '11px',
                    color: '#555'
                },
                html: 'Clients will be unauthenticated after this amount of time regardless of activity. They may re-authenticate immediately.'.t()
            }]
        }]
    }]
});
