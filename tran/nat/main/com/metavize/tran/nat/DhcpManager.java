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
package com.metavize.tran.nat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.metavize.mvvm.MvvmContextFactory;
import com.metavize.mvvm.NetworkingConfiguration;
import com.metavize.mvvm.tran.HostName;
import com.metavize.mvvm.tran.HostNameList;
import com.metavize.mvvm.tran.IPNullAddr;
import com.metavize.mvvm.tran.IPaddr;
import com.metavize.mvvm.tran.ParseException;
import com.metavize.mvvm.tran.TransformStartException;
import com.metavize.mvvm.tran.firewall.MACAddress;
import org.apache.log4j.Logger;

class DhcpManager
{
    private static final String COMMENT = "#";
    private static final String HEADER  = COMMENT + " AUTOGENERATED BY METAVIZE DO NOT MODIFY MANUALLY\n\n";

    private static final String FLAG_DHCP_RANGE       = "dhcp-range";
    private static final String FLAG_DHCP_HOST        = "dhcp-host";
    private static final String FLAG_DHCP_OPTION      = "dhcp-option";
    private static final String FLAG_DNS_LOCAL_DOMAIN = "domain";

    private static final String FLAG_DHCP_GATEWAY     = "3";
    private static final String FLAG_DHCP_NETMASK     = "1";
    private static final String FLAG_DHCP_NAMESERVERS = "6";
    private static final String FLAG_DNS_LISTEN       = "listen-address";
    private static final String FLAG_DNS_LISTEN_PORT  = "port";

    private static final String FLAG_DNS_BIND_INTERFACES = "bind-interfaces";
    private static final String FLAG_DNS_INTERFACE       = "interface";

    private static final int DHCP_LEASE_ENTRY_LENGTH  = 5;
    private static final String DHCP_LEASE_DELIM      = " ";

    private static final String DHCP_LEASES_FILE      = "/var/lib/misc/dnsmasq.leases";
    private static final int DHCP_LEASE_ENTRY_EOL     = 0;
    private static final int DHCP_LEASE_ENTRY_MAC     = 1;
    private static final int DHCP_LEASE_ENTRY_IP      = 2;
    private static final int DHCP_LEASE_ENTRY_HOST    = 3;

    private static final String DNS_MASQ_FILE         = "/etc/dnsmasq.conf";
    private static final String DNS_MASQ_CMD          = "/etc/init.d/dnsmasq ";
    private static final String DNS_MASQ_CMD_RESTART  = DNS_MASQ_CMD + " restart";
    private static final String DNS_MASQ_CMD_STOP     = DNS_MASQ_CMD + " stop";
    private static final HostName LOCAL_DOMAIN_DEFAULT;

    private static final String HOST_FILE             = "/etc/hosts";
    private static final String HOST_NAME_FILE        = "/etc/hostname";
    private static final String DEFAULT_HOSTNAME      = "mv-edgeguard";
    private static final String[] HOST_FILE_START     = new String[] {
        HEADER,
        "127.0.0.1  localhost"
    };

    private static final String[] HOST_FILE_END       = new String[] {
        "# The following lines are desirable for IPv6 capable hosts",
        "# (added automatically by netbase upgrade)",
        "",
        "::1     ip6-localhost ip6-loopback",
        "fe00::0 ip6-localnet",
        "ff00::0 ip6-mcastprefix",
        "ff02::1 ip6-allnodes",
        "ff02::2 ip6-allrouters",
        "ff02::3 ip6-allhosts"
    };

    private final Logger logger = Logger.getLogger( DhcpManager.class );
    private final NatImpl transform;

    private final DhcpMonitor dhcpMonitor;

    DhcpManager( NatImpl transform )
    {
        this.transform = transform;
        this.dhcpMonitor = new DhcpMonitor( transform, MvvmContextFactory.context());
    }

    void configure( NatSettings settings, NetworkingConfiguration netConfig ) throws TransformStartException
    {

        try {
            writeConfiguration( settings, netConfig );
            writeHosts( settings );
        } catch ( Exception e ) {
            throw new TransformStartException( "Unable to reload DNS masq configuration", e );
        }

        /* Enable/Disable DHCP forwarding  */
        try {
            if ( settings.getDhcpEnabled()) {
                dhcpMonitor.start();

                MvvmContextFactory.context().argonManager().disableDhcpForwarding();
            } else {
                dhcpMonitor.stop();

                MvvmContextFactory.context().argonManager().enableDhcpForwarding();
            }
        } catch ( Exception e ) {
            throw new TransformStartException( "Error updating DHCP forwarding settings", e );
        }
    }

    void startDnsMasq() throws TransformStartException
    {
        int code;

        try {
            logger.debug( "Restarting DNS Masq server" );

            /* restart dnsmasq */
            Process p = Runtime.getRuntime().exec( DNS_MASQ_CMD_RESTART );
            code = p.waitFor();
        } catch ( Exception e ) {
            throw new TransformStartException( "Unable to reload DNS masq configuration", e );
        }

        if ( code != 0 ) {
            throw new TransformStartException( "Error starting DNS masq server" + code );
        }
    }

    void deconfigure()
    {
        int code;

        try {
            writeDisabledConfiguration();

            Process p = Runtime.getRuntime().exec( DNS_MASQ_CMD_RESTART );
            code = p.waitFor();

            if ( code != 0 ) logger.error( "Error stopping DNS masq server, returned code: " + code );
        } catch ( Exception e ) {
            logger.error( "Error while disabling the DNS masq server", e );
        }

        /* Re-enable DHCP forwarding */
        try {
            logger.info( "Reenabling DHCP forwarding" );
            MvvmContextFactory.context().argonManager().enableDhcpForwarding();
        } catch ( Exception e ) {
            logger.error( "Error enabling DHCP forwarding", e );
        }

        /* Stop the DHCP Monitor */
        dhcpMonitor.stop();
    }

    void loadLeases( NatSettings settings )
    {
        BufferedReader in = null;

        /* Insert the rules from the leases file, than layover the rules from the settings */
        List<DhcpLeaseRule> leaseList  = new LinkedList<DhcpLeaseRule>();
        Map<MACAddress,Integer> macMap = new HashMap<MACAddress,Integer>();

        /* The time right now to determine if leases have been expired */
        Date now = new Date();


        /* Open up the interfaces file */
        try {
            in = new BufferedReader(new FileReader( DHCP_LEASES_FILE ));
            String str;
            while (( str = in.readLine()) != null ) {
                parseLease( str, leaseList, now, macMap );
            }
        } catch ( FileNotFoundException ex ) {
            logger.info( "The file: " + DHCP_LEASES_FILE + " does not exist yet" );
        } catch ( Exception ex ) {
            logger.error( "Error reading file: " + DHCP_LEASES_FILE, ex );
        } finally {
            try {
                if ( in != null )  in.close();
            } catch ( Exception ex ) {
                logger.error( "Unable to close file: " + DHCP_LEASES_FILE, ex );
            }
        }

        /* Lay over the settings from NAT */
        List <DhcpLeaseRule> staticList = settings.getDhcpLeaseList();

        overlayStaticLeases( staticList, leaseList, macMap );

        /* Set the list */
        settings.setDhcpLeaseList( leaseList );
    }

    void parseLease( String str, List<DhcpLeaseRule> leaseList, Date now, Map<MACAddress,Integer> macMap )
    {
        str = str.trim();
        String strArray[] = str.split( DHCP_LEASE_DELIM );
        String tmp, host;
        Date eol;
        MACAddress mac;
        IPNullAddr ip;

        if ( strArray.length != DHCP_LEASE_ENTRY_LENGTH ) {
            logger.error( "Invalid DHCP lease: " + str );
            return;
        }

        tmp  = strArray[DHCP_LEASE_ENTRY_EOL];
        try {
            eol = new Date( Long.parseLong( tmp ) * 1000 );
        } catch ( Exception e ) {
            logger.error( "Invalid DHCP date: " + tmp );
            return;
        }

        if ( eol.before( now )) {
            if (logger.isDebugEnabled()) {
                logger.debug( "Lease already expired: " + str );
            }
            return;
        }

        tmp  = strArray[DHCP_LEASE_ENTRY_MAC];
        try {
            mac = MACAddress.parse( tmp );
        } catch ( Exception e ) {
            logger.error( "Invalid MAC address: " + tmp );
            return;
        }

        tmp  = strArray[DHCP_LEASE_ENTRY_IP];
        try {
            ip = IPNullAddr.parse( tmp );
        } catch ( Exception e ) {
            logger.error( "Invalid IP address: " + tmp );
            return;
        }

        host  = strArray[DHCP_LEASE_ENTRY_HOST];

        /* Insert the lease */
        DhcpLeaseRule rule  = new DhcpLeaseRule( mac, host, ip, IPNullAddr.getNullAddr(), eol, true );

        /* Determine if the rule already exists */
        Integer index = macMap.get( mac );

        if ( index == null ) {
            leaseList.add( rule );
            macMap.put( mac, leaseList.size() - 1 );
        } else {
            /* XXX Right now resolve by MAC is always true */
            leaseList.set( index, rule );
        }
    }

    private void overlayStaticLeases( List<DhcpLeaseRule> staticList, List<DhcpLeaseRule> leaseList,
                                      Map<MACAddress,Integer> macMap )
    {
        if ( staticList == null ) {
            return;
        }

        for ( Iterator<DhcpLeaseRule> iter = staticList.iterator() ; iter.hasNext() ; ) {
            DhcpLeaseRule rule = iter.next();

            MACAddress mac = rule.getMacAddress();
            Integer index = macMap.get( mac );
            if ( index == null ) {
                /* Insert a new rule */
                DhcpLeaseRule currentRule = new DhcpLeaseRule( mac, "", IPNullAddr.getNullAddr(),
                                                               rule.getStaticAddress(), null, true );
                currentRule.setDescription( rule.getDescription());
                currentRule.setCategory( rule.getCategory());

                leaseList.add( currentRule );

                macMap.put( mac, leaseList.size() - 1 );
            } else {
                DhcpLeaseRule currentRule = leaseList.get( index );
                currentRule.setStaticAddress( rule.getStaticAddress());
                currentRule.setResolvedByMac( rule.getResolvedByMac());
                currentRule.setDescription( rule.getDescription());
                currentRule.setCategory( rule.getCategory());
            }
        }
    }

    /* This removes all of the non-static leases */
    void fleeceLeases( NatSettings settings )
    {
        /* Lay over the settings from NAT */
        List <DhcpLeaseRule> staticList = settings.getDhcpLeaseList();

        for ( Iterator<DhcpLeaseRule> iter = staticList.iterator() ; iter.hasNext() ; ) {
            DhcpLeaseRule rule = iter.next();

            IPNullAddr staticAddress = rule.getStaticAddress();

            if ( staticAddress == null || staticAddress.isEmpty()) {
                iter.remove();
            }
        }
    }

    private void writeConfiguration( NatSettings settings, NetworkingConfiguration netConfig )
    {
        StringBuilder sb = new StringBuilder();

        IPaddr externalAddress = netConfig.host();

        sb.append( HEADER );

        if ( settings.getDhcpEnabled()) {
            /* XXX Presently always defaulting lease times to a fixed value */
            comment( sb, "DHCP Range:" );
            sb.append( FLAG_DHCP_RANGE + "=" + settings.getDhcpStartAddress().toString());
            sb.append( "," + settings.getDhcpEndAddress().toString() + ",4h\n\n\n" );


            /* XXXX Could move this outside of the is dhcp enabled, which would bind to the
             * inside interface if using NAT, without DHCP but with DNS forwarding
             */
            /* Bind to the inside interface if using Nat */
            if ( settings.getNatEnabled()) {
                String inside = null;

                try {
                    inside = MvvmContextFactory.context().argonManager().getInside();

                    comment( sb, "Bind to the inside interface" );
                    sb.append( FLAG_DNS_BIND_INTERFACES + "\n" );
                    sb.append( FLAG_DNS_INTERFACE + "=" + inside + "\n\n" );
                } catch( Exception e ) {
                    logger.error( "Error retrieving inside interface, not binding to inside" );
                    inside = null;
                }
            }

            /* Configure all of the hosts */
            List<DhcpLeaseRule> list = (List<DhcpLeaseRule>)settings.getDhcpLeaseList();

            if ( list != null ) {
                for ( Iterator<DhcpLeaseRule> iter = list.iterator() ; iter.hasNext() ; ) {
                    DhcpLeaseRule rule = iter.next();

                    if ( !rule.getStaticAddress().isEmpty()) {
                        comment( sb, "Static DHCP Host" );
                        if ( rule.getResolvedByMac()) {
                            sb.append( FLAG_DHCP_HOST + "=" + rule.getMacAddress().toString());
                            sb.append( "," + rule.getStaticAddress().toString() + ",24h\n\n" );
                        } else {
                            sb.append( FLAG_DHCP_HOST + "=" + rule.getHostname());
                            sb.append( "," + rule.getStaticAddress().toString() + ",24h\n\n" );
                        }
                    }
                }
            }

            IPaddr gateway;
            IPaddr netmask;

            /* If Nat is turned on, use the settings from nat, otherwise use
             * the settings from networking configuration */
            if ( settings.getNatEnabled()) {
                gateway = settings.getNatInternalAddress();
                netmask = settings.getNatInternalSubnet();
            } else {
                gateway = netConfig.gateway();
                netmask  = netConfig.netmask();
            }

            comment( sb, "Setting the gateway" );
            sb.append( FLAG_DHCP_OPTION + "=" + FLAG_DHCP_GATEWAY );
            sb.append( "," + gateway.toString() + "\n\n" );

            comment( sb, "Setting the subnet" );
            sb.append( FLAG_DHCP_OPTION + "=" + FLAG_DHCP_NETMASK );
            sb.append( "," + netmask.toString() + "\n\n" );


            appendNameServers( sb, settings, netConfig );
        } else {
            comment( sb, "DHCP is disabled, not using a range or any host rules\n" );
        }

        if ( !settings.getDnsEnabled()) {
            /* Cannot bind to localhost, because that will also disable DHCP */
            comment( sb, "DNS is disabled, binding to port 54" );
            sb.append( FLAG_DNS_LISTEN_PORT + "=54\n\n" );
        } else {
            HostName localDomain = settings.getDnsLocalDomain();
            /* Write out the localdomain */
            comment( sb, "Setting the Local domain name" );

            if ( localDomain.isEmpty()) {
                comment( sb, "Local domain name is empty, using " + LOCAL_DOMAIN_DEFAULT.toString());
                localDomain = LOCAL_DOMAIN_DEFAULT;
            }

            sb.append( FLAG_DNS_LOCAL_DOMAIN + "=" + localDomain + "\n\n" );
        }

        writeFile( sb, DNS_MASQ_FILE );
    }

    /**
     * Save the file /etc/hosts
     */
    private void writeHosts( NatSettings settings )
    {
        StringBuilder sb = new StringBuilder();

        for ( int c = 0 ; c < HOST_FILE_START.length ; c ++ ) {
            sb.append( HOST_FILE_START[c] + "\n" );
        }

        String hostname = getHostName();
        sb.append( "127.0.0.1\t" + hostname );
        sb.append( "\n" );

        if ( settings.getDnsEnabled()) {
            List<DnsStaticHostRule> hostList = mergeHosts( settings );

            for ( Iterator<DnsStaticHostRule> iter = hostList.iterator(); iter.hasNext() ; ) {
                DnsStaticHostRule rule = iter.next();
                HostNameList hostNameList = rule.getHostNameList();
                if ( hostNameList.isEmpty()) {
                    comment( sb, "Empty host name list for host " + rule.getStaticAddress().toString());
                } else {
                    sb.append( rule.getStaticAddress().toString() + "\t" + hostNameList.toString() + "\n" );
                }
            }
        } else {
            comment( sb, "DNS is disabled, skipping hosts" );
        }

        sb.append( "\n" );

        for ( int c = 0 ; c < HOST_FILE_END.length ; c ++ ) {
            sb.append( HOST_FILE_END[c] + "\n" );
        }

        writeFile( sb, HOST_FILE );
    }

    /* Get the hostname of the box */
    private String getHostName()
    {
        String hostname = DEFAULT_HOSTNAME;

        BufferedReader in = null;

        /* Open up the interfaces file */
        try {
            in = new BufferedReader(new FileReader( HOST_NAME_FILE ));
            String str;
            str = in.readLine().trim();
            /* Try to parse the hostname, throws an exception if it fails */
            HostName.parse( str );
            hostname = str;
        } catch ( Exception ex ) {
            /* Go to the default */
            hostname = DEFAULT_HOSTNAME;
        }

        try {
            if ( in != null ) in.close();
        } catch ( Exception e ) {
            logger.error( "Error closing file: " + e );
        }

        return hostname;
    }

    /**
     * Create a new list will all of entries for the same host in the same list
     */
    private List<DnsStaticHostRule> mergeHosts( NatSettings settings )
    {
        List<DnsStaticHostRule> list = new LinkedList<DnsStaticHostRule>();
        Map<IPaddr,DnsStaticHostRule> map = new HashMap<IPaddr,DnsStaticHostRule>();

        for ( Iterator iter = settings.getDnsStaticHostList().iterator(); iter.hasNext() ; ) {
            DnsStaticHostRule rule = (DnsStaticHostRule)iter.next();
            IPaddr addr  = rule.getStaticAddress();
            DnsStaticHostRule current = map.get( addr );

            if ( current == null ) {
                /* Make a copy of the static route rule */
                current = new DnsStaticHostRule( new HostNameList( rule.getHostNameList()), addr );
                map.put( addr, current );
                list.add( current );
            } else {
                current.getHostNameList().merge( rule.getHostNameList() );
            }
        }

        HostName localDomain = settings.getDnsLocalDomain();
        localDomain = ( localDomain.isEmpty()) ? LOCAL_DOMAIN_DEFAULT : localDomain;

        for ( Iterator<DnsStaticHostRule> iter = list.iterator() ; iter.hasNext() ; ) {
            HostNameList hostNameList = iter.next().getHostNameList();
            hostNameList.qualify( localDomain );
            hostNameList.removeReserved();
        }

        return list;
    }

    /* XXX This should go into a global util class */
    private void writeFile( StringBuilder sb, String fileName )
    {
        BufferedWriter out = null;

        /* Open up the interfaces file */
        try {
            String data = sb.toString();

            out = new BufferedWriter(new FileWriter( fileName ));
            out.write( data, 0, data.length());
        } catch ( Exception ex ) {
            /* XXX May need to catch this exception, restore defaults
             * then try again */
            logger.error( "Error writing file " + fileName + ":", ex );
        }

        try {
            if ( out != null )
                out.close();
        } catch ( Exception ex ) {
            logger.error( "Unable to close file: " + fileName , ex );
        }
    }

    private void appendNameServers( StringBuilder sb, NatSettings settings, NetworkingConfiguration netConfig )
    {
        String nameservers = "";
        IPaddr tmp;

        if ( settings.getDnsEnabled()) {
            if ( settings.getNatEnabled()) {
                nameservers += settings.getNatInternalAddress().toString();
            } else {
                nameservers += netConfig.host().toString();
            }
        } else {

            tmp = netConfig.dns1();

            if ( tmp != null && !tmp.isEmpty()) {
                nameservers += ( nameservers.length() == 0 ) ? "" : ",";
                nameservers += tmp.toString();
            }

            tmp = netConfig.dns2();

            if ( tmp != null && !tmp.isEmpty()) {
                nameservers += ( nameservers.length() == 0 ) ? "" : ",";
                nameservers += tmp.toString();
            }
        }

        if ( nameservers.length() == 0 ) {
            comment( sb, "No nameservers specified\n" );
        } else {
            comment( sb, "Nameservers:" );
            sb.append( FLAG_DHCP_OPTION + "=" + FLAG_DHCP_NAMESERVERS );
            sb.append( "," + nameservers + "\n\n" );
        }
    }

    /* This guarantees the comment appears with a newline at the end */
    private void comment( StringBuilder sb, String comment )
    {
        sb.append( COMMENT + " " + comment + "\n" );
    }

    private void writeDisabledConfiguration()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( HEADER );
        comment( sb, "DNS is disabled, binding DNS to local host" );
        sb.append( FLAG_DNS_LISTEN + "=" + "127.0.0.1\n\n" );

        writeFile( sb, DNS_MASQ_FILE );
    }

    static
    {
        HostName h;

        try {
            h = HostName.parse( "local.domain" );
        } catch ( ParseException e ) {
            /* This should never happen */
            System.err.println( "Unable to initialize LOCAL_DOMAIN_DEFAULT: " + e );
            h = null;
        }
        LOCAL_DOMAIN_DEFAULT = h;
    }
}
