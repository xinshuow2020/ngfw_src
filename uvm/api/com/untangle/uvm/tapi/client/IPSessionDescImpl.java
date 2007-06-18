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

package com.untangle.uvm.tapi.client;

import com.untangle.uvm.tapi.IPSessionDesc;
import com.untangle.uvm.tapi.SessionStats;
import java.net.InetAddress;

class IPSessionDescImpl extends SessionDescImpl implements IPSessionDesc {

    protected byte clientState;
    protected byte serverState;

    protected short protocol;

    protected byte clientIntf;
    protected byte serverIntf;

    protected boolean isInbound;

    protected InetAddress clientAddr;
    protected InetAddress serverAddr;

    protected int clientPort;
    protected int serverPort;

    protected IPSessionDescImpl(int id, short protocol, SessionStats stats,
                                byte clientState, byte serverState,
                                byte clientIntf, byte serverIntf,
                                InetAddress clientAddr, InetAddress serverAddr,
                                int clientPort, int serverPort, boolean isInbound)
    {
        super(id, stats);
        this.protocol = protocol;
        this.clientState = clientState;
        this.serverState = serverState;
        this.clientIntf = clientIntf;
        this.serverIntf = serverIntf;
        this.clientAddr = clientAddr;
        this.serverAddr = serverAddr;
        this.clientPort = clientPort;
        this.serverPort = serverPort;
        this.isInbound = isInbound;
    }

    public short protocol()
    {
        return protocol;
    }
    
    public byte clientIntf()
    {
        return clientIntf;
    }

    public byte serverIntf()
    {
        return serverIntf;
    }

    public byte clientState()
    {
        return clientState;
    }

    public byte serverState()
    {
        return serverState;
    }

    public InetAddress clientAddr()
    {
        return clientAddr;
    }

    public InetAddress serverAddr()
    {
        return serverAddr;
    }

    public int clientPort()
    {
        return clientPort;
    }

    public int serverPort()
    {
        return serverPort;
    }

    public boolean isInbound()
    {
        return isInbound;
    }

    public boolean isOutbound()
    {
        return !isInbound;
    }
}
