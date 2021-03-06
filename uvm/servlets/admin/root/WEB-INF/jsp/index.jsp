<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
        <title>Untangle - ${buildStamp}</title>

        <script src="/jsonrpc/jsonrpc.js"></script>
        <script src="/highcharts-5.0.9/highstock.js"></script>
        <script src="/highcharts-5.0.9/highcharts-3d.js"></script>
        <script src="/highcharts-5.0.9/no-data-to-display.js"></script>

        <!-- ExtJS lib & theme -->
        <script src="/ext6.2/ext-all-debug.js"></script>
        <script src="/ext6.2/classic/theme-${extjsTheme}/theme-${extjsTheme}.js"></script>
        <link href="/ext6.2/classic/theme-${extjsTheme}/resources/theme-${extjsTheme}-all.css" rel="stylesheet" />

        <%-- Triton theme already contains fontawesome --%>
        <c:if test="${extjsTheme!='triton'}">
        <link href="/ext6.2/fonts/font-awesome/css/font-awesome.min.css" rel="stylesheet" />
        </c:if>

        <%-- Import custom fonts (see sass/_vars.scss)--%>
        <link href="/ext6.2/fonts/source-sans-pro/css/fonts.css" rel="stylesheet" />
        <link href="/ext6.2/fonts/roboto-condensed/css/fonts.css" rel="stylesheet" />

        <link href="styles/ung-all.css" rel="stylesheet" />

        <%-- app loader style --%>
        <style type="text/css">
            #app-loader {
                position: fixed;
                text-align: center;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: #282828;
                opacity: 1;
                z-index: 9999;
                transition: opacity 1s ease-out;
            }
            #app-loader.removing {
                opacity: 0;
            }
            #app-loader i {
                color: #737373;
            }
        </style>

        <script>
            Highcharts.setOptions({
                global: {
                    useUTC: false
                }
            });

            // JSON Rpc client
            var rpc = new JSONRpcClient('/admin/JSON-RPC');

            Ext.QuickTips.init();

            // Disable Ext Area to avoid unwanted debug messages
            Ext.enableAria = false;
            Ext.enableAriaButtons = false;
            Ext.enableAriaPanels = false;
            Ext.supports.MouseWheel = false;

            // IMPORTANT! override the default models ext idProperty so it does not interfere with backend 'id'
            Ext.data.Model.prototype.idProperty = '_id';

            Ext.onReady(function () {
                try {
                    var startUpInfo = rpc.UvmContext.getWebuiStartupInfo();
                } catch (ex) {
                    alert(ex);
                    // Ext.get('app-loader').destroy();
                }
                Ext.apply(rpc, startUpInfo);

                if (!rpc.translations.decimal_sep) { rpc.translations.decimal_sep = '.'; }
                if (!rpc.translations.thousand_sep) { rpc.translations.thousand_sep = ','; }
                if (!rpc.translations.date_fmt) { rpc.translations.date_fmt = 'Y-m-d'; }
                if (!rpc.translations.timestamp_fmt) { rpc.translations.timestamp_fmt = 'Y-m-d h:i:s a'; }


                String.prototype.t = function() {
                    return rpc.translations[this.valueOf()] || '<cite>' + this.valueOf() + '</cite>';
                };

                // Ext.application({
                //     name: 'Ung',
                //     extend: 'Ung.Application',
                // });

                // load the untangle app only after the rpc is in place and translations set
                Ext.Loader.loadScript({
                    url: 'script/ung-all.js',
                    onLoad: function () {
                        Ext.application({
                            name: 'Ung',
                            extend: 'Ung.Application'
                        });
                    }
                });
            });
        </script>
    </head>

    <body>
        <div id="app-loader">
            <div style="position: absolute; left: 50%; top: 30%; margin-left: -75px; margin-top: -60px; width: 150px; height: 140px; font-size: 16px;">
                <img src="/images/BrandingLogo.png"/>
                <i class="fa fa-spinner fa-spin fa-lg fa-fw"></i>
            </div>
        </div>

        <form name="exportGridSettings" id="exportGridSettings" method="post" action="gridSettings" style="display: none;">
            <input type="hidden" name="gridName" value=""/>
            <input type="hidden" name="gridData" value=""/>
            <input type="hidden" name="type" value="export"/>
        </form>

        <form name="downloadForm" id="downloadForm" method="POST" action="download" style="display: none;">
            <input type="hidden" name="type" value="" />
            <input type="hidden" name="arg1" value="" />
            <input type="hidden" name="arg2" value="" />
            <input type="hidden" name="arg3" value="" />
            <input type="hidden" name="arg4" value="" />
            <input type="hidden" name="arg5" value="" />
            <input type="hidden" name="arg6" value="" />
        </form>

    </body>
</html>
