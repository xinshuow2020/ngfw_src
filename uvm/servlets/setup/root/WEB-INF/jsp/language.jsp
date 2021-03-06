<!DOCTYPE html>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib uri="http://java.untangle.com/jsp/uvm" prefix="uvm" %>
<html xmlns:uvm="http://java.untangle.com/jsp/uvm">
  <head>
    <meta charset="UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <title>Setup Wizard</title>
    <style type="text/css">
        @import "/ext6/classic/theme-${extjsTheme}/resources/theme-${extjsTheme}-all.css?s=${buildStamp}";
    </style>
    
    <uvm:skin src="common.css?s=${buildStamp}" name="${skinName}"/>
    <uvm:skin src="admin.css?s=${buildStamp}" name="${skinName}"/>

    <script type="text/javascript" src="/ext6/ext-all.js?s=${buildStamp}"></script>
    <script type="text/javascript" src="/ext6/classic/theme-${extjsTheme}/theme-${extjsTheme}.js?s=${buildStamp}"></script>
    
    <script type="text/javascript" src="/jsonrpc/jsonrpc.js?s=${buildStamp}"></script>
    <script type="text/javascript" src="/script/i18n.js?s=${buildStamp}"></script>
    <script type="text/javascript" src="/script/wizard.js?s=${buildStamp}"></script>
    
    <script type="text/javascript" src="script/language.js?s=${buildStamp}"></script>
    <script type="text/javascript" src="script/util.js?s=${buildStamp}"></script>

    <script type="text/javascript">
    Ext.onReady(function(){
        Ung.Language.init({
            languageList : ${languageList},
            language : "${language}",
            languageSource : "${languageSource}"
        });
    });
    </script>
  </head>

  <body class="wizard">
    <div id="container"></div>
  </body>
</html>
