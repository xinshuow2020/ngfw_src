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

package com.untangle.node.spyware;

import java.io.IOException;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.untangle.uvm.LocalUvmContextFactory;
import com.untangle.uvm.security.Tid;
import com.untangle.uvm.node.LocalNodeManager;
import com.untangle.uvm.node.NodeContext;
import com.untangle.uvm.node.NodeManager;

public class UnblockerServlet extends HttpServlet
{
    // HttpServlet methods ----------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException
    {
        resp.setContentType("text/xml");
        resp.addHeader("Cache-Control", "no-cache");

        String nonce = req.getParameter("nonce");
        String tidStr = req.getParameter("tid");
        boolean global = Boolean.parseBoolean(req.getParameter("global"));

        try {
            LocalNodeManager tman = LocalUvmContextFactory.context().nodeManager();
            Tid tid = new Tid(Long.parseLong(tidStr));
            NodeContext tctx = tman.nodeContext(tid);
            Spyware tran = (Spyware)tctx.node();

            if (tran.unblockSite(nonce, global)) {
                resp.getOutputStream().println("<success/>");
            } else {
                resp.getOutputStream().println("<failure/>");
            }
        } catch (IOException exn) {
            throw new ServletException(exn);
        }
    }
}

