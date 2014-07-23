/**
 * $Id$
 */
package com.untangle.node.smtp;

import static com.untangle.node.smtp.SmtpNodeImpl.PROTOCOL_NAME;

import org.apache.log4j.Logger;

import com.untangle.node.token.AbstractUnparser;
import com.untangle.node.token.Token;
import com.untangle.uvm.UvmContextFactory;
import com.untangle.uvm.vnet.NodeTCPSession;
import com.untangle.uvm.vnet.TCPStreamer;

/**
 * Base class for the SmtpClient/ServerUnparser
 */
abstract class SmtpUnparser extends AbstractUnparser
{
    private final Logger logger = Logger.getLogger(SmtpUnparser.class);

    protected static final String SHARED_STATE_KEY = "SMTP-shared-state";

    protected SmtpUnparser( boolean clientSide )
    {
        super( clientSide );
    }
    
    /**
     * Is the casing currently in passthru mode
     */
    protected boolean isPassthru( NodeTCPSession session )
    {
        SmtpSharedState sharedState = (SmtpSharedState) session.attachment( SHARED_STATE_KEY );
        return sharedState.passthru;
    }

    /**
     * Called by the unparser to declare that we are now in passthru mode. This is called either because of a parsing
     * error by the caller, or the reciept of a passthru token.
     * 
     */
    protected void declarePassthru( NodeTCPSession session)
    {
        SmtpSharedState sharedState = (SmtpSharedState) session.attachment( SHARED_STATE_KEY );
        sharedState.passthru = true;
    }

    @Override
    public void unparse( NodeTCPSession session, Token token )
    {
        if (token instanceof PassThruToken) {
            logger.debug("Received PassThruToken");
            declarePassthru( session );// Inform the parser of this state
            return;
        }
        doUnparse( session, token );
        return;
    }

    /**
     * Delegated "unparse" method. The superclass performs some housekeeping before calling this method. <br>
     * <br>
     * Note that subclasses should <b>not</b> worry about tracing, or receiving Passthru tokens
     */
    protected abstract void doUnparse( NodeTCPSession session, Token token );

    @Override
    public void handleFinalized( NodeTCPSession session )
    {
        logger.debug("(" + PROTOCOL_NAME + ")(" + (isClientSide() ? "client" : "server") + ") handleFinalized");
    }
}
