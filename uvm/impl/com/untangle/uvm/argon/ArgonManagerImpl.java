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

package com.untangle.uvm.argon;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Properties;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;

import com.untangle.jnetcap.Netcap;
import com.untangle.jnetcap.InterfaceData;
import com.untangle.jnetcap.JNetcapException;

import com.untangle.uvm.ArgonException;
import com.untangle.uvm.ArgonManager;

import com.untangle.uvm.localapi.SessionMatcher;
import com.untangle.uvm.localapi.LocalIntfManager;

import com.untangle.uvm.node.firewall.InterfaceRedirect;


/**
 * Argon manager.
 *
 * @author <a href="mailto:rbscott@untangle.com">Robert Scott</a>
 * @version 1.0
 */
public class ArgonManagerImpl implements ArgonManager
{
    private static final ArgonManagerImpl INSTANCE = new ArgonManagerImpl();
    
    private final Netcap netcap = Netcap.getInstance();

    private final Logger logger = Logger.getLogger( this.getClass());

    private boolean isShutdown = false;
        
    private ArgonManagerImpl()
    {
    }

    /* Indicate that the shutdown process has started, this is used to prevent NAT from both
     * re-enabling the bridge during shutdown, Argon will do that automatically.
     * This also prevents nodes from registering or deregistering interfaces after shutdown 
     */
    synchronized void isShutdown() 
    {
        isShutdown = true;
    }    
            
    /* Set the interface override list. */
    public void setInterfaceOverrideList( List<InterfaceRedirect> overrideList )
    {
        InterfaceOverride.getInstance().setOverrideList( overrideList );
    }

    /* Set the interface override list. */
    public void clearInterfaceOverrideList()
    {
        InterfaceOverride.getInstance().clearOverrideList();
    }
    
    /* Get the outgoing argon interface for an IP address */
    public byte getOutgoingInterface( InetAddress destination ) throws ArgonException
    {
        try {
            byte netcapIntf = netcap.getOutgoingInterface( destination );
            
            return  Argon.getInstance().getIntfManager().toArgon( netcapIntf );
        } catch ( JNetcapException e ) {
            throw new ArgonException( e );
        }
    }

    /** Get the interface manager */
    public LocalIntfManager getIntfManager()
    {
        return Argon.getInstance().getIntfManager();
    }
    
    /** Get the number of sessions from the VectronTable */
    public int getSessionCount()
    {
        return VectronTable.getInstance().count();
    }
    
    /** Shutdown all of the sessions that match <code>matcher</code> */
    public void shutdownMatches( SessionMatcher matcher )
    {
        VectronTable.getInstance().shutdownMatches( matcher );
    }

    public static final ArgonManagerImpl getInstance()
    {
        return INSTANCE;
    }

    private void pause()
    {
        try {
            Thread.sleep( 2000 );
        } catch ( Exception e ) {
            logger.warn( "Interrupted while pausing", e );
        }
    }
}
