// Copyright (c) 2006 Metavize Inc.
// All rights reserved.

// test

function testDialog()
{
   var dialog = new FileDialog("Test Dialog", "HOLY CRAP");
   dialog.setVisible(true);
}

// Dialog

function Dialog(title, content)
{
   if (title) {
      this.init(title, content);
   }
}

Dialog.prototype = {
   init: function(title, content) {
      this.div = document.createElement("div");

      var s = this.div.appendChild(document.createElement("div"));
      Element.addClassName(s, "shadow");

      var md = s.appendChild(document.createElement("div"));
      Element.addClassName(md, "dialog");

      var titlebar = new TitleBar(md, title);
      with (this) {
         titlebar.onClose = function() { setVisible(false); };
      }

      md.appendChild(content);

      this.clickBlocker = new ClickBlocker();
   },

   setVisible: function(visible) {
      if (visible != this.visible) {
         if (visible) {
            var b = document.getElementsByTagName("body")[0];
            b.appendChild(this.div);
         } else {
            this.div.parentNode.removeChild(this.div);
         }

         this.clickBlocker.setVisible(visible);
      }

      this.visible = visible;
   }
}

// FileDialog

FileDialog.prototype = new Dialog();
FileDialog.prototype.constructor = FileDialog;
FileDialog.superclass = Dialog.prototype;

function FileDialog(title, message)
{
   if (title) {
      this.init(title, message);
   }
}

FileDialog.prototype.init = function(title, message)
{
   var panel = document.createElement("div");
   Element.addClassName(panel, "panel");

   var label = panel.appendChild(document.createElement("div"));
   Element.addClassName(label, "label");
   label.appendChild(document.createTextNode(message));

   var form = panel.appendChild(document.createElement("form"));
   form.action = "put"
   form.method = "post";
   form.enctype = form.encoding = "multipart/form-data";
   form.target = "hidden-target";

   var iframe = makeHiddenIFrame(form, "hidden-target");
   with (this) {
      iframe.uploadComplete = function() {
         setTimeout(function() { setVisible(false); }, 1000);
      };
   }

   var file = document.createElement("input");
   file.type = "file";
   file.name = "file";
   form.appendChild(file);

   var input = document.createElement("input");
   input.type = "submit";
   input.value = "Upload";
   form.appendChild(input);

   form.onsubmit = function() {
      file.disabled = true;
      input.disabled = true;
   }

   // XXX close when do
   FileDialog.superclass.init.call(this, title, panel);
}

// TitleBar

function TitleBar(parent, title)
{
   this.parent = parent;

   var elem = parent.appendChild(document.createElement("table"));
   Element.addClassName(elem, "titlebar");

   var tbody = elem.appendChild(document.createElement("tbody"));

   var tr = tbody.appendChild(document.createElement("tr"));

   var td = tr.appendChild(document.createElement("td"));
   this.titleText = td.appendChild(document.createTextNode(title));

   td = tr.appendChild(document.createElement("td"));
   Element.addClassName(td, "titlebar-close");

   var close = td.appendChild(document.createElement("img"));
   close.src = "close.gif";
   with (this) {
      close.onclick = function() { if (onClose) { onClose(); } };
   }
}

TitleBar.prototype = {
      setTitle: function(title) {
         this.titleText.textContent = title;
      },

      onClose: null
}

// ClickBlocker

function ClickBlocker()
{
   this.div = document.createElement("div");
   this.div.id = "click-blocker";
   this.visible = false;
}

ClickBlocker.prototype = {
   setVisible: function(visible) {
      if (visible != this.visible) {
         if (visible) {
            var b = document.getElementsByTagName("body")[0];
            b.appendChild(this.div);
         } else {
            this.div.parentNode.removeChild(this.div);
         }
      }

      this.visible = visible;
   }
}

// util

function makeHiddenIFrame(parent, name)
{
   parent.innerHTML += '\<iframe id="' + name + '" name="' + name
      + '" style="display: none"><\/iframe>';

   var children = parent.childNodes;
   for (var i = 0; i < children.length; i++) {
      var c = children[i];
      if (c.id == name) {
         return c;
      }
   }
}
