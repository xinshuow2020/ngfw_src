Ext.define('Ung.config.localdirectory.Main', {
    extend: 'Ung.cmp.ConfigPanel',
    alias: 'widget.config.localdirectory',

    /* requires-start */
    requires: [
        'Ung.config.localdirectory.MainController',
        'Ung.config.localdirectory.MainModel'
    ],
    /* requires-end */
    controller: 'config.localdirectory',

    viewModel: {
        type: 'config.localdirectory'
    },

    layout: 'fit',

    items: [{
        xtype: 'ungrid',
        border: false,
        title: 'Local Users'.t(),

        tbar: ['@add'],
        recordActions: ['edit', 'delete'],

        listProperty: 'usersData.list',

        emptyRow: {
            username: '',
            firstName: '',
            lastName: '',
            email: '',
            password: '',
            passwordBase64Hash: '',
            expirationTime: 0,
            javaClass: 'com.untangle.uvm.LocalDirectoryUser'
        },

        bind: '{users}',

        columns: [{
            header: 'user/login ID'.t(),
            width: 140,
            dataIndex: 'username',
            editor: {
                xtype: 'textfield',
                allowBlank: false,
                emptyText: '[enter login]'.t(),
                regex: /^[\w\. ]+$/,
                regexText: 'The field user/login ID can have only alphanumeric characters.'.t()
            }
        }, {
            header: 'first name'.t(),
            width: 120,
            dataIndex: 'firstName',
            editor: {
                xtype:'textfield',
                emptyText: '[enter first name]'.t(),
                allowBlank: false
            }
        }, {
            header: 'last name'.t(),
            width: 120,
            dataIndex: 'lastName',
            editor: {
                xtype: 'textfield',
                emptyText: '[last name]'.t()
            }
        }, {
            header: 'email address'.t(),
            width: 250,
            dataIndex: 'email',
            flex:1,
            editor: {
                xtype: 'textfield',
                emptyText: '[email address]'.t(),
                vtype: 'email'
            }
        }, {
            header: 'datetime',
            dataIndex: 'expirationTime',
            width: 150,
            resizable: false,
            renderer: function (time) {
                return time > 0 ? new Date(time) : 'Never'.t();
            }
        }],
        editorFields: [{
            xtype: 'textfield',
            fieldLabel: 'User/Login ID'.t(),
            bind: '{record.username}',
            emptyText: '[enter login]'.t(),
            allowBlank: false,
            regex: /^[\w\. ]+$/,
            regexText: 'The field user/login ID can have only alphanumeric character.'.t(),
        }, {
            xtype: 'textfield',
            fieldLabel: 'First Name'.t(),
            bind: '{record.firstName}',
            emptyText: '[enter first name]'.t(),
            allowBlank: false,
            width: 300
        }, {
            xtype: 'textfield',
            fieldLabel: 'Last Name'.t(),
            bind: '{record.lastName}',
            emptyText: '[last name]'.t(),
            width: 300
        }, {
            xtype: 'textfield',
            fieldLabel: 'Email Address'.t(),
            bind: '{record.email}',
            emptyText: '[email address]'.t(),
            vtype: 'email',
            width: 300
        }, {
            xtype: 'container',
            layout: {
                type: 'hbox'
            },
            items: [{
                xtype: 'displayfield',
                fieldLabel: 'Expiration Time'.t(),
                labelAlign: 'right',
                labelWidth: 180,
                width: 185
            }, {
                xtype: 'checkbox',
                boxLabel: 'Never'.t(),
                bind: '{checked}'
            }, {
                xtype: 'datefield',
                format: 'time',
                minValue: '',
                margin: '0 10',
                editable: false,
                bind: '{record.expirationTime}'
            }]
        }]
        // extraVM: {
        //     formulas: {
        //         checked: function (get) {
        //             return 'test';
        //         }
        //     }
        // }

    }]

});
