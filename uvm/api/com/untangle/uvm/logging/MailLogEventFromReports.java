/**
 * $Id: MailLogEventFromReports.java,v 1.00 2012/01/07 17:26:38 dmorris Exp $
 */
package com.untangle.uvm.logging;

import java.net.InetAddress;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.Type;

import com.untangle.uvm.logging.SyslogBuilder;
import com.untangle.uvm.logging.SyslogPriority;

/**
 * Log event from the denormalized reports.n_mail_addrs table
 *
 * @author Sebastien Delafond
 * @version 1.0
 */
@Entity
@org.hibernate.annotations.Entity(mutable=false)
@Table(name="n_mail_addrs", schema="reports")
@SuppressWarnings("serial")
public class MailLogEventFromReports extends LogEvent
{
    private Long sessionId;
    private Integer clientIntf;
    private Integer serverIntf;
    private InetAddress cClientAddr;
    private InetAddress sClientAddr;
    private InetAddress cServerAddr;
    private InetAddress sServerAddr;
    private Integer cClientPort;
    private Integer sClientPort;
    private Integer cServerPort;
    private Integer sServerPort;
    private Long policyId;
    private Boolean policyInbound;
    private String uid;
    private Long msgId;
    private String subject;
    private String sender;
    private String serverType;
    private Integer addrPos;
    private String addr;
    private String addrName;
    private String addrKind;
    private Long msgBytes;
    private Integer msgAttachments;
    private String hname;
    private Boolean virusClamClean;
    private String virusClamName;
    private Float saScore;
    private Boolean saIsSpam;
    private String saAction;
    private Float ctScore;
    private Boolean ctIsSpam;
    private String ctAction;
    private Boolean virusKasperskyClean;
    private String virusKasperskyName;
    private Boolean virusCommtouchClean;
    private String virusCommtouchName;
    private Float phishScore;
    private Boolean phishIsSpam;
    private String phishAction;
    private String vendor;

    // constructors -----------------------------------------------------------

    protected MailLogEventFromReports() { }

    // accessors --------------------------------------------------------------
    @Column(name="session_id")
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    @Column(name="client_intf")
    public Integer getClientIntf() { return clientIntf; }
    public void setClientIntf(Integer clientIntf) { this.clientIntf = clientIntf; }

    @Column(name="server_intf")
    public Integer getServerIntf() { return serverIntf; }
    public void setServerIntf(Integer serverIntf) { this.serverIntf = serverIntf; }

    @Column(name="c_client_addr")
    @Type(type="com.untangle.uvm.type.InetAddressUserType")
    public InetAddress getCClientAddr() { return cClientAddr; }
    public void setCClientAddr(InetAddress cClientAddr) { this.cClientAddr = cClientAddr; }

    @Column(name="s_client_addr")
    @Type(type="com.untangle.uvm.type.InetAddressUserType")
    public InetAddress getSClientAddr() { return sClientAddr; }
    public void setSClientAddr(InetAddress sClientAddr) { this.sClientAddr = sClientAddr; }

    @Column(name="c_server_addr")
    @Type(type="com.untangle.uvm.type.InetAddressUserType")
    public InetAddress getCServerAddr() { return cServerAddr; }
    public void setCServerAddr(InetAddress cServerAddr) { this.cServerAddr = cServerAddr; }

    @Column(name="s_server_addr")
    @Type(type="com.untangle.uvm.type.InetAddressUserType")
    public InetAddress getSServerAddr() { return sServerAddr; }
    public void setSServerAddr(InetAddress sServerAddr) { this.sServerAddr = sServerAddr; }

    @Column(name="c_client_port")
    public Integer getCClientPort() { return cClientPort; }
    public void setCClientPort(Integer cClientPort) { this.cClientPort = cClientPort; }

    @Column(name="s_client_port")
    public Integer getSClientPort() { return sClientPort; }
    public void setSClientPort(Integer sClientPort) { this.sClientPort = sClientPort; }

    @Column(name="c_server_port")
    public Integer getCServerPort() { return cServerPort; }
    public void setCServerPort(Integer cServerPort) { this.cServerPort = cServerPort; }

    @Column(name="s_server_port")
    public Integer getSServerPort() { return sServerPort; }
    public void setSServerPort(Integer sServerPort) { this.sServerPort = sServerPort; }

    @Column(name="policy_id")
    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    @Column(name="uid")
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    @Column(name="msg_id")
    public Long getMsgId() { return msgId; }
    public void setMsgId(Long msgId) { this.msgId = msgId; }

    @Column(name="subject")
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    @Column(name="sender")
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    @Column(name="server_type")
    public String getServerType() { return serverType; }
    public void setServerType(String serverType) { this.serverType = serverType; }

    @Column(name="addr_pos")
    public Integer getAddrPos() { return addrPos; }
    public void setAddrPos(Integer addrPos) { this.addrPos = addrPos; }

    @Column(name="addr")
    public String getAddr() { return addr; }
    public void setAddr(String addr) { this.addr = addr; }

    @Column(name="addr_name")
    public String getAddrName() { return addrName; }
    public void setAddrName(String addrName) { this.addrName = addrName; }

    @Column(name="addr_kind")
    public String getAddrKind() { return addrKind; }
    public void setAddrKind(String addrKind) { this.addrKind = addrKind; }

    @Column(name="msg_bytes")
    public Long getMsgBytes() { return msgBytes; }
    public void setMsgBytes(Long msgBytes) { this.msgBytes = msgBytes; }

    @Column(name="msg_attachments")
    public Integer getMsgAttachments() { return msgAttachments; }
    public void setMsgAttachments(Integer msgAttachments) { this.msgAttachments = msgAttachments; }

    @Column(name="hname")
    public String getHname() { return hname; }
    public void setHname(String hname) { this.hname = hname; }

    @Column(name="virus_clam_clean")
    public Boolean getVirusClamClean() { return virusClamClean; }
    public void setVirusClamClean(Boolean virusClamClean) { this.virusClamClean = virusClamClean; }

    @Column(name="virus_clam_name")
    public String getVirusClamName() { return virusClamName; }
    public void setVirusClamName(String virusClamName) { this.virusClamName = virusClamName; }

    @Column(name="sa_score")
    public Float getSaScore() { return saScore; }
    public void setSaScore(Float saScore) { this.saScore = saScore; }

    @Column(name="sa_is_spam")
    public Boolean getSaIsSpam() { return saIsSpam; }
    public void setSaIsSpam(Boolean saIsSpam) { this.saIsSpam = saIsSpam; }

    @Column(name="sa_action")
    public String getSaAction() { return saAction; }
    public void setSaAction(String saAction) { this.saAction = saAction; }

    @Column(name="ct_score")
    public Float getCtScore() { return ctScore; }
    public void setCtScore(Float ctScore) { this.ctScore = ctScore; }

    @Column(name="ct_is_spam")
    public Boolean getCtIsSpam() { return ctIsSpam; }
    public void setCtIsSpam(Boolean ctIsSpam) { this.ctIsSpam = ctIsSpam; }

    @Column(name="ct_action")
    public String getCtAction() { return ctAction; }
    public void setCtAction(String ctAction) { this.ctAction = ctAction; }

    @Column(name="virus_kaspersky_clean")
    public Boolean getVirusKasperskyClean() { return virusKasperskyClean; }
    public void setVirusKasperskyClean(Boolean virusKasperskyClean) { this.virusKasperskyClean = virusKasperskyClean; }

    @Column(name="virus_kaspersky_name")
    public String getVirusKasperskyName() { return virusKasperskyName; }
    public void setVirusKasperskyName(String virusKasperskyName) { this.virusKasperskyName = virusKasperskyName; }

    @Column(name="virus_commtouch_clean")
    public Boolean getVirusCommtouchClean() { return virusCommtouchClean; }
    public void setVirusCommtouchClean(Boolean virusCommtouchClean) { this.virusCommtouchClean = virusCommtouchClean; }

    @Column(name="virus_commtouch_name")
    public String getVirusCommtouchName() { return virusCommtouchName; }
    public void setVirusCommtouchName(String virusCommtouchName) { this.virusCommtouchName = virusCommtouchName; }

    @Column(name="phish_score")
    public Float getPhishScore() { return phishScore; }
    public void setPhishScore(Float phishScore) { this.phishScore = phishScore; }

    @Column(name="phish_is_spam")
    public Boolean getPhishIsSpam() { return phishIsSpam; }
    public void setPhishIsSpam(Boolean phishIsSpam) { this.phishIsSpam = phishIsSpam; }

    @Column(name="phish_action")
    public String getPhishAction() { return phishAction; }
    public void setPhishAction(String phishAction) { this.phishAction = phishAction; }

    @Column(name="vendor")
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public void appendSyslog(SyslogBuilder sb) // FIXME: not called for now
    {
    }

    @Transient
    public String getSyslogId()
    {
        return ""; // FIMXE ?
    }

    @Transient
    public SyslogPriority getSyslogPriority()
    {
        // FIXME
        return SyslogPriority.INFORMATIONAL;
    }
}