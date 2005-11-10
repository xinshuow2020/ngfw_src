/*
 * Copyright (c) 2004, 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */

package com.metavize.tran.httpblocker;

import java.nio.ByteBuffer;

import com.metavize.mvvm.tapi.TCPSession;
import com.metavize.mvvm.tran.Transform;
import com.metavize.tran.http.HttpStateMachine;
import com.metavize.tran.http.RequestLineToken;
import com.metavize.tran.http.StatusLine;
import com.metavize.tran.token.Chunk;
import com.metavize.tran.token.EndMarker;
import com.metavize.tran.token.Header;
import com.metavize.tran.token.Token;
import org.apache.log4j.Logger;

public class HttpBlockerHandler extends HttpStateMachine
{
    private static final int SCAN = Transform.GENERIC_0_COUNTER;
    private static final int BLOCK = Transform.GENERIC_1_COUNTER;
    private static final int PASS = Transform.GENERIC_2_COUNTER;

    private static final Logger logger = Logger
        .getLogger(HttpBlockerHandler.class);

    private final HttpBlockerImpl transform;

    // constructors -----------------------------------------------------------

    HttpBlockerHandler(TCPSession session, HttpBlockerImpl transform)
    {
        super(session);

        this.transform = transform;
    }

    // HttpStateMachine methods -----------------------------------------------

    @Override
    protected RequestLineToken doRequestLine(RequestLineToken requestLine)
    {
        return requestLine;
    }

    @Override
    protected Header doRequestHeader(Header requestHeader)
    {
        logger.debug("in doRequestHeader(): " + requestHeader);
        transform.incrementCount(SCAN, 1);

        String c2sReplacement = transform.getBlacklist()
            .checkRequest(getSession().clientAddr(), getRequestLine(),
                          requestHeader);
        logger.debug("check request returns: " + c2sReplacement);

        if (null == c2sReplacement) {
            releaseRequest();
        } else {
            transform.incrementCount(BLOCK, 1);
            blockRequest(generateResponse(c2sReplacement, isRequestPersistent()));
        }

        return requestHeader;
    }

    @Override
    protected Chunk doRequestBody(Chunk c)
    {
        return c;
    }

    @Override
    protected void doRequestBodyEnd() { }

    @Override
    protected StatusLine doStatusLine(StatusLine statusLine)
    {
        return statusLine;
    }

    @Override
    protected Header doResponseHeader(Header responseHeader)
    {
        if (100 == getStatusLine().getStatusCode()) {
            releaseResponse();
        } else {
            logger.debug("in doResponseHeader: " + responseHeader);

            String s2cReplacement = transform.getBlacklist()
                .checkResponse(getSession().clientAddr(), getResponseRequest(),
                               responseHeader);
            logger.debug("checkResponse returns: " + s2cReplacement);

            if (null == s2cReplacement) {
                transform.incrementCount(PASS, 1);

                releaseResponse();
            } else {
                transform.incrementCount(BLOCK, 1);
                blockResponse(generateResponse(s2cReplacement,
                                               isResponsePersistent()));
            }
        }

        return responseHeader;
    }

    @Override
    protected Chunk doResponseBody(Chunk c)
    {
        return c;
    }

    @Override
    protected void doResponseBodyEnd() { }

    // private methods --------------------------------------------------------

    private Token[] generateResponse(String replacement, boolean persistent)
    {
        Token response[] = new Token[4];

        // XXX make canned responses in constructor
        // XXX Do template replacement
        ByteBuffer buf = ByteBuffer.allocate(replacement.length());
        buf.put(replacement.getBytes()).flip();

        StatusLine sl = new StatusLine("HTTP/1.1", 403, "Forbidden");
        response[0] = sl;

        Header h = new Header();
        h.addField("Content-Length", Integer.toString(buf.remaining()));
        h.addField("Content-Type", "text/html");
        h.addField("Connection", persistent ? "Keep-Alive" : "Close");
        response[1] = h;

        Chunk c = new Chunk(buf);
        response[2] = c;

        response[3] = EndMarker.MARKER;

        return response;
    }
}
