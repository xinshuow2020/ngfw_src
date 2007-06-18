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

package com.untangle.node.mail.papi;
import static com.untangle.node.util.Ascii.*;
import static com.untangle.node.util.BufferUtil.*;
import static com.untangle.node.util.ASCIIUtil.*;
import java.nio.*;

import com.untangle.node.mime.*;
import org.apache.log4j.Logger;


/**
 * Specialized little class to process
 * ByteBuffers which contain RFC821
 * Messages (MIME).
 * <br>
 * Instances are stateful and
 * not threadsafe.
 */
public class MessageBoundaryScanner {

    //CR
    private final byte[] CR_BA = new byte[] {CR};
    private final byte[] CRLF =CRLF_BA;
    private final byte[] CRLF_DOT = new byte[] {
        (byte) CR, (byte) LF, (byte) DOT
    };
    private final byte[] CRLF_DOT_CR = new byte[] {
        (byte) CR, (byte) LF, (byte) DOT, (byte) CR
    };
    private final byte[] CRLF_DOT_CRLF = new byte[] {
        (byte) CR, (byte) LF, (byte) DOT, (byte) CR, (byte) LF
    };
    private final byte[] CRLF_DOT_DOT = new byte[] {
        (byte) CR, (byte) LF, (byte) DOT, (byte) DOT
    };
    private final byte[] CRLF_DOT_DOT_CR = new byte[] {
        (byte) CR, (byte) LF, (byte) DOT, (byte) DOT, (byte) CR
    };
    private final byte[] CRLF_DOT_DOT_CRLF = new byte[] {
        (byte) CR, (byte) LF, (byte) DOT, (byte) DOT, (byte) CR, (byte) LF
    };
    private final byte[] CRLF_CR = new byte[] {
        (byte) CR, (byte) LF, (byte) CR
    };
    private final byte[] CRLF_CRLF = new byte[] {
        (byte) CR, (byte) LF, (byte) CR, (byte) LF
    };

    public enum ScanningState {
        INIT,
        LOOKING_FOR_HEADERS_END,
        INIT_BODY,
        LOOKING_FOR_BODY_END,
        DONE
    };

    private final Logger m_logger = Logger.getLogger(MessageBoundaryScanner.class);

    private boolean m_headersBlank = false;
    private boolean m_isEmptyMessage = false;
    private ScanningState m_state = ScanningState.INIT;

    public MessageBoundaryScanner() {}

    /**
     * Reset this Object for reuse
     */
    public void reset() {
        m_headersBlank = false;
        m_isEmptyMessage = false;
        m_state = ScanningState.INIT;
    }

    /**
     * Get current state (see the enum on this class).
     */
    public ScanningState getState() {
        return m_state;
    }

    /**
     * Returns true if the headers were blank (the
     * message started with "CRLF").  This method
     * should only be called after the state
     * has progressed beyond INIT
     */
    public boolean isHeadersBlank() {
        return m_headersBlank;
    }

    /**
     * Returns true if the entire message was a
     * "CRLF.CRLF".  This method
     * should only be called after the state
     * has progressed beyond INIT
     */
    public boolean isEmptyMessage() {
        return m_isEmptyMessage;
    }

    /**
     * Process the headers, returning true
     * when the end of the headers has been found (a CR
     * on a line by itself).  The terminating CR
     * <b>is consumed</b> (i.e. the buffer is advanced
     * past the terminating CR).
     * <br>
     * There are three boundary cases to be detected.
     * <br>
     * The first case involves the {@link #isEmptyMessage empty message}
     * It happens when the message starts with "CRLF.CRLF". In such a case,
     * the state will be advanced to DONE.
     * <br>The second case is blank headers.  This is when
     * the message starts with "CRLFX" where "X" is not
     * the start of "CRLF.CRLF".
     * <br>
     * The third case is an opening line which does
     * not have a colon.  We'll treat this as part
     * of the body, declaring the headers blank.
     * <br>
     * When this method concludes, it may leave bytes in the
     * buffer which are intended to be passed-back on the next call
     * at the head.
     *
     * @param buf the buffer to scan
     * @param maxHeaderLineSz the max header line size
     *
     * @return true if end of headers encountered
     *
     */
    public boolean processHeaders(ByteBuffer buf,
                                  int maxHeaderLineSz) {

        //Note there is a special case we must
        //guard against.  A mail body
        //with only a CRLF.CRLF (i.e. no headers
        //or body) is used in pipelining as a
        //retroactive RSET.
        //
        //We can only detect this if we have at least 5
        //bytes, so make sure we have at least that many.

        //m_logger.info("scanning state (header): " + m_state + ", buf: " + bbToString(buf) + ", " + buf);

        if(m_state == ScanningState.INIT) {
            //Special case.  We cannot know what is going on
            //without 5 bytes, to catch the blank-message
            //case
            if(buf.remaining() < 5) {
                return false;
            }
            //We're guaranteed at least 5 bytes

            //Check for the "blank" message case
            if(
               (buf.get(buf.position()) == CR) &&
               (buf.get(buf.position()+1) == LF) &&
               (buf.get(buf.position()+2) == DOT) &&
               (buf.get(buf.position()+3) == CR) &&
               (buf.get(buf.position()+4) == LF)) {

                m_isEmptyMessage = true;
                m_headersBlank = true;
                m_state = ScanningState.DONE;
                //Advance buffer past the blank stuff
                buf.position(buf.position() + 5);
                return true;
            }
            m_isEmptyMessage = false;

            //Check for the no-headers case
            if(
               (buf.get(buf.position()) == CR) &&
               (buf.get(buf.position()+1) == LF)) {

                //Blank headers
                buf.position(buf.position() + 2);
                m_state = ScanningState.INIT_BODY;
                m_headersBlank = true;
                return true;
            }

            //Check for a first line which isn't a header,
            //and should be treated as body
            int crlfIndex = findCrLf(buf);
            if(crlfIndex > 0) {//Cannot be equal to zero, as wel aready tested for this
                boolean foundColon = false;
                ByteBuffer dup = buf.duplicate();
                while(dup.hasRemaining()) {
                    if(dup.get() == COLON) {
                        foundColon = true;
                        break;
                    }
                }
                //If we found a line yet not a colon,
                //headers are blank
                if(!foundColon) {
                    m_headersBlank = true;
                    m_state = ScanningState.INIT_BODY;
                    return true;
                }
                else {
                    //Fall through
                }
            }
            else {
                //Still cannot determine if this is a valid
                //header line, or part of the body from a
                //crap message
                if(buf.remaining() > maxHeaderLineSz) {
                    m_logger.warn("Exceeded max candidate header line length of \"" +
                                  maxHeaderLineSz + "\".  Assume no headers");
                    m_headersBlank = true;
                    m_state = ScanningState.INIT_BODY;
                    return true;
                }
                return false;
            }

            m_state = ScanningState.LOOKING_FOR_HEADERS_END;
        }

        // handle amread's special syntactically incorrect message
        // - last header field/value does not end with <CRLF>
        //   and DATA section is immediately terminated by <CRLF.CRLF>
        //   or
        //   last header field/value ends with <CRLF>
        //   and DATA section is only terminated by <.CRLF>
        if(buf.remaining() == 5) {
            if((buf.get(buf.position()) == CR) &&
               (buf.get(buf.position()+1) == LF) &&
               (buf.get(buf.position()+2) == DOT) &&
               (buf.get(buf.position()+3) == CR) &&
               (buf.get(buf.position()+4) == LF)) {

                m_state = ScanningState.INIT_BODY;
                //Advance buffer past 1st CRLF pair
                // - last header field/value ends with <CRLF>
                // - processBody will handle the rest of buffer <.CRLF>
                //   as a case of a blank message body
                buf.position(buf.position() + 2);
                return true;
            }
        }

        //Scan for the end of headers
        int headersEnd = findPattern(buf, CRLF_CRLF, 0, CRLF_CRLF.length);
        int newPosition =
            headersEnd>0?
            headersEnd + 4:
            buf.limit();

        if(headersEnd >= 0) {
            //Found the end of the headers
            buf.position(headersEnd + 4);
            m_headersBlank = false;
            m_state = ScanningState.INIT_BODY;
            return true;
        }
        else {
            //Figure out if we should hold-back some bytes
            int headerTermBackset = searchForStart(buf, CRLF_CRLF);
            int msgEndBackset = searchForStart(buf, CRLF_DOT_CRLF);
            buf.position(buf.limit() - Math.max(headerTermBackset, msgEndBackset));
            return false;
        }
    }

    /**
     * Process a body chunk, moving bytes from
     * source to sink.  Look for lines to
     * "dot escape" as well as the terminator.
     * <br><br>
     * If this method returns false, data may be
     * left in the buffer.  This is a simplification,
     * in case the last few bytes were candidates
     * for a significant sequence (please use
     * some magic to cause them to come back to this object
     * on the next read).
     * <br><br>
     * A return of true indicates that we found the
     * end.  If true is returned, the source is positioned
     * just after the "CRLF.CRLF" sequence.
     * Sink should be as big as the source, although
     * it may be underfilled by "dot escaped"
     * lines or the terminator.
     * <br><br>
     * The sink is <b>not</b>flipped when this method returns.
     * <br><br>
     * <b>Do not call this method again
     * after the end was found (i.e. when
     * state is DONE).</b>
     *
     *
     * @param source the source
     * @param sink the sink
     *
     * @return true if end of body encountered.
     */
    public boolean processBody(ByteBuffer source,
                               ByteBuffer sink) {

        //m_logger.info("scanning state (body): " + m_state + ", source: " + bbToString(source) + ", " + source);

        if(m_state == ScanningState.DONE) {
            return true;
        }

        //Special case.  If the body is blank, then
        //the CRLF which terminated the headers
        //may also start the body termination.
        //For this case, we look for ".CRLF"
        if(m_state == ScanningState.INIT_BODY) {
            //Not enough bytes to determine if this is the end
            if(source.remaining() < 3) {
                return false;
            }
            //Check for no body case, knowing what we
            //last saw was the CRLF terminating the headers
            if((source.get(source.position()) == DOT) &&
               (source.get(source.position()+1) == CR) &&
               (source.get(source.position()+2) == LF)) {

                m_isEmptyMessage = true;
                source.position(source.position()+3);
                m_state = ScanningState.DONE;
                return true;
            }

            m_state = ScanningState.LOOKING_FOR_BODY_END;
        }

        //Scan for lines we need to escape
        int index = findPattern(source,
                                CRLF_DOT_DOT,
                                0,
                                CRLF_DOT_DOT.length);

        while(index >= 0 && source.hasRemaining()) {
            //Copy before the ".." (if there is any)
            if(source.position() < index) {
                ByteBuffer dup = source.duplicate();
                dup.position(source.position());
                dup.limit(index);
                sink.put(dup);
            }
            //Write "CRLF."
            sink.put(CRLF_DOT);

            //Position just after the "CRLF.."
            source.position(index + CRLF_DOT_DOT.length);
            index = findPattern(source,
                                CRLF_DOT_DOT,
                                0,
                                CRLF_DOT_DOT.length);
        }

        //No more CRLF..s.  Look for end of message
        if(source.hasRemaining()) {//BEGIN More Bytes after dot escape scanning
            //Look for "CRLF.CRLF"
            index = findPattern(source,
                                CRLF_DOT_CRLF,
                                0,
                                CRLF_DOT_CRLF.length);
            if(index != -1) {
                //Copy bytes without CRLF.CRLF to sink
                if(source.position() < index) {
                    ByteBuffer dup = source.duplicate();
                    dup.position(source.position());
                    dup.limit(index);
                    sink.put(dup);
                }

                //Position source just after CRLF.CRLF
                source.position(index + CRLF_DOT_CRLF.length);
                m_state = ScanningState.DONE;
                return true;
            }
            else {//BEGIN Copy remaining Bytes
                //Amount *not* to copy
                //to sink and reserve for next
                //read
                //Check for out four end patterns
                //"CR", "CRLF", "CRLF.", "CRLF.CR"
                //There should not be any "CRLF.."
                //If they are found, cause them to *stay* in the buffer
                int holdBackMsgEnd = searchForStart(source, CRLF_DOT_CRLF);

                //Copy remaining bytes, holding back
                //what may be the start of
                source.limit(source.limit() - holdBackMsgEnd);
                sink.put(source);
                source.limit(source.limit() + holdBackMsgEnd);
            }//ENDOF Copy remaining Bytes
        }//ENDOF More Bytes after dot escape scanning
        return false;
    }

    /**
     * Searches the tail of the buffer to see if
     * it could have been the start of the
     * given pattern.
     *
     * Returns the number of bytes from
     * the tail which could be the start of the
     * pattern.
     */
    private static final int searchForStart(ByteBuffer buf,
                                            byte[] bytes) {
        if(!buf.hasRemaining()) {
            return 0;
        }
        int startScan =
            buf.remaining() > bytes.length?
            bytes.length:
            buf.remaining();

        for(int i = startScan; i>0; i--) {
            boolean found = true;
            for(int j = 0; j<i; j++) {
                if(buf.get(buf.limit() - i + j) != bytes[j]) {
                    found = false;
                }
            }
            if(found) {
                return i;
            }
        }
        return 0;
    }
    /*
      public static void main(String[] args)
      throws Exception {

      ByteBuffer b1 = null;
      ByteBuffer b2 = null;
      ByteBuffer b3 = null;
      ByteBuffer b4 = null;
      ByteBuffer b5 = null;

      b1 = ByteBuffer.wrap("\r\n..X".getBytes());
      System.out.println("Expect true: " + findPattern(b1, "\r\n..".getBytes(), 0, 4));


      b1 = ByteBuffer.wrap("abcd".getBytes());
      System.out.println("Expect 2, " + searchForStart(b1, "cd".getBytes()));

      b1 = ByteBuffer.wrap("abcd".getBytes());
      System.out.println("Expect 0, " + searchForStart(b1, "xy".getBytes()));

      b1 = ByteBuffer.wrap("abxycd".getBytes());
      System.out.println("Expect 0, " + searchForStart(b1, "xy".getBytes()));

      b1 = ByteBuffer.wrap("abcd".getBytes());
      System.out.println("Expect 1, " + searchForStart(b1, "dx".getBytes()));

      b1 = ByteBuffer.wrap("abc".getBytes());
      System.out.println("Expect 3, " + searchForStart(b1, "abcdefg".getBytes()));

      b1 = ByteBuffer.wrap("efg".getBytes());
      System.out.println("Expect 0, " + searchForStart(b1, "xyzpdq".getBytes()));


      //Good case 1 chunk
      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n.\r\n".getBytes());
      test("Good, all in one", b1);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\n".getBytes());
      b2 = ByteBuffer.wrap("Body\r\nbody2\r\n.\r\n".getBytes());
      test("Headers, Body", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n".getBytes());
      b2 = ByteBuffer.wrap("\r\nBody\r\nbody2\r\n.\r\n".getBytes());
      test("HeadersCRLF, CRLFBody", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r".getBytes());
      b2 = ByteBuffer.wrap("\nBody\r\nbody2\r\n.\r\n".getBytes());
      test("HeadersCRLFCR, LFBody", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r".getBytes());
      b2 = ByteBuffer.wrap("\n\r\nBody\r\nbody2\r\n.\r\n".getBytes());
      test("HeadersCR, LFCRLFBody", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo".getBytes());
      b2 = ByteBuffer.wrap("\r\n\r\nBody\r\nbody2\r\n.\r\n".getBytes());
      test("Headers, CRLFCRLFBody", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody".getBytes());
      b2 = ByteBuffer.wrap("2\r\n.\r\n".getBytes());
      test("HeadersBody, BodyCRLF.CRLF", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2".getBytes());
      b2 = ByteBuffer.wrap("\r\n.\r\n".getBytes());
      test("HeadersBody, CRLF.CRLF", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r".getBytes());
      b2 = ByteBuffer.wrap("\n.\r\n".getBytes());
      test("HeadersBodyCR, LF.CRLF", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n".getBytes());
      b2 = ByteBuffer.wrap(".\r\n".getBytes());
      test("HeadersBodyCRLF, .CRLF", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n.".getBytes());
      b2 = ByteBuffer.wrap("\r\n".getBytes());
      test("HeadersBodyCRLF., CRLF", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n.\r".getBytes());
      b2 = ByteBuffer.wrap("\n".getBytes());
      test("HeadersBodyCRLF.CR, LF", b1, b2);

      //Remainder
      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n.\r\nRCPT TO:<foo@moo>\r\n".getBytes());
      test("Good, all in one", b1);

      //No headers case
      b1 = ByteBuffer.wrap("\r\nBody\r\nbody2\r\n.\r\n".getBytes());
      test("No Headers", b1);

      //no body case
      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\n.\r\n".getBytes());
      test("No Body", b1);

      //blank message case
      b1 = ByteBuffer.wrap("\r\n.\r\n".getBytes());
      test("Blank Msg", b1);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n..\r\n\r\n..foo\r\n.\r\n".getBytes());
      test("All in one, dot escaping", b1);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r".getBytes());
      b2 = ByteBuffer.wrap("\n..\r\n\r\n..foo\r\n.\r\n".getBytes());
      test("HeaderBodyCR, LF..CRLFBody", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n".getBytes());
      b2 = ByteBuffer.wrap("..\r\n\r\n..foo\r\n.\r\n".getBytes());
      test("HeaderBodyCRLF, ..CRLFBody", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n.".getBytes());
      b2 = ByteBuffer.wrap(".\r\n\r\n..foo\r\n.\r\n".getBytes());
      test("HeaderBodyCRLF., .CRLFBody", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n..".getBytes());
      b2 = ByteBuffer.wrap("\r\n\r\n..foo\r\n.\r\n".getBytes());
      test("HeaderBodyCRLF., .CRLFBody", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r".getBytes());
      b2 = ByteBuffer.wrap("\n..XX\r\n..foo\r\n.\r\n".getBytes());
      test("HeaderBodyCR, LF..XXBody", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n".getBytes());
      b2 = ByteBuffer.wrap("..XX\r\n..foo\r\n.\r\n".getBytes());
      test("HeaderBodyCRLF, ..XXBody", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n.".getBytes());
      b2 = ByteBuffer.wrap(".XX\r\n..foo\r\n.\r\n".getBytes());
      test("HeaderBodyCRLF., .XXBody", b1, b2);

      b1 = ByteBuffer.wrap("FOO: moo\r\nDOO: goo\r\n\r\nBody\r\nbody2\r\n..".getBytes());
      b2 = ByteBuffer.wrap("XX\r\n..foo\r\n.\r\n".getBytes());
      test("HeaderBodyCRLF.., XXBody", b1, b2);

      b1 = ByteBuffer.wrap("BODY\r\n.\r\n".getBytes());
      test("No Headers (bleed into body)", b1);

      try {
      byte[] bytes = new byte[1026];
      for(int i = 0; i<bytes.length; i++) {
      bytes[i] = (byte) 'X';
      }
      test("Line too long", ByteBuffer.wrap(bytes));
      System.out.println("\n\n***************\n***ERROR*** Expected exception \n************\n\n");
      }
      catch(Exception ex) {
      System.out.println("Got exception (as expected)");
      }
      }


      private static void test(String desc, ByteBuffer... bufs)
      throws Exception {
      System.out.println("\n\n\n==============================================\nBEGIN TEST " + desc);

      for(int i = 0; i<bufs.length; i++) {
      printBuffer(bufs[i], "Source " + i);
      }
      System.out.println("Begin calling our scanner");
      MessageBoundaryScanner scanner = new MessageBoundaryScanner();

      for(int i = 0; i<bufs.length; i++) {
      System.out.println("Iterate on buffer " + i);
      switch(scanner.getState()) {
      case INIT:
      case LOOKING_FOR_HEADERS_END:
      if(scanner.processHeaders(bufs[i], 1024)) {
      System.out.println("Consumed Header on buffer: " + i);
      if(bufs[i].hasRemaining()) {
      i--;
      System.out.println("Decrement i (" + i + ") to revisit this buffer");
      break;
      }
      }
      else {
      System.out.println("Not enough bytes in buffer: " + i + " for header");
      if(i+1 >= bufs.length) {
      System.out.println("\n\n*************\n***ERROR*** Wants more bytes " +
      "(there are not any)\n*************\n");
      return;
      }
      if(bufs[i].hasRemaining()) {
      int rem = bufs[i].remaining();
      ByteBuffer newBuf = ByteBuffer.allocate(rem + bufs[i+1].remaining());
      newBuf.put(bufs[i]);
      newBuf.put(bufs[i+1]);
      bufs[i+1] = newBuf;
      bufs[i+1].flip();
      System.out.println("Moved remainder (" + rem + ") into next buffer");
      }
      }
      break;
      case INIT_BODY:
      case LOOKING_FOR_BODY_END:
      ByteBuffer sink = ByteBuffer.allocate(bufs[i].remaining());
      if(scanner.processBody(bufs[i], sink)) {
      sink.flip();
      System.out.println("Read end of message on buffer: " + i);
      printBuffer(sink, "(last) Body Chunk");
      printBuffer(bufs[i], "Remaining");
      }
      else {
      sink.flip();
      System.out.println("Not enough bytes in buffer: " + i + " for body");
      printBuffer(sink, "BodyChunk");
      printBuffer(bufs[i], "Remaining");
      if(i+1 >= bufs.length) {
      System.out.println("\n\n*************\n***ERROR*** Wants more bytes " +
      "(there are not any)\n*************\n");
      return;
      }
      if(bufs[i].hasRemaining()) {
      int rem = bufs[i].remaining();
      ByteBuffer newBuf = ByteBuffer.allocate(rem + bufs[i+1].remaining());
      newBuf.put(bufs[i]);
      newBuf.put(bufs[i+1]);
      bufs[i+1] = newBuf;
      bufs[i+1].flip();
      System.out.println("Moved remainder (" + rem + ") into next buffer");
      }
      }
      break;
      case DONE:
      System.out.println("\n\n*************\n***ERROR*** Should not hit DONE state " +
      "\n*************\n");
      break;
      }
      }

      System.out.println("Summary:  MsgEmpty? " +
      scanner.isEmptyMessage() + ", headers empty? " + scanner.isHeadersBlank());
      }

      private static void printBuffer(ByteBuffer pBuf,
      String txt) {

      int bufLen = pBuf.remaining();
      ByteBuffer buf = pBuf.duplicate();
      ByteBuffer dup = buf.duplicate();
      boolean nonASCII = false;
      while(dup.hasRemaining()) {
      byte b = dup.get();
      if(!isPrintableASCII(b)) {
      nonASCII = true;
      break;
      }
      }
      StringBuilder sb = new StringBuilder();
      if(nonASCII) {
      while(buf.hasRemaining()) {
      byte b = buf.get();
      if(isPrintableASCII(b)) {
      sb.append("\"" + (char) b + "\"");
      }
      else {
      sb.append("UNPRINT: " + (int) b);
      }
      sb.append('\n');
      }
      }
      else {
      while(buf.hasRemaining()) {
      byte b = buf.get();
      if(b == CR) {
      if(buf.hasRemaining()) {
      if(buf.get(buf.position()) == LF) {
      buf.get();
      }
      }
      sb.append((char) LF);
      }
      else {
      sb.append((char) b);
      }
      }
      }
      System.out.println("\t\t----BEGIN " + txt + " (" + bufLen + " bytes)-----");
      System.out.println(sb.toString());
      System.out.println("\t\t----ENDOF " + txt + "-----");
      }
      private static boolean isPrintableASCII(byte b) {
      return (b > 31 && b < 127) ||
      (b == CR || b == LF || b == HTAB);
      }
    */
}
