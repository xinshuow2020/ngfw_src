/*
 * Copyright (c) 2003, 2004, 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */
package com.metavize.tran.protofilter;

import java.util.List;

import com.metavize.mvvm.tran.Transform;

public interface ProtoFilter extends Transform
{
    ProtoFilterSettings getProtoFilterSettings();
    void setProtoFilterSettings(ProtoFilterSettings settings);

    List<ProtoFilterLog> getLogs(int limit);

    List<ProtoFilterLog> getLogs(int limit, boolean blockedOnly);
}
