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

package com.untangle.node.openvpn;

import javax.persistence.MappedSuperclass;

import com.untangle.uvm.node.IPaddr;
import com.untangle.uvm.node.Rule;
import com.untangle.uvm.node.Validatable;
import com.untangle.uvm.node.ValidateException;
import org.hibernate.annotations.Type;

/**
 * A network that is available at a site.
 *
 * @author <a href="mailto:rbscott@untangle.com">Robert Scott</a>
 * @version 1.0
 */
@MappedSuperclass
public abstract class SiteNetwork extends Rule implements Validatable
{
    private static final long serialVersionUID = -2918169040527785684L;

    private IPaddr network;
    private IPaddr netmask;

    // constructors -----------------------------------------------------------

    public SiteNetwork() { }

    // accessors --------------------------------------------------------------

    /**
     * @return network exported by this client or server.
     */
    @Type(type="com.untangle.uvm.type.IPaddrUserType")
    public IPaddr getNetwork()
    {
        return this.network;
    }

    public void setNetwork( IPaddr network )
    {
        this.network = network;
    }

    /**
     * Get the range of netmask on the client side(null for site->machine).
     *
     * @return This is the network that is reachable when this client connects.
     */
    @Type(type="com.untangle.uvm.type.IPaddrUserType")
    public IPaddr getNetmask()
    {
        return this.netmask;
    }

    public void setNetmask( IPaddr netmask )
    {
        this.netmask = netmask;
    }

    public void validate() throws ValidateException
    {
        /* XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */
    }
}
