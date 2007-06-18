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


package com.untangle.gui.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.untangle.gui.login.MLoginJFrame;

public class DropdownLoginTask implements ActionListener {

    // CONSTANTS ////////////////////
    private static final int STEPS = 10;
    private static final int STEP_DELAY = 100;
    /////////////////////////////////

    private MLoginJFrame mLoginJFrame;
    private Timer dropdownTask;
    private Window parentWindow;
    private JComponent childComponent;
    private Dimension minDimension, maxDimension;
    private boolean goingDown;
    private int currentStep;
    private int currentY;

    public DropdownLoginTask(MLoginJFrame mLoginJFrame, JComponent childComponent){
        this.mLoginJFrame = mLoginJFrame;
        this.childComponent = childComponent;
        this.minDimension = childComponent.getMinimumSize();
        this.maxDimension = childComponent.getMaximumSize();
        dropdownTask = new Timer(STEP_DELAY, this);
    }

    public void start(boolean goingDown){
        this.goingDown = goingDown;
        if(goingDown){
            currentStep = 0;
        }
        else{
            currentStep = STEPS -1;
        }
        dropdownTask.start();
    }

    public void actionPerformed(ActionEvent evt){

        if( goingDown ){
            if( currentStep == 0 ){
            }
            else if( (currentStep>0) && (currentStep<STEPS-1) ){
                updatePanelPosition();
            }
            else{ //( currentStep == STEPS-1 )
                updatePanelPosition();
                mLoginJFrame.setInputsEnabled(true);
                dropdownTask.stop();
                return;
            }
            currentStep++;
        }
        else{
            currentStep--;
            if( currentStep == STEPS-1 ){
                updatePanelPosition();
            }
            else if( (currentStep<STEPS-1) && (currentStep>-1) ){
                updatePanelPosition();
            }
            else{ // (currentStep==-1)
                dropdownTask.stop();
                return;
            }
        }
    }

    private void updatePanelPosition(){
        currentY = (int) (minDimension.height + (((float)(maxDimension.height - minDimension.height))/((float)STEPS-1))*(float)currentStep);
        childComponent.setPreferredSize(new Dimension(childComponent.getWidth(), currentY));
        mLoginJFrame.pack();
        mLoginJFrame.repaint();
    }

}
