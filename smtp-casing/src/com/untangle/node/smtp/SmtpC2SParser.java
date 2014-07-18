/**
 * $Id$
 */
package com.untangle.node.smtp;

import static com.untangle.node.util.BufferUtil.findCrLf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;

import org.apache.log4j.Logger;

import com.untangle.node.smtp.mime.HeaderNames;
import com.untangle.node.smtp.mime.MIMEAccumulator;
import com.untangle.node.smtp.mime.MIMEUtil;
import com.untangle.node.token.Chunk;
import com.untangle.node.token.ParseException;
import com.untangle.node.token.ParseResult;
import com.untangle.node.token.PassThruToken;
import com.untangle.node.token.Token;
import com.untangle.node.util.ASCIIUtil;
import com.untangle.uvm.UvmContextFactory;
import com.untangle.uvm.vnet.NodeTCPSession;

/**
 * SMTP client parser
 */
class SmtpC2SParser extends SmtpParser
{
    private static final String CLIENT_PARSER_STATE_KEY = "SMTP-client-parser-state";

    private static final Logger logger = Logger.getLogger(SmtpC2SParser.class);

    private static final int MAX_COMMAND_LINE_SZ = 1024 * 2;

    private enum SmtpClientState { COMMAND, BODY, HEADERS };

    private class SmtpC2SParserSessionState
    {
        protected SmtpClientState currentState = SmtpClientState.COMMAND;
        protected ScannerAndAccumulator sac;
    }

    public SmtpC2SParser()
    {
        super( true );
    }

    @Override
    public void handleNewSession( NodeTCPSession session )
    {
        SmtpC2SParserSessionState state = new SmtpC2SParserSessionState();
        session.attach( CLIENT_PARSER_STATE_KEY, state );

        SmtpSharedState clientSideSharedState = new SmtpSharedState();
        session.attach( SHARED_STATE_KEY, clientSideSharedState );

        lineBuffering( session, false );
    }

    @Override
    @SuppressWarnings("fallthrough")
    protected ParseResult doParse( NodeTCPSession session, ByteBuffer buf ) throws FatalMailParseException
    {
        SmtpC2SParserSessionState state = (SmtpC2SParserSessionState) session.attachment( CLIENT_PARSER_STATE_KEY );
        SmtpSharedState clientSideSharedState = (SmtpSharedState) session.attachment( SHARED_STATE_KEY );

        // ===============================================
        // In general, there are a lot of helper functions called which return true/false. 
        // Most of these operate on the ScannerAndAccumulator data member. If false
        // is returned from these methods, this method performs cleanup and enters passthru mode.
        //

        List<Token> toks = new LinkedList<Token>();
        boolean done = false;

        while (!done && buf.hasRemaining()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Draining tokens from buffer (" + toks.size() + " tokens so far)");
            }

            // Re-check passthru, in case we hit it while looping in
            // this method.
            if ( isPassthru( session ) ) {
                if (buf.hasRemaining()) {
                    toks.add(new Chunk(buf));
                }
                return new ParseResult(toks);
            }

            switch ( state.currentState ) {

                // ==================================================
            case COMMAND:

                if ( clientSideSharedState.isInSASLLogin() ) {
                    logger.debug("In SASL Exchange");
                    SmtpSASLObserver observer = clientSideSharedState.getSASLObserver();
                    ByteBuffer dup = buf.duplicate();
                    switch (observer.clientData(buf)) {
                    case EXCHANGE_COMPLETE:
                        logger.debug("SASL Exchange complete");
                        clientSideSharedState.closeSASLExchange();
                        // fallthrough ?? XXX
                    case IN_PROGRESS:
                        // There should not be any extra bytes left with "in progress"
                        dup.limit(buf.position());
                        toks.add(new SASLExchangeToken(dup));
                        break;
                    case RECOMMEND_PASSTHRU:
                        logger.debug("Entering passthru on advice of SASLObserver");
                        declarePassthru( session );
                        toks.add( PassThruToken.PASSTHRU );
                        toks.add( new Chunk( dup.slice() ) );
                        buf.position( buf.limit() );
                        return new ParseResult(toks);
                    }
                    break;
                }

                if (findCrLf(buf) >= 0) {// BEGIN Complete Command
                    // Parse the next command. If there is a parse error, pass along the original chunk
                    ByteBuffer dup = buf.duplicate();
                    Command cmd = null;
                    try {
                        cmd = CommandParser.parse(buf);
                    } catch (ParseException pe) {
                        // Duplicate the bad buffer
                        dup.limit(findCrLf(dup) + 2);
                        ByteBuffer badBuf = ByteBuffer.allocate(dup.remaining());
                        badBuf.put(dup);
                        badBuf.flip();
                        // Position the "real" buffer beyond the bad point.
                        buf.position(dup.position());

                        logger.warn("Exception parsing command line \"" + ASCIIUtil.bbToString(badBuf)
                                    + "\".  Pass to server and monitor response", pe);

                        cmd = new UnparsableCommand(badBuf);

                        clientSideSharedState.commandReceived(cmd, new CommandParseErrorResponseCallback( session, badBuf ));

                        toks.add(cmd);
                        break;
                    }

                    // If we're here, we have a legitimate command
                    toks.add(cmd);

                    if (cmd.getType() == CommandType.AUTH) {
                        logger.debug("Received an AUTH command (hiding details for privacy reasons)");
                        AUTHCommand authCmd = (AUTHCommand) cmd;
                        String mechName = authCmd.getMechanismName();
                        if ( ! clientSideSharedState.openSASLExchange(mechName) ) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Unable to find SASLObserver for \"" + mechName + "\"");
                            }
                            declarePassthru( session );
                            toks.add( PassThruToken.PASSTHRU );
                            toks.add(  new Chunk( buf ) );
                            return new ParseResult(toks, null);
                        } else {
                            logger.debug("Opening SASL Exchange");
                        }

                        switch ( clientSideSharedState.getSASLObserver().initialClientResponse(authCmd.getInitialResponse()) ) {
                        case EXCHANGE_COMPLETE:
                            logger.debug("SASL Exchange complete");
                            clientSideSharedState.closeSASLExchange();
                            break;
                        case IN_PROGRESS:
                            break;// Nothing interesting to do
                        case RECOMMEND_PASSTHRU:
                            logger.debug("Entering passthru on advice of SASLObserver");
                            declarePassthru( session );
                            toks.add(PassThruToken.PASSTHRU);
                            toks.add(new Chunk(buf));
                            return new ParseResult(toks);
                        }
                        break;
                    } else {
                        // This is broken off so we don't put folks
                        // passwords into the log
                        if (logger.isDebugEnabled()) {
                            logger.debug("Received command: " + cmd.toDebugString());
                        }
                    }

                    if (cmd.getType() == CommandType.STARTTLS) {
                        logger.debug("Enqueue observer for response to STARTTLS, " + "to go into passthru if accepted");
                        clientSideSharedState.commandReceived( cmd, new TLSResponseCallback( session ) );
                    } else if (cmd.getType() == CommandType.DATA) {
                        logger.debug("entering data transmission (DATA)");
                        if ( ! openSAC( session ) ) {
                            // Error opening the temp file. The
                            // error has been reported and the temp file
                            // cleaned-up
                            logger.debug("Declare passthru as we cannot buffer MIME");
                            declarePassthru( session );
                            toks.add(PassThruToken.PASSTHRU);
                            toks.add(new Chunk(buf));
                            return new ParseResult(toks, null);
                        }
                        logger.debug("Change state to " + SmtpClientState.HEADERS
                                     + ".  Enqueue response handler in case DATA "
                                     + "command rejected (returning us to " + SmtpClientState.COMMAND + ")");
                        clientSideSharedState.commandReceived(cmd, new DATAResponseCallback( session, state.sac ));
                        state.currentState = SmtpClientState.HEADERS;
                        // Go back and start evaluating the header bytes.
                    } else {
                        clientSideSharedState.commandReceived(cmd);
                    }
                }// ENDOF Complete Command
                else {// BEGIN Not complete Command
                    // Check for attack
                    if (buf.remaining() > MAX_COMMAND_LINE_SZ) {
                        logger.debug("Line longer than " + MAX_COMMAND_LINE_SZ + " received without new line. "
                                     + "Assume tunneling (permitted) and declare passthru");
                        declarePassthru( session );
                        toks.add(PassThruToken.PASSTHRU);
                        toks.add(new Chunk(buf));
                        return new ParseResult(toks, null);
                    }
                    logger.debug("Command line does not end with CRLF.  Need more bytes");
                    done = true;
                }// ENDOF Not complete Command
                break;

                // ==================================================
            case HEADERS:
                // Duplicate the buffer, in case we have a problem
                ByteBuffer dup = buf.duplicate();
                boolean endOfHeaders = state.sac.scanner.processHeaders(buf, 1024 * 4);

                // If we're here, we didn't get a line which was too long.
                // Write what we have to disk.
                ByteBuffer dup2 = dup.duplicate();
                dup2.limit(buf.position());

                if ( state.sac.scanner.isHeadersBlank() ) {
                    logger.debug("Headers are blank");
                } else {
                    logger.debug("About to write the " + (endOfHeaders ? "last" : "next") + " "
                                 + dup2.remaining() + " header bytes to disk");
                }

                if (! state.sac.accumulator.addHeaderBytes(dup2, endOfHeaders) ) {
                    logger.error("Unable to write header bytes to disk.  Enter passthru");
                    puntDuringHeaders( session, toks, dup );
                    return new ParseResult(toks, null);
                }

                if (endOfHeaders) {// BEGIN End of Headers
                    InternetHeaders headers = state.sac.accumulator.parseHeaders();
                    if (headers == null) {// BEGIN Header PArse Error
                        logger.error("Unable to parse headers.  This will be caught downstream");
                    }// ENDOF Header PArse Error

                    logger.debug("Adding the BeginMIMEToken");
                    clientSideSharedState.beginMsgTransmission();
                    toks.add(new BeginMIMEToken( state.sac.accumulator, createMessageInfo( session, headers )) );
                    state.sac.noLongerAccumulatorMaster();
                    state.currentState = SmtpClientState.BODY;
                    if ( state.sac.scanner.isEmptyMessage() ) {
                        logger.debug("Message blank.  Skip to reading commands");
                        toks.add( new ContinuedMIMEToken( state.sac.accumulator.createChunk(null, true) ) );
                        state.currentState = SmtpClientState.COMMAND;
                        state.sac = null;
                    }

                }// ENDOF End of Headers
                else {
                    logger.debug("Need more header bytes");
                    done = true;
                }
                break;

                // ==================================================
            case BODY:
                ByteBuffer bodyBuf = ByteBuffer.allocate(buf.remaining());
                boolean bodyEnd = state.sac.scanner.processBody(buf, bodyBuf);
                bodyBuf.flip();
                MIMEAccumulator.MIMEChunk mimeChunk = null;
                if (bodyEnd) {
                    logger.debug("Found end of body");
                    mimeChunk = state.sac.accumulator.createChunk(bodyBuf, true);
                    logger.debug("Adding last MIME token with length: " + mimeChunk.getData().remaining());
                    state.sac = null;
                    state.currentState = SmtpClientState.COMMAND;
                } else {
                    mimeChunk = state.sac.accumulator.createChunk(bodyBuf, false);
                    logger.debug("Adding continued MIME token with length: " + mimeChunk.getData().remaining());
                    done = true;
                }
                toks.add(new ContinuedMIMEToken(mimeChunk));
                break;
            }
        }

        // Compact the buffer
        buf = compactIfNotEmpty(buf, MAX_COMMAND_LINE_SZ);

        if (buf == null) {
            logger.debug("returning ParseResult with " + toks.size() + " tokens and a null buffer");
        } else {
            logger.debug("returning ParseResult with " + toks.size() + " tokens and a buffer with " + buf.remaining()
                         + " remaining (" + buf.position() + " to be seen on next invocation)");
        }
        return new ParseResult(toks, buf);
    }

    @Override
    public void handleFinalized( NodeTCPSession session )
    {
        SmtpC2SParserSessionState state = (SmtpC2SParserSessionState) session.attachment( CLIENT_PARSER_STATE_KEY );

        super.handleFinalized( session );

        if ( state.sac != null ) {
            logger.debug("Unexpected finalized in state " + state.currentState);
            state.sac.accumulator.dispose();
            state.sac = null;
        }
    }

    /**
     * Callback if TLS starts
     */
    private void tlsStarting( NodeTCPSession session )
    {
        logger.debug("TLS Command accepted.  Enter passthru mode so as to not attempt to parse cyphertext");
        declarePassthru( session );// Inform the unparser of this state
    }

    // ================ Inner Class =================

    /**
     * Callback registered with the SmtpSharedState for the response to the DATA command
     */
    class DATAResponseCallback implements SmtpSharedState.ResponseAction
    {
        private NodeTCPSession session;
        private ScannerAndAccumulator targetSAC;

        public DATAResponseCallback( NodeTCPSession session, ScannerAndAccumulator sac)
        {
            this.session = session;
            this.targetSAC = sac;
        }

        public void response(int code)
        {
            SmtpC2SParserSessionState state = (SmtpC2SParserSessionState) session.attachment( CLIENT_PARSER_STATE_KEY );

            if (code < 400) {
                logger.debug("DATA command accepted");
            } else {
                logger.debug("DATA command rejected");
                if ( state.sac != null && targetSAC == state.sac && state.sac.isMasterOfAccumulator() ) {

                    state.sac.accumulator.dispose();
                    state.sac = null;
                    state.currentState = SmtpClientState.COMMAND;
                } else {
                    logger.debug("DATA command rejected, yet we have moved on to a new transaction");
                }
            }
        }
    }

    /**
     * Callback registered with the SmtpSharedState for the response to the STARTTLS command
     */
    class TLSResponseCallback implements SmtpSharedState.ResponseAction
    {
        private NodeTCPSession session;

        protected TLSResponseCallback( NodeTCPSession session )
        {
            this.session = session;
        }

        public void response( int code )
        {
            if (code < 300) {
                tlsStarting( session );
            } else {
                logger.debug("STARTTLS command rejected.  Do not go into passthru");
            }
        }
    }

    /**
     * Callback registered with the SmtpSharedState for the response to a command we could not parse. If the
     * response can be parsed, and it is an error, we do not go into passthru. If the response is positive, then we go
     * into passthru.
     */
    class CommandParseErrorResponseCallback implements SmtpSharedState.ResponseAction
    {
        private NodeTCPSession session;
        private String offendingCommand;

        CommandParseErrorResponseCallback( NodeTCPSession session, ByteBuffer bufWithOffendingLine )
        {
            this.session = session;
            offendingCommand = ASCIIUtil.bbToString(bufWithOffendingLine);
        }

        public void response(int code)
        {
            if (code < 300) {
                logger.error("Could not parse command line \"" + offendingCommand
                             + "\" yet accepted by server.  Parser error.  Enter passthru");
                declarePassthru( session );
            } else {
                logger.debug("Command \"" + offendingCommand + "\" unparsable, and rejected "
                             + "by server.  Do not enter passthru (assume errant client)");
            }
        }
    }

    /**
     * Open the MIMEAccumulator and Scanner (ScannerAndAccumulator). If there was an error, the ScannerAndAccumulator is
     * not set as a data member and any files/streams are cleaned-up.
     * 
     * @return false if there was an error creating the file.
     */
    private boolean openSAC( NodeTCPSession session )
    {
        SmtpC2SParserSessionState state = (SmtpC2SParserSessionState) session.attachment( CLIENT_PARSER_STATE_KEY );

        try {
            state.sac = new ScannerAndAccumulator( new MIMEAccumulator( session ) );
            return true;
        } catch (IOException ex) {
            logger.error("Exception creating MIME Accumulator", ex);
            return false;
        }
    }

    private boolean addRecipientsFromHeader(String[] rcpts, MessageInfo ret, AddressKind recipientType)
    {
        boolean hasRecipient = false;
        if (rcpts != null) {
            try {
                for (String addr : rcpts) {
                    InternetAddress[] iaList = InternetAddress.parseHeader(addr, false);
                    for (InternetAddress ia : iaList) {
                        ret.addAddress(recipientType, ia.getAddress(), ia.getPersonal());
                        hasRecipient = true;
                    }
                }
            } catch (Exception e) {
                ret.addAddress(recipientType, "Illegal_address", "");
                logger.error(e);
            }
        }
        return hasRecipient;
    }
    
    /**
     * Helper method to break-out the creation of a MessageInfo
     */
    private MessageInfo createMessageInfo( NodeTCPSession session, InternetHeaders headers )
    {
        SmtpC2SParserSessionState state = (SmtpC2SParserSessionState) session.attachment( CLIENT_PARSER_STATE_KEY );
        SmtpSharedState clientSideSharedState = (SmtpSharedState) session.attachment( SHARED_STATE_KEY );

        if (headers == null) {
            return new MessageInfo(session.sessionEvent(), session.getServerPort(), "");
        }

        MessageInfo ret = new MessageInfo(session.sessionEvent(), session.getServerPort(), "");

        ret.setSubject(headers.getHeader(HeaderNames.SUBJECT, ""));
        // Drain all TO and CC
        String[] toRcpts = headers.getHeader(HeaderNames.TO);
        String[] ccRcpts = headers.getHeader(HeaderNames.CC);

        boolean hasFrom = false;
        boolean hasTo = addRecipientsFromHeader(toRcpts, ret, AddressKind.TO);
        hasTo = hasTo || addRecipientsFromHeader(ccRcpts, ret, AddressKind.CC);

        try {
            // Drain FROM
            String from = headers.getHeader(HeaderNames.FROM, "");
            if (from != null) {
                InternetAddress ia = new InternetAddress(from);
                ret.addAddress(AddressKind.FROM, ia.getAddress(), ia.getPersonal());
                hasFrom = true;
            }
        } catch (Exception e) {
            ret.addAddress(AddressKind.FROM, "Illegal_address", "");
            logger.error(e);
        }
        UvmContextFactory.context().logEvent(ret);

        // Add anyone from the transaction
        SmtpTransaction smtpTx = clientSideSharedState.getCurrentTransaction();
        if (smtpTx == null) {
            logger.error("Transaction tracker returned null for current transaction");
        } else {
            // Transfer the FROM
            if (smtpTx.getFrom() != null && !MIMEUtil.isNullAddress(smtpTx.getFrom())) {
                ret.addAddress(AddressKind.ENVELOPE_FROM, smtpTx.getFrom().getAddress(), smtpTx.getFrom().getPersonal());
                if (!hasFrom) {
                    // needed in order to show up in logs even if the headers do not contain "FROM"
                    ret.addAddress(AddressKind.FROM, smtpTx.getFrom().getAddress(), smtpTx.getFrom().getPersonal());
                }
            }
            List<InternetAddress> txRcpts = smtpTx.getRecipients(false);
            for (InternetAddress addr : txRcpts) {
                if (MIMEUtil.isNullAddress(addr)) {
                    continue;
                }
                ret.addAddress(AddressKind.ENVELOPE_TO, addr.getAddress(), addr.getPersonal());
                if (!hasTo) {
                    // needed in order to show up in logs even if the headers do not contain "TO" or "CC"
                    ret.addAddress(AddressKind.TO, addr.getAddress(), addr.getPersonal());
                }
            }
        }
        return ret;
    }

    /**
     * This code was moved-out of the "parse" method as it was repeated a few times.
     */
    private void puntDuringHeaders( NodeTCPSession session, List<Token> toks, ByteBuffer buf )
    {
        SmtpC2SParserSessionState state = (SmtpC2SParserSessionState) session.attachment( CLIENT_PARSER_STATE_KEY );

        // Get any bytes trapped in the file
        ByteBuffer trapped = state.sac.accumulator.drainFileToByteBuffer();
        if (trapped == null) {
            logger.debug("Could not recover buffered header bytes");
        } else {
            logger.debug("Retreived " + trapped.remaining() + " bytes trapped in file");
        }
        // Nuke the accumulator
        state.sac.accumulator.dispose();
        state.sac = null;
        // Passthru
        declarePassthru( session );
        toks.add( PassThruToken.PASSTHRU );
        if (trapped != null && trapped.remaining() > 0) {
            toks.add(new Chunk(trapped));
        }
        toks.add(new Chunk(buf));
    }

    /**
     * Little class to associate the MIMEAccumulator and the boundary scanner as-one.
     */
    private class ScannerAndAccumulator
    {
        final MessageBoundaryScanner scanner;
        final MIMEAccumulator accumulator;
        private boolean isMasterOfAccumulator = true;

        ScannerAndAccumulator(MIMEAccumulator accumulator)
        {
            scanner = new MessageBoundaryScanner();
            this.accumulator = accumulator;
        }

        boolean isMasterOfAccumulator()
        {
            return isMasterOfAccumulator;
        }

        void noLongerAccumulatorMaster()
        {
            isMasterOfAccumulator = false;
        }
    }
}