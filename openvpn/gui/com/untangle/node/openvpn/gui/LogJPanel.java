/*
 * $HeadURL$
 * Copyright (c) 2003-2007 Untangle, Inc. 
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */


package com.untangle.node.openvpn.gui;

import java.util.*;
import javax.swing.table.*;

import com.untangle.gui.node.*;
import com.untangle.gui.util.*;
import com.untangle.gui.widgets.editTable.*;
import com.untangle.uvm.logging.EventManager;
import com.untangle.uvm.logging.EventRepository;
import com.untangle.uvm.logging.RepositoryDesc;
import com.untangle.uvm.node.Node;
import com.untangle.node.openvpn.*;

public class LogJPanel extends MLogTableJPanel {

    private VpnNode vpnNode;

    public LogJPanel(Node node, MNodeControlsJPanel mNodeControlsJPanel){
        super(node, mNodeControlsJPanel);

        vpnNode = (VpnNode)logNode;

        setTableModel(new LogTableModel());

        EventManager<ClientConnectEvent> eventManager = vpnNode.getClientConnectEventManager();
        for (RepositoryDesc fd : eventManager.getRepositoryDescs()) {
            queryJComboBox.addItem(fd.getName());
        }
    }

    protected void refreshSettings(){
        EventManager<ClientConnectEvent> em = vpnNode.getClientConnectEventManager();
        EventRepository<ClientConnectEvent> ef = em.getRepository((String)queryJComboBox.getSelectedItem());
        settings = ef.getEvents();
    }

    class LogTableModel extends MSortedTableModel<Object>{

        public TableColumnModel getTableColumnModel(){
            DefaultTableColumnModel tableColumnModel = new DefaultTableColumnModel();
            //                                 #   min  rsz    edit   remv   desc   typ               def
            addTableColumn( tableColumnModel,  0,  150, true,  false, false, false, Date.class,   null, "start time" );
            addTableColumn( tableColumnModel,  1,  150, true,  false, false, false, Date.class,   null, "end time" );
            addTableColumn( tableColumnModel,  2,  150, true,  false, false, false, String.class, null, "client name" );
            addTableColumn( tableColumnModel,  3,  150, true,  false, false, false, IPPortString.class, null, "client address" );
            addTableColumn( tableColumnModel,  4,  100, true,  false, false, false, Integer.class, null, sc.html("Kbytes<br>sent") );
            addTableColumn( tableColumnModel,  5,  100, true,  false, false, false, Integer.class, null, sc.html("Kbytes<br>received") );

            return tableColumnModel;
        }

        public void generateSettings(Object settings, Vector<Vector> tableVector, boolean validateOnly) throws Exception {}

        public Vector<Vector> generateRows(Object settings){
            List<ClientConnectEvent> requestLogList = (List<ClientConnectEvent>) settings;
            Vector<Vector> allEvents = new Vector<Vector>(requestLogList.size());
            Vector event;

            for( ClientConnectEvent requestLog : requestLogList ){

                event = new Vector(6);
                event.add( requestLog.getStart() );
                event.add( (requestLog.getEnd()==null?new Date(0):requestLog.getEnd()) );
                event.add( requestLog.getClientName() );
                event.add( new IPPortString(requestLog.getAddress(), requestLog.getPort()) );
                event.add( requestLog.getBytesTx()/1024l );
                event.add( requestLog.getBytesRx()/1024l );
                allEvents.add( event );
            }

            return allEvents;
        }

    }

}
