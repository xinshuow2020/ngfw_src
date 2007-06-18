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

package com.untangle.uvm.engine;


import java.io.IOException;
import java.security.Principal;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.apache.catalina.authenticator.*;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;


/**
 * Sits in front of the Tomcat SingleSignOn to allow query string to contain
 * sso id, since we can't get cookies through jnlp.
 */

public class OurSingleSignOn extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    public static final String SSO_SESSION_PARAMETER_NAME = "jsessionidsso";

    /**
     * Descriptive information about this Valve implementation.
     */
    protected static String info =
        "com.untangle.uvm.engine.OurSingleSignOn";


    // ---------------------------------------------------------- Valve Methods

    /**
     * Perform single-sign-on support processing for this request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException {

        // Has a valid user already been authenticated?
        if (containerLog.isDebugEnabled())
            containerLog.debug("Process request for '" + request.getRequestURI() + "'");
        if (request.getUserPrincipal() != null) {
            if (containerLog.isDebugEnabled())
                containerLog.debug(" Principal '" + request.getUserPrincipal().getName() +
                                   "' has already been authenticated");
            getNext().invoke(request, response);
            return;
        }

        // Check for the single sign on cookie
        if (containerLog.isDebugEnabled())
            containerLog.debug(" Checking for SSO cookie");
        Cookie cookie = null;
        String ssoVal = null;
        Cookie cookies[] = request.getCookies();
        if (cookies == null)
            cookies = new Cookie[0];
        for (int i = 0; i < cookies.length; i++) {
            if (Constants.SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
                cookie = cookies[i];
                break;
            }
        }

        if (cookie == null) {
            // Check for the single sign on URL header
            ssoVal = getSsoSessionId(request);
            if (ssoVal != null) {
                cookie = new Cookie(Constants.SINGLE_SIGN_ON_COOKIE, ssoVal);
                cookie.setMaxAge(-1);
                cookie.setPath("/");
                request.addCookie(cookie);
            }
        }

        // Invoke the next Valve in our pipeline
        getNext().invoke(request, response);

    }


    // ------------------------------------------------------ Protected Methods

    /**
     * Get sso session id from query string.
     */
    protected String getSsoSessionId(Request req) {
        return req.getParameter(SSO_SESSION_PARAMETER_NAME);
    }
}
