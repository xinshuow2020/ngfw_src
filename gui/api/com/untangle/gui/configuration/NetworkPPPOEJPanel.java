/*
 * $HeadURL$
 * Copyright (c) 2003-2007 Untangle, Inc. 
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2,
 * as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your
 * choice, provided that you also meet, for each linked independent module,
 * the terms and conditions of the license of that module.  An independent
 * module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version
 * of the library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.untangle.gui.configuration;

import java.awt.*;

import com.untangle.gui.node.*;
import com.untangle.gui.util.*;
import com.untangle.gui.widgets.dialogs.*;
import com.untangle.uvm.*;
import com.untangle.uvm.networking.BasicNetworkSettings;
import com.untangle.uvm.networking.PPPoEConnectionRule;
import com.untangle.uvm.security.*;
import com.untangle.uvm.node.*;

public class NetworkPPPOEJPanel extends javax.swing.JPanel
    implements Savable<NetworkCompoundSettings>, Refreshable<NetworkCompoundSettings> {

    private static final String EXCEPTION_NAME     = "You must specify a Name.";
    private static final String EXCEPTION_PASSWORD = "You must specify a Password.";

    public NetworkPPPOEJPanel() {
        initComponents();
        optionJScrollPane.getVerticalScrollBar().setFocusable(false);
        Util.addPanelFocus(this, pppoeDisabledJRadioButton);
        Util.addFocusHighlight(nameJTextField);
        Util.addFocusHighlight(passwordJPasswordField);
        Util.addFocusHighlight(optionJEditorPane);
    }

    // SETTINGS CHANGE NOTIFICATION /////////
    private SettingsChangedListener settingsChangedListener;
    public void setSettingsChangedListener(SettingsChangedListener settingsChangedListener){
        this.settingsChangedListener = settingsChangedListener;
    }
    ///////////////////////////////////////////

    public void doSave(NetworkCompoundSettings networkCompoundSettings, boolean validateOnly) throws Exception {

        // PPPOE ENABLED //////////
        boolean isPPPOEEnabled = pppoeEnabledJRadioButton.isSelected();

        // NAME  ////////////
        String name = null;
        nameJTextField.setBackground( Color.WHITE );
        if( isPPPOEEnabled ){
            name = nameJTextField.getText();
            if( name.trim().length() == 0 ){
                nameJTextField.setBackground( Util.INVALID_BACKGROUND_COLOR );
                throw new Exception(EXCEPTION_NAME);
            }
        }

        // PASSWORD //
        String password = null;
        passwordJPasswordField.setBackground( Color.WHITE );
        if( isPPPOEEnabled ){
            password = new String( passwordJPasswordField.getPassword() );
            if( password.trim().length() == 0 ){
                passwordJPasswordField.setBackground( Util.INVALID_BACKGROUND_COLOR );
                throw new Exception(EXCEPTION_PASSWORD);
            }
        }

        // OPTIONS //
        String options = null;
        if( isPPPOEEnabled ){
            options = optionJEditorPane.getText();
        }


        // SAVE SETTINGS ////////////
        if( !validateOnly ){
            BasicNetworkSettings basicSettings = networkCompoundSettings.getBasicSettings();
            PPPoEConnectionRule connectionRule = basicSettings.getPPPoESettings();
            connectionRule.setLive( isPPPOEEnabled );
            if( isPPPOEEnabled ){
                connectionRule.setUsername( name );
                connectionRule.setPassword( password );
                connectionRule.setSecretField( options );
            }
        }
    }


    boolean isPPPOEEnabledCurrent;
    String nameCurrent;
    String passwordCurrent;
    String optionCurrent;

    public void doRefresh(NetworkCompoundSettings networkCompoundSettings){
        BasicNetworkSettings basicSettings = networkCompoundSettings.getBasicSettings();
        PPPoEConnectionRule connectionRule = basicSettings.getPPPoESettings();

        // PPPOE ENABLED /////
        isPPPOEEnabledCurrent = connectionRule.isLive();
        setPPPOEEnabledDependency( isPPPOEEnabledCurrent );
        if( isPPPOEEnabledCurrent )
            pppoeEnabledJRadioButton.setSelected(true);
        else
            pppoeDisabledJRadioButton.setSelected(true);
        Util.addSettingChangeListener(settingsChangedListener, this, pppoeEnabledJRadioButton);
        Util.addSettingChangeListener(settingsChangedListener, this, pppoeDisabledJRadioButton);

        // NAME ////
        nameCurrent = connectionRule.getUsername();
        nameJTextField.setText( nameCurrent );
        nameJTextField.setBackground( Color.WHITE );
        Util.addSettingChangeListener(settingsChangedListener, this, nameJTextField);

        // PASSWORD ////
        passwordCurrent = connectionRule.getPassword();
        passwordJPasswordField.setText( passwordCurrent );
        passwordJPasswordField.setBackground( Color.WHITE );
        Util.addSettingChangeListener(settingsChangedListener, this, passwordJPasswordField);

        // OPTIONS //
        optionCurrent = connectionRule.getSecretField();
        optionJEditorPane.setText( optionCurrent );
        Util.addSettingChangeListener(settingsChangedListener, this, optionJEditorPane);

    }


    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        pppoeButtonGroup = new javax.swing.ButtonGroup();
        dhcpJPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        pppoeDisabledJRadioButton = new javax.swing.JRadioButton();
        pppoeEnabledJRadioButton = new javax.swing.JRadioButton();
        staticIPJPanel = new javax.swing.JPanel();
        nameJLabel = new javax.swing.JLabel();
        nameJTextField = new javax.swing.JTextField();
        passwordJLabel = new javax.swing.JLabel();
        passwordJPasswordField = new javax.swing.JPasswordField();
        jSeparator4 = new javax.swing.JSeparator();
        optionalJLabel = new javax.swing.JLabel();
        optionJScrollPane = new javax.swing.JScrollPane();
        optionJEditorPane = new javax.swing.JEditorPane();

        setLayout(new java.awt.GridBagLayout());

        setMaximumSize(new java.awt.Dimension(563, 470));
        setMinimumSize(new java.awt.Dimension(563, 470));
        setPreferredSize(new java.awt.Dimension(563, 470));
        dhcpJPanel.setLayout(new java.awt.GridBagLayout());

        dhcpJPanel.setBorder(new javax.swing.border.TitledBorder(null, "PPP Over Ethernet (PPPoE) Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 16)));
        jLabel11.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel11.setText("<html>You may need PPPoE to be enabled if you are connecting to the Internet via certain Internet Service Providers (ISPs) using a DSL modem, or certain other types of modems.  Your ISP should tell you if they require you to use PPPoE or not.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        dhcpJPanel.add(jLabel11, gridBagConstraints);

        pppoeButtonGroup.add(pppoeDisabledJRadioButton);
        pppoeDisabledJRadioButton.setFont(new java.awt.Font("Dialog", 0, 12));
        pppoeDisabledJRadioButton.setText("<html><b>Disable</b>  PPPoE.  (This is the default setting)</html>");
        pppoeDisabledJRadioButton.setActionCommand("<html><b>Use DHCP</b> to automatically set Untangle's IP address from the network's DHCP server.</html>");
        pppoeDisabledJRadioButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    pppoeDisabledJRadioButtonActionPerformed(evt);
                }
            });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 50, 0, 0);
        dhcpJPanel.add(pppoeDisabledJRadioButton, gridBagConstraints);

        pppoeButtonGroup.add(pppoeEnabledJRadioButton);
        pppoeEnabledJRadioButton.setFont(new java.awt.Font("Dialog", 0, 12));
        pppoeEnabledJRadioButton.setText("<html><b>Enable</b> PPPoE.</html>");
        pppoeEnabledJRadioButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    pppoeEnabledJRadioButtonActionPerformed(evt);
                }
            });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 50, 0, 0);
        dhcpJPanel.add(pppoeEnabledJRadioButton, gridBagConstraints);

        staticIPJPanel.setLayout(new java.awt.GridBagLayout());

        nameJLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        nameJLabel.setText("Name: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        staticIPJPanel.add(nameJLabel, gridBagConstraints);

        nameJTextField.addCaretListener(new javax.swing.event.CaretListener() {
                public void caretUpdate(javax.swing.event.CaretEvent evt) {
                    nameJTextFieldCaretUpdate(evt);
                }
            });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        staticIPJPanel.add(nameJTextField, gridBagConstraints);

        passwordJLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        passwordJLabel.setText("Password: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        staticIPJPanel.add(passwordJLabel, gridBagConstraints);

        passwordJPasswordField.addCaretListener(new javax.swing.event.CaretListener() {
                public void caretUpdate(javax.swing.event.CaretEvent evt) {
                    passwordJPasswordFieldCaretUpdate(evt);
                }
            });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        staticIPJPanel.add(passwordJPasswordField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        dhcpJPanel.add(staticIPJPanel, gridBagConstraints);

        jSeparator4.setForeground(new java.awt.Color(200, 200, 200));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        dhcpJPanel.add(jSeparator4, gridBagConstraints);

        optionalJLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        optionalJLabel.setText("<html>The <b>Optional Command</b> allows you to specify more PPPoE related options.  This should only be changed if told to do so by an Administrator, ISP, or Untangle Technical Support.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        dhcpJPanel.add(optionalJLabel, gridBagConstraints);

        optionJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        optionJScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        optionJScrollPane.setMinimumSize(new java.awt.Dimension(22, 200));
        optionJScrollPane.setPreferredSize(new java.awt.Dimension(3, 200));
        optionJEditorPane.addCaretListener(new javax.swing.event.CaretListener() {
                public void caretUpdate(javax.swing.event.CaretEvent evt) {
                    optionJEditorPaneCaretUpdate(evt);
                }
            });

        optionJScrollPane.setViewportView(optionJEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        dhcpJPanel.add(optionJScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(dhcpJPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void optionJEditorPaneCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_optionJEditorPaneCaretUpdate

    }//GEN-LAST:event_optionJEditorPaneCaretUpdate

    private void passwordJPasswordFieldCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_passwordJPasswordFieldCaretUpdate

    }//GEN-LAST:event_passwordJPasswordFieldCaretUpdate

    private void nameJTextFieldCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_nameJTextFieldCaretUpdate

    }//GEN-LAST:event_nameJTextFieldCaretUpdate

    private void pppoeEnabledJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pppoeEnabledJRadioButtonActionPerformed
        setPPPOEEnabledDependency( true );
    }//GEN-LAST:event_pppoeEnabledJRadioButtonActionPerformed

    private void pppoeDisabledJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pppoeDisabledJRadioButtonActionPerformed
        setPPPOEEnabledDependency( false );
    }//GEN-LAST:event_pppoeDisabledJRadioButtonActionPerformed

    private void setPPPOEEnabledDependency(boolean enabled){
        nameJLabel.setEnabled( enabled );
        nameJTextField.setEnabled( enabled );
        passwordJLabel.setEnabled( enabled );
        passwordJPasswordField.setEnabled( enabled );
        optionalJLabel.setEnabled( enabled );
        optionJScrollPane.setEnabled( enabled );
        optionJEditorPane.setEnabled( enabled );
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel dhcpJPanel;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel nameJLabel;
    public javax.swing.JTextField nameJTextField;
    private javax.swing.JEditorPane optionJEditorPane;
    private javax.swing.JScrollPane optionJScrollPane;
    private javax.swing.JLabel optionalJLabel;
    private javax.swing.JLabel passwordJLabel;
    private javax.swing.JPasswordField passwordJPasswordField;
    private javax.swing.ButtonGroup pppoeButtonGroup;
    public javax.swing.JRadioButton pppoeDisabledJRadioButton;
    public javax.swing.JRadioButton pppoeEnabledJRadioButton;
    private javax.swing.JPanel staticIPJPanel;
    // End of variables declaration//GEN-END:variables


}
