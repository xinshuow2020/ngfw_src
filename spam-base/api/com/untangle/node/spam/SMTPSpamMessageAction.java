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

package com.untangle.node.spam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// XXX convert to enum when we dump XDoclet

public class SMTPSpamMessageAction implements Serializable
{
    private static final long serialVersionUID = -6364692037092527263L;

    private static final Map INSTANCES = new HashMap();

    public static final char PASS_KEY = 'P';
    public static final char MARK_KEY = 'M';
    public static final char BLOCK_KEY = 'B';
    public static final char QUARANTINE_KEY = 'Q';
    public static final char SAFELIST_KEY = 'S'; // special pass case
    public static final char OVERSIZE_KEY = 'Z'; // special pass case

    public static final SMTPSpamMessageAction PASS = new SMTPSpamMessageAction(PASS_KEY, "pass message");
    public static final SMTPSpamMessageAction MARK = new SMTPSpamMessageAction(MARK_KEY, "mark message");
    public static final SMTPSpamMessageAction BLOCK = new SMTPSpamMessageAction(BLOCK_KEY, "block message");
    public static final SMTPSpamMessageAction QUARANTINE = new SMTPSpamMessageAction(QUARANTINE_KEY, "quarantine message");
    public static final SMTPSpamMessageAction SAFELIST = new SMTPSpamMessageAction(SAFELIST_KEY, "safelist message", false);
    public static final SMTPSpamMessageAction OVERSIZE = new SMTPSpamMessageAction(OVERSIZE_KEY, "oversize message", false);

    private String name;
    private char key;
    private boolean uiSelectable;

    private SMTPSpamMessageAction(char key, String name)
    {
        this(key, name, true);
    }

    private SMTPSpamMessageAction(char key, String name, boolean uiSelectable)
    {
        this.key = key;
        this.name = name;
        this.uiSelectable = uiSelectable;
        INSTANCES.put(key, this);
    }

    public static SMTPSpamMessageAction getInstance(char key)
    {
        return (SMTPSpamMessageAction)INSTANCES.get(key);
    }

    public static SMTPSpamMessageAction getInstance(String name)
    {
        SMTPSpamMessageAction zMsgAction;
        for (Iterator i = INSTANCES.keySet().iterator(); true == i.hasNext(); )
            {
                zMsgAction = (SMTPSpamMessageAction)INSTANCES.get(i.next());
                if (name.equals(zMsgAction.getName())) {
                    return zMsgAction;
                }
            }
        return null;
    }

    public String toString()
    {
        return name;
    }

    public char getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    Object readResolve()
    {
        return getInstance(key);
    }

    private static SMTPSpamMessageAction[] arproto = new SMTPSpamMessageAction[0];

    // UI gets selectable values here
    public static SMTPSpamMessageAction[] getValues()
    {
        List azMsgAction = new ArrayList();
        for (Iterator iter = INSTANCES.keySet().iterator(); iter.hasNext();) {
            SMTPSpamMessageAction zMsgAction = (SMTPSpamMessageAction)INSTANCES.get(iter.next());
            if (!zMsgAction.uiSelectable)
                continue;
            azMsgAction.add(zMsgAction);
        }
        return (SMTPSpamMessageAction[]) azMsgAction.toArray(arproto);
    }
}
