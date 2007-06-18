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


package com.untangle.jnetcap;

public interface TCPEndpoints extends Endpoints {
    public int fd();

    /**
     * Configure a TCP File descriptor for blocking or non-blocking mode.<p/>
     *
     * @param mode <code>true</code> enable blocking, <code>false</code> to disable blocking.
     */
    public void blocking( boolean mode );

    public int read( byte[] data );

    public int write( byte[] data );
    public int write( String data );

    public void close();
}
