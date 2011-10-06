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

package com.untangle.node.spam;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;

import com.untangle.uvm.logging.LogEvent;
import com.untangle.uvm.logging.MailLogEventFromReports;
import com.untangle.uvm.logging.RepositoryDesc;
import com.untangle.uvm.logging.ListEventFilter;
import com.untangle.uvm.util.I18nUtil;

public class SpamSpamFilter implements ListEventFilter<MailLogEventFromReports>
{
    private static final RepositoryDesc SPAM_REPO_DESC = new RepositoryDesc(I18nUtil.marktr("Spam Events"));
    // XXX Hack - specify clamphish label here
    private static final RepositoryDesc CLAM_REPO_DESC = new RepositoryDesc(I18nUtil.marktr("Identity Theft Events"));

    private final String vendor;
    private final String logQuery;
    private final RepositoryDesc repoDesc;

    // constructors -----------------------------------------------------------

    public SpamSpamFilter(String vendor)
    {
        if (vendor.equals("SpamAssassin")) //FIXME: more cases
            this.vendor = "sa";
        else if (vendor.equals("CommtouchAs"))
            this.vendor = "ct";
        else
            this.vendor = vendor;

        logQuery = "FROM MailLogEventFromReports evt" + 
            " WHERE evt." + this.vendor + "IsSpam IS TRUE" + 
            " AND evt.addrKind = 'T'" +
            " AND evt.policyId = :policyId" + 
            " ORDER BY evt.timeStamp DESC";

        // XXX Hack - specify clamphish vendor name here (see ClamPhishScanner)
        if (false == vendor.equals("Clam")) {
            repoDesc = SPAM_REPO_DESC;
        } else {
            repoDesc = CLAM_REPO_DESC;
        }
    }

    // SimpleEventFilter methods ----------------------------------------------

    public RepositoryDesc getRepositoryDesc()
    {
        return repoDesc;
    }

    @SuppressWarnings("unchecked") //Query
    public void doGetEvents(Session s, List<MailLogEventFromReports> l,
                            int limit, Map<String, Object> params) {
        Query q = s.createQuery(logQuery);
        for (String param : q.getNamedParameters()) {
            Object o = params.get(param);
            if (null != o)
                q.setParameter(param, o);
        }
        
        q.setMaxResults(limit);

        int c = 0;
        for (Iterator<MailLogEventFromReports> i = q.iterate(); i.hasNext() && c < limit; ) {
            MailLogEventFromReports evt = i.next();
            evt.setVendor(vendor);
            evt.setSender(MailLogEventFromReports.getSenderForMsg(s, evt.getMsgId()));

            Hibernate.initialize(evt);

            l.add(evt);
            c++;
        }
    }
}
