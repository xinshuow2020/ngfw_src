/*
 * Copyright (c) 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */

package com.metavize.tran.http;

import java.net.URI;
import java.nio.ByteBuffer;

import com.metavize.tran.token.Token;

/**
 * The in-memory token passed through the pipeline.
 *
 * @author <a href="mailto:amread@metavize.com">Aaron Read</a>
 * @version 1.0
 */
public class RequestLineToken implements Token
{
    private RequestLine requestLine;
    private String httpVersion;

    public RequestLineToken(RequestLine requestLine, String httpVersion)
    {
        this.requestLine = requestLine;
        this.httpVersion = httpVersion;
    }

    public RequestLine getRequestLine()
    {
        return requestLine;
    }

    public void setRequestLine(RequestLine requestLine)
    {
        this.requestLine = requestLine;
    }

    public HttpMethod getMethod()
    {
        return requestLine.getMethod();
    }

    public void setMethod(HttpMethod httpMethod)
    {
        requestLine.setMethod(httpMethod);
    }

    public URI getRequestUri()
    {
        return requestLine.getRequestUri();
    }

    public void setRequestUri(URI uri)
    {
        requestLine.setRequestUri(uri);
    }

    public String getHttpVersion()
    {
        return httpVersion;
    }

    public void setHttpVersion()
    {
        this.httpVersion = httpVersion;
    }

    // Token methods ----------------------------------------------------------

    public ByteBuffer getBytes()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getMethod()).append(" ").append(getRequestUri().toString())
            .append(" ").append(httpVersion).append("\r\n");
        byte[] buf = sb.toString().getBytes();

        return ByteBuffer.wrap(buf);
    }
}
