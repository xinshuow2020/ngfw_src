/*
 * Copyright (c) 2003, 2004, 2005, 2006 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */

package com.metavize.mvvm;

import java.io.File;
import java.io.IOException;

import com.metavize.mvvm.addrbook.AddressBook;
import com.metavize.mvvm.api.RemoteIntfManager;
import com.metavize.mvvm.api.RemoteShieldManager;
import com.metavize.mvvm.localapi.LocalIntfManager;
import com.metavize.mvvm.localapi.LocalShieldManager;
import com.metavize.mvvm.logging.EventLogger;
import com.metavize.mvvm.logging.LoggingManager;
import com.metavize.mvvm.logging.SyslogManager;
import com.metavize.mvvm.networking.LocalNetworkManager;
import com.metavize.mvvm.policy.PolicyManager;
import com.metavize.mvvm.portal.LocalPortalManager;
import com.metavize.mvvm.security.AdminManager;
import com.metavize.mvvm.tapi.MPipeManager;
import com.metavize.mvvm.tapi.PipelineFoundry;
import com.metavize.mvvm.toolbox.ToolboxManager;
import com.metavize.mvvm.tran.LocalTransformManager;
import com.metavize.mvvm.util.TransactionWork;

/**
 * Provides an interface to get all local MVVM components from an MVVM
 * instance.  This interface is accessible locally.
 *
 * @author <a href="mailto:amread@metavize.com">Aaron Read</a>
 * @version 1.0
 */
public interface MvvmLocalContext
{
    /**
     * Gets the current state of the MVVM
     *
     * @return a <code>MvvmState</code> enumerated value
     */
    MvvmState state();

    /**
     * Get the <code>ToolboxManager</code> singleton.
     *
     * @return a <code>ToolboxManager</code> value
     */
    ToolboxManager toolboxManager();

    /**
     * Get the <code>TransformManager</code> singleton.
     *
     * @return a <code>TransformManager</code> value
     */
    LocalTransformManager transformManager();

    /**
     * Get the <code>LoggingManager</code> singleton.
     *
     * @return a <code>LoggingManager</code> value
     */
    LoggingManager loggingManager();

    SyslogManager syslogManager();

    /**
     * Get the <code>PolicyManager</code> singleton.
     *
     * @return a <code>PolicyManager</code> value
     */
    PolicyManager policyManager();

    /**
     * Get the <code>AdminManager</code> singleton.
     *
     * @return a <code>AdminManager</code> value
     */
    AdminManager adminManager();

    /**
     * Get the <code>PortalManager</code> singleton.
     *
     * @return a <code>PortalManager</code> value
     */
    LocalPortalManager portalManager();

    ArgonManager argonManager();

    LocalIntfManager localIntfManager();

    // XXX has stuff for local use, should probably be renamed w/o 'Impl'
    LocalNetworkManager networkManager();

    /** Get the <code>LocalShieldManager</code> singleton.
         *
     * @return the ShieldManager.
     */
    LocalShieldManager localShieldManager();

    ReportingManager reportingManager();

    ConnectivityTester getConnectivityTester();

    MailSender mailSender();

    /**
     * Get the AppServerManager singleton for this instance
     *
     * @return the singleton
     */
    LocalAppServerManager appServerManager();

    /**
     * Get the AddressBook singleton for this instance
     *
     * @return the singleton
     */
    AddressBook appAddressBook();

    /**
     * Save settings to local hard drive.
     *
     * @exception IOException if the save was unsuccessful.
     */
    void localBackup() throws IOException;

    /**
     * Save settings to USB key drive.
     *
     * @exception IOException if the save was unsuccessful.
     */
    void usbBackup() throws IOException;

    Process exec(String cmd) throws IOException;
    Process exec(String[] cmd) throws IOException;
    Process exec(String[] cmd, String[] envp) throws IOException;
    Process exec(String[] cmd, String[] envp, File dir) throws IOException;

    void shutdown();

    /**
     * Reboots the EdgeGuard box as if the right button menu was used and confirmed.
     * Note that this currently will not reboot a non-production (dev) box; this
     * behavior may change in the future.  XXX
     *
     */
    void rebootBox();

    // debugging / performance management
    void doFullGC();

    // making sure the client and mvvm versions are the same
    String version();

    /**
     * Get the <code>MPipeManager</code> singleton.
     *
     * @return a <code>MPipeManager</code> value
     */
    MPipeManager mPipeManager();

    /**
     * The pipeline compiler.
     *
     * @return a <code>PipelineFoundry</code> value
     */
    PipelineFoundry pipelineFoundry();

    /**
     * Returns true if the product has been activated, false otherwise
     *
     * @return a <code>boolean</code> value
     */
    boolean isActivated();

    /**
     * Activates the EdgeGuard using the given key.  Returns true if
     * the activation succeeds, false otherwise (if the key is bogus).
     *
     * @param key a <code>String</code> giving the key to be activated
     * under
     * @return a <code>boolean</code> true if the activation succeeded
     */
    boolean activate(String key);

    boolean runTransaction(TransactionWork tw);

    Thread newThread(Runnable runnable);

    EventLogger eventLogger();

    void waitForStartup();

    CronJob makeCronJob(Period p, Runnable r);

    /**
     * Get the activation key.  <b>Don't be naughty and use this</b>
     *
     * @return the activation key.
     */
    String getActivationKey();

    /**
     * Create a backup which the client can save to a local
     * disk.  The returned bytes are for a .tar.gz file, so it is a good
     * idea to either use a ".tar.gz" extension or ".metavizebk" extension
     * so basic validation can be performed for {@link #restore restore).
     *
     * @return the byte[] contents of the backup.
     *
     * @exception IOException if something goes wrong (a lot can go wrong,
     *            but it is nothing the user did to cause this).
     */
    byte[] createBackup() throws IOException;


    /**
     * Restore from a previous {@link #createBackup backup}.
     *
     *
     * @exception IOException something went wrong to prevent the
     *            restore (not the user's fault).
     *
     * @exception IllegalArgumentException if the provided bytes do not seem
     *            to have come from a valid backup (is the user's fault).
     */
    void restoreBackup(byte[] backupFileBytes)
      throws IOException, IllegalArgumentException;

    /*
     * Loads a shared library (.so) into the MVVM classloader.  This is so a transform
     * dosen't load it into its own, which doesn't work right.
     */
    void loadLibrary(String libname);
}
