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

package com.untangle.uvm.user;

import com.untangle.uvm.node.ValidateException;

public class RemotePhoneBookImpl implements RemotePhoneBook
{
    private final LocalPhoneBook local;
    
    public RemotePhoneBookImpl( LocalPhoneBook local )
    {
        this.local = local;
    }
                         
    /* retrieve the WMI settings */
    public WMISettings getWMISettings()
    {
        return local.getWMISettings();
    }
    
    /* set the WMI settings */
    public void setWMISettings( WMISettings settings ) throws ValidateException
    {
        local.setWMISettings( settings );
    }

    public String productIdentifier()
    {
        return local.productIdentifier();
    }
}
