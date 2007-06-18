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

package com.untangle.gui.node;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import com.untangle.gui.main.*;
import com.untangle.gui.pipeline.*;
import com.untangle.gui.util.*;
import com.untangle.gui.widgets.dialogs.*;
import com.untangle.uvm.*;
import com.untangle.uvm.toolbox.MackageDesc;

public class MNodeJButton extends JButton {

    private MackageDesc mackageDesc;
    private GridBagConstraints gridBagConstraints;
    private JProgressBar statusJProgressBar;
    private JLabel nameJLabel;
    private JLabel organizationIconJLabel;
    private JLabel descriptionIconJLabel;
    private String toolTipString;
    private boolean isTrial = false;
    private JPanel contentJPanel;

    private static Color COLOR_NAME_DISABLED = new Color(140,140,140);

    public MNodeJButton(MackageDesc mackageDesc) {
        this.mackageDesc = mackageDesc;

        // INITIAL LAYOUT
        contentJPanel = new JPanel();
        contentJPanel.setOpaque(false);
        contentJPanel.setLayout(new GridBagLayout());
        setLayout(new BorderLayout());
        add(contentJPanel);

        // ORG ICON
        /*
          organizationIconJLabel = new JLabel();
          if( mackageDesc.getOrgIcon() != null )
          organizationIconJLabel.setIcon( new javax.swing.ImageIcon(mackageDesc.getOrgIcon()) );
          else
          organizationIconJLabel.setIcon( new ImageIcon(getClass().getResource("/com/untangle/gui/node/IconOrgUnknown42x42.png")) );
        */
        //organizationIconJLabel.setDisabledIcon(this.orgIcon);
        /*
          organizationIconJLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
          organizationIconJLabel.setFocusable(false);
          organizationIconJLabel.setPreferredSize(new java.awt.Dimension(42, 42));
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 0;
          gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
          gridBagConstraints.weightx = 0.5;
          gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 4);
          contentJPanel.add(organizationIconJLabel, gridBagConstraints);
        */

        // DESC ICON
        descriptionIconJLabel = new JLabel();
        if( mackageDesc.getDescIcon() != null )
            descriptionIconJLabel.setIcon( new javax.swing.ImageIcon(mackageDesc.getDescIcon()) );
        else
            descriptionIconJLabel.setIcon( new ImageIcon(getClass().getResource("/com/untangle/gui/node/IconDescUnknown42x42.png")) );
        //descriptionIconJLabel.setDisabledIcon(this.descIcon);
        descriptionIconJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        descriptionIconJLabel.setFocusable(false);
        descriptionIconJLabel.setPreferredSize(new java.awt.Dimension(42, 48));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 2);
        contentJPanel.add(descriptionIconJLabel, gridBagConstraints);

        //DISPLAY NAME
        nameJLabel = new JLabel();
        String tempName;
        tempName = mackageDesc.getDisplayName();
        if( (mackageDesc.getExtraName()!=null) && (mackageDesc.getExtraName().contains("Trial")) )
            tempName += " (Trial)";
        nameJLabel.setText( "<html>" + Util.wrapString(tempName, 19) + "</html>");
        nameJLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        nameJLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        nameJLabel.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        contentJPanel.add(nameJLabel, gridBagConstraints);
        try{ ((JComponent)nameJLabel).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY, new Boolean(true)); }
        catch(Throwable t){}

        //status progressbar
        statusJProgressBar = new JProgressBar();
        statusJProgressBar.setBorderPainted(true);
        //statusJProgressBar.setVisible(true);
        statusJProgressBar.setVisible(false);
        statusJProgressBar.setStringPainted(true);
        statusJProgressBar.setOpaque(true);
        statusJProgressBar.setIndeterminate(false);
        statusJProgressBar.setValue(0);
        //statusJProgressBar.setForeground(new java.awt.Color(68, 91, 255));
        statusJProgressBar.setFont(new java.awt.Font("Dialog", 0, 12));
        statusJProgressBar.setPreferredSize(new java.awt.Dimension(130, 20));
        statusJProgressBar.setMaximumSize(new java.awt.Dimension(130, 20));
        statusJProgressBar.setMinimumSize(new java.awt.Dimension(130, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        contentJPanel.add(statusJProgressBar, gridBagConstraints);

        // TOOLTIP
        toolTipString = Util.wrapString( mackageDesc.getLongDescription(), 80);

        setMargin(new Insets(6,0,6,0));
        setFocusPainted(false);
        setFocusable(false);
        setOpaque(false);

    }

    public Dimension getPreferredSize(){
        Dimension d = contentJPanel.getPreferredSize();
        Insets i = getMargin();
        d.width = 140;
        d.height += i.top;
        d.height += i.bottom;
        return d;
    }

    public MNodeJButton duplicate(){
        MNodeJButton newMNodeJButton = new MNodeJButton( mackageDesc );
        Dimension currentDimension = getSize();
        newMNodeJButton.setMinimumSize(currentDimension);
        newMNodeJButton.setMaximumSize(currentDimension);
        newMNodeJButton.setPreferredSize(currentDimension);
        return newMNodeJButton;
    }

    public boolean getIsTrial(){ return isTrial; }
    public void setIsTrial(boolean isTrial){
        this.isTrial = isTrial;
        /*
          String tempName;
          if(isTrial)
          tempName = mackageDesc.getDisplayName() + " (Trial)";
          else
          tempName = mackageDesc.getDisplayName();
          nameJLabel.setText( "<html><b>" + Util.wrapString(tempName, 19) + "</b></html>");
        */
    }



    // CONVENIENCE WRAPPERS FOR MACKAGE /////////
    public MackageDesc getMackageDesc(){ return mackageDesc; }
    public String getFullDescription(){ return new String( mackageDesc.getLongDescription() ); }
    public String getShortDescription(){ return new String( mackageDesc.getShortDescription() ); }
    public String getExtraName(){ return mackageDesc.getExtraName(); }
    public String getName(){
        String name = mackageDesc.getName();
        //if(isTrial)
        //    name = name.replace("-libitem", "-trial30-libitem");
        return name;
    }
    public String getDisplayName(){
        return mackageDesc.getDisplayName();
    }
    public int    getViewPosition(){ return mackageDesc.getViewPosition(); }
    public String getPrice(){ return mackageDesc.getPrice(); }
    public String getWebpage(){ return mackageDesc.getWebsite(); }
    ////////////////////////////////////////////

    public void highlight(){
        new FadeTask(this,false);
    }


    // VIEW UPDATING ///////////
    private void updateView(final String message, final int progress, final String toolTip, final boolean isEnabled, boolean doNow){
        if(doNow){
            MNodeJButton.this.setProgress(message, progress);
            MNodeJButton.this.setTT(toolTip);
            MNodeJButton.this.setEnabled(isEnabled);
        }
        else{
            SwingUtilities.invokeLater( new Runnable() { public void run() {
                MNodeJButton.this.setProgress(message, progress);
                MNodeJButton.this.setTT(toolTip);
                MNodeJButton.this.setEnabled(isEnabled);
            } } );
        }
    }

    public void setDeployableView(){ updateView(null, -1, "Ready to be installed into the rack.", true, false); }
    public void setProcurableView(){ updateView(null, -1, "Ready to be purchased from the store.", true, false); }
    public void setDeployedView(){ updateView(null, -1, "Installed into rack.", false, false); }

    public void setDeployingView(){ updateView("Installing", 101, "Installing.", false, true); }
    public void setProcuringView(){ updateView("Downloading", 101, "Downloading.", false, true); }
    public void setRemovingFromToolboxView(){ updateView("Removing", 101, "Removing from the toolbox.", false, true); }
    public void setRemovingFromRackView(){ updateView("Removing", 101, "Removing from the rack.", false, false); }

    public void setFailedInitView(){ updateView(null, -1, "Failed graphical initialization.", false, false); }
    public void setFailedProcureView(){ updateView(null, -1, "Failed purchase.", true, false); }
    public void setFailedDeployView(){ updateView(null, -1, "Failed installation.", true, false); }
    public void setFailedRemoveFromToolboxView(){ updateView(null, -1, "Failed removal from toolbox.", true, false); }
    public void setFailedRemoveFromRackView(){ updateView(null, -1, "Failed removal from rack.", false, false); }
    /////////////////////////////


    // VIEW UPDATE HELPERS //////////////////

    public void setTT(String status){
        this.setToolTipText( "<html>" + "<b>Description:</b><br>" + toolTipString + "<br><br>" + "<b>Status:</b><br>" + status + "</html>");
    }

    public void setProgress(String message, int progress){
        statusJProgressBar.setString(message);
        if(progress < 0){
            statusJProgressBar.setIndeterminate(false);
            statusJProgressBar.setVisible(false);
        }
        else if(progress <= 100){
            statusJProgressBar.setIndeterminate(false);
            statusJProgressBar.setValue(progress<1?1:progress);
            statusJProgressBar.setVisible(true);
        }
        else{
            statusJProgressBar.setIndeterminate(true);
            statusJProgressBar.setVisible(true);
        }
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        //organizationIconJLabel.setEnabled(enabled);
        descriptionIconJLabel.setEnabled(enabled);
        nameJLabel.setEnabled(enabled);
        if(enabled)
            nameJLabel.setForeground(Color.BLACK);
        else
            nameJLabel.setForeground(COLOR_NAME_DISABLED);
    }
    ///////////////////////////////////



}
