import unittest2
import time
import sys
import pdb
import os
import re
import socket
import subprocess
import base64
from jsonrpc import ServiceProxy
from jsonrpc import JSONRPCException
from uvm import Manager
from uvm import Uvm
import remote_control
import test_registry
import system_properties

uvmContext = Uvm().getUvmContext()
defaultRackId = 1
nodeData = None
node = None
nodeDataAD = None
nodeAD = None
adHost = "10.111.56.48"
radiusHost = "10.111.56.71"
localUserName = 'test20'
adUserName = 'atsadmin'

# pdb.set_trace()

def flushEvents():
    reports = uvmContext.nodeManager().node("untangle-node-reporting")
    if (reports != None):
        reports.flushEvents()

def createCaptureInternalNicRule():
    faceValue = str(remote_control.interface)
    return {
        "capture": True,
        "description": "Test Rule - Capture all internal traffic",
        "enabled": True,
        "id": 1,
        "javaClass": "com.untangle.node.capture.CaptureRule",
        "matchers": {
            "javaClass": "java.util.LinkedList",
            "list": [{
                "invert": False,
                "javaClass": "com.untangle.node.capture.CaptureRuleMatcher",
                "matcherType": "SRC_INTF",
                "value": faceValue
                }]
            },
        "ruleId": 1
    };

def createLocalDirectoryUser():
    return {'javaClass': 'java.util.LinkedList', 
        'list': [{
            'username': localUserName, 
            'firstName': '[firstName]', 
            'lastName': '[lastName]', 
            'javaClass': 'com.untangle.uvm.LocalDirectoryUser', 
            'expirationTime': 0, 
            'passwordBase64Hash': base64.b64encode('passwd'), 
            'email': 'test20@example.com'
            },]
    }

def removeLocalDirectoryUser():
    return {'javaClass': 'java.util.LinkedList', 
        'list': []
    }

def createADSettings():
    # Need to send Radius setting even though it's not used in this case.
    return {
       "activeDirectorySettings": {
            "LDAPHost": adHost,
            "LDAPPort": 389,
            "OUFilter": "",
            "domain": "adtesting.int",
            "enabled": True,
            "javaClass": "com.untangle.node.adconnector.ActiveDirectorySettings",
            "superuser": "ATSadmin",
            "superuserPass": "passwd"},
        "radiusSettings": {
            "port": 1812, 
            "enabled": False, 
            "authenticationMethod": "PAP", 
            "javaClass": "com.untangle.node.adconnector.RadiusSettings", 
            "server": radiusHost, 
            "sharedSecret": "mysharedsecret"}
    }

def createRadiusSettings():
    return {
        "activeDirectorySettings": {
            "enabled": False, 
            "superuserPass": "passwd", 
            "LDAPPort": "389", 
            "OUFilter": "", 
            "domain": "adtest.metaloft.com", 
            "javaClass": "com.untangle.node.adconnector.ActiveDirectorySettings", 
            "LDAPHost": adHost, 
            "superuser": "Administrator"}, 
        "radiusSettings": {
            "port": 1812, 
            "enabled": True, 
            "authenticationMethod": "PAP", 
            "javaClass": "com.untangle.node.adconnector.RadiusSettings", 
            "server": radiusHost, 
            "sharedSecret": "chakas"}
        }

def findNameInHostTable (hostname='test'):
    #  Test for username in session
    foundTestSession = False
    remote_control.runCommand("nohup netcat -d -4 test.untangle.com 80",stdout=False,nowait=True)
    time.sleep(2) # since we launched netcat in background, give it a second to establish connection
    hostList = uvmContext.hostTable().getHosts()
    sessionList = hostList['list']
    # find session generated with netcat in session table.
    for i in range(len(sessionList)):
        print sessionList[i]
        # print "------------------------------"
        if (sessionList[i]['address'] == remote_control.clientIP) and \
            (sessionList[i]['username'] == hostname) and \
            (not sessionList[i]['penaltyBoxed']):
            foundTestSession = True
            break
    remote_control.runCommand("pkill netcat")
    return foundTestSession
    
class CaptureTests(unittest2.TestCase):

    @staticmethod
    def nodeName():
        return "untangle-node-capture"

    @staticmethod
    def nodeNameAD():
        return "untangle-node-adconnector"

    @staticmethod
    def vendorName():
        return "Untangle"

    def setUp(self):
        global nodeData, node, nodeDataRD, nodeDataAD, nodeAD, adResult, radiusResult, test_untangle_com_ip
        if node == None:
            if (uvmContext.nodeManager().isInstantiated(self.nodeName())):
                print "ERROR: Node %s already installed" % self.nodeName()
                raise unittest2.SkipTest('node %s already instantiated' % self.nodeName())
            node = uvmContext.nodeManager().instantiate(self.nodeName(), defaultRackId)
            nodeData = node.getCaptureSettings()
        if nodeAD == None:
            if (uvmContext.nodeManager().isInstantiated(self.nodeNameAD())):
                print "ERROR: Node %s already installed" % self.nodeNameAD()
                raise unittest2.SkipTest('node %s already instantiated' % self.nodeName())
            nodeAD = uvmContext.nodeManager().instantiate(self.nodeNameAD(), defaultRackId)
            nodeDataAD = nodeAD.getSettings().get('activeDirectorySettings')
            nodeDataRD = nodeAD.getSettings().get('radiusSettings')
        adResult = subprocess.call(["ping","-c","1",adHost],stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        radiusResult = subprocess.call(["ping","-c","1",radiusHost],stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        # Get the IP address of test.untangle.com
        test_untangle_com_ip = socket.gethostbyname("test.untangle.com")   

        # remove previous temp files
        remote_control.runCommand("rm -f /tmp/capture_test_*")

    def test_010_clientIsOnline(self):
        result = remote_control.isOnline()
        assert (result == 0)

    def test_020_defaultTrafficCheck(self):
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_020.log -O /tmp/capture_test_020.out http://test.untangle.com/")
        assert (result == 0)

    def test_021_captureTrafficCheck(self):
        global node, nodeData, captureIP
        nodeData['captureRules']['list'].append(createCaptureInternalNicRule())
        node.setSettings(nodeData)
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_021.log -O /tmp/capture_test_021.out http://test.untangle.com/")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'Captive Portal' /tmp/capture_test_021.out")
        assert (search == 0)
        # get the IP address of the capture page 
        ipfind = remote_control.runCommand("grep 'Location' /tmp/capture_test_021.log",stdout=True)
        # print 'ipFind %s' % ipfind
        ip = re.findall( r'[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}(?:[0-9:]{0,6})', ipfind )
        captureIP = ip[0]
        print 'Capture IP address is %s' % captureIP

        # check event log
        flushEvents()
        query = None;
        for q in node.getRuleEventQueries():
            if q['name'] == 'All Events': query = q;
        assert(query != None)
        events = uvmContext.getEvents(query['query'],defaultRackId,100)
        assert(events != None)
        found = remote_control.check_events( events.get('list'), 5,
                                            'c_server_addr', test_untangle_com_ip,
                                            'c_client_addr', remote_control.clientIP,
                                            'capture_blocked', True )
        assert( found )

    def test_023_captureAnonymousLogin(self):
        global node, nodeData

        # Create Internal NIC capture rule with basic login page
        nodeData['authenticationType']="NONE"
        nodeData['pageType'] = "BASIC_MESSAGE"
        node.setSettings(nodeData)

        # check that basic captive page is shown
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_023.log -O /tmp/capture_test_023.out http://test.untangle.com/")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'Captive Portal' /tmp/capture_test_023.out")
        assert (search == 0)

        # Verify anonymous works
        appid = str(node.getNodeSettings()["id"])
        print 'appid is %s' % appid  # debug line
        result = remote_control.runCommand("wget -a /tmp/capture_test_023a.log -O /tmp/capture_test_023a.out  \'http://" + captureIP + "/capture/handler.py/infopost?method=GET&nonce=9abd7f2eb5ecd82b&method=GET&appid=" + appid + "&agree=agree&submit=Continue&host=test.untangle.com&uri=/\'")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'Hi!' /tmp/capture_test_023a.out")
        assert (search == 0)
        
        # logout user to clean up test.
        # wget http://<internal IP>/capture/logout  
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_023b.log -O /tmp/capture_test_023b.out http://" + captureIP + "/capture/logout")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'logged out' /tmp/capture_test_023b.out")
        assert (search == 0)

    def test_024_captureAnonymousLoginTimeout(self):
        global node, nodeData
        if remote_control.quickTestsOnly:
            raise unittest2.SkipTest('Skipping a time consuming test')

        # Create Internal NIC capture rule with basic login page
        nodeData['authenticationType']="NONE"
        nodeData['pageType'] = "BASIC_MESSAGE"
        nodeData['userTimeout'] = 120
        node.setSettings(nodeData)

        # check that basic captive page is shown
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_024.log -O /tmp/capture_test_024.out http://test.untangle.com/")
        assert (result == 0)

        # Verify anonymous works
        appid = str(node.getNodeSettings()["id"])
        print 'appid is %s' % appid  # debug line
        result = remote_control.runCommand("wget -a /tmp/capture_test_024a.log -O /tmp/capture_test_024a.out  \'http://" + captureIP + "/capture/handler.py/infopost?method=GET&nonce=9abd7f2eb5ecd82b&method=GET&appid=" + appid + "&agree=agree&submit=Continue&host=test.untangle.com&uri=/\'")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'Hi!' /tmp/capture_test_024a.out")
        assert (search == 0)
        
        # Wait for captive timeout
        time.sleep(180)
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_024b.log -O /tmp/capture_test_024b.out http://test.untangle.com/")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'Captive Portal' /tmp/capture_test_024b.out")
        assert (search == 0)

    def test_025_captureAnonymousLoginHTTPS(self):
        global node, nodeData

        # Create Internal NIC capture rule with basic login page
        nodeData['authenticationType']="NONE"
        nodeData['pageType'] = "BASIC_MESSAGE"
        nodeData['userTimeout'] = 3600  # back to default setting
        node.setSettings(nodeData)

        # check that basic captive page is shown
        result = remote_control.runCommand("curl -s --connect-timeout 2 -L -o /tmp/capture_test_025.out --insecure https://test.untangle.com/")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'Captive Portal' /tmp/capture_test_025.out")
        assert (search == 0)

        # Verify anonymous works
        appid = str(node.getNodeSettings()["id"])
        print 'appid is %s' % appid  # debug line
        result = remote_control.runCommand("curl -s --connect-timeout 2 -L -o /tmp/capture_test_025a.out --insecure  \'https://" + captureIP + "/capture/handler.py/infopost?method=GET&nonce=9abd7f2eb5ecd82b&method=GET&appid=" + appid + "&agree=agree&submit=Continue&host=test.untangle.com&uri=/\'")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'Hi!' /tmp/capture_test_025a.out")
        assert (search == 0)
        
        # logout user to clean up test.
        # wget http://<internal IP>/capture/logout  
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_025b.log -O /tmp/capture_test_025b.out http://" + captureIP + "/capture/logout")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'logged out' /tmp/capture_test_025b.out")
        assert (search == 0)

    def test_030_captureLocalDirLogin(self):
        global node, nodeData
        # Create local directory user 'test20'
        uvmContext.localDirectory().setUsers(createLocalDirectoryUser())
        # results = uvmContext.localDirectory().getUsers()
        # print results

        # Create Internal NIC capture rule with basic login page
        nodeData['authenticationType']="LOCAL_DIRECTORY"
        nodeData['pageType'] = "BASIC_LOGIN"
        node.setSettings(nodeData)

        # check that basic captive page is shown
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_030.log -O /tmp/capture_test_030.out http://test.untangle.com/")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'username and password' /tmp/capture_test_030.out")
        assert (search == 0)

        # check if local directory login and password 
        appid = str(node.getNodeSettings()["id"])
        # print 'appid is %s' % appid  # debug line
        result = remote_control.runCommand("wget -a /tmp/capture_test_030a.log -O /tmp/capture_test_030a.out  \'http://" + captureIP + "/capture/handler.py/authpost?username=test20&password=passwd&nonce=9abd7f2eb5ecd82b&method=GET&appid=" + appid + "&host=test.untangle.com&uri=/\'")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'Hi!' /tmp/capture_test_030a.out")
        assert (search == 0)
        foundUsername = findNameInHostTable(localUserName)
        assert(foundUsername)        

        # logout user to clean up test.
        # wget http://<internal IP>/capture/logout  
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_030b.log -O /tmp/capture_test_030b.out http://" + captureIP + "/capture/logout")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'logged out' /tmp/capture_test_030b.out")
        assert (search == 0)
        foundUsername = findNameInHostTable(localUserName)
        assert(not foundUsername)        


    def test_035_captureADLogin(self):
        global nodeData, node, nodeDataAD, nodeAD, captureIP
        if (adResult != 0):
            raise unittest2.SkipTest("No AD server available")
        # Configure AD settings
        testResultString = nodeAD.getActiveDirectoryManager().getActiveDirectoryStatusForSettings(createADSettings())
        # print 'testResultString %s' % testResultString  # debug line
        nodeAD.setSettings(createADSettings())
        assert ("success" in testResultString)
        # Create Internal NIC capture rule with basic AD login page
        nodeData['authenticationType']="ACTIVE_DIRECTORY"
        nodeData['pageType'] = "BASIC_LOGIN"
        node.setSettings(nodeData)

        # check that basic captive page is shown
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_035.log -O /tmp/capture_test_035.out http://test.untangle.com/")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'username and password' /tmp/capture_test_035.out")
        assert (search == 0)

        # check if AD login and password 
        appid = str(node.getNodeSettings()["id"])
        # print 'appid is %s' % appid  # debug line
        result = remote_control.runCommand("wget -a /tmp/capture_test_035a.log -O /tmp/capture_test_035a.out  \'http://" + captureIP + "/capture/handler.py/authpost?username=" + adUserName + "&password=passwd&nonce=9abd7f2eb5ecd82b&method=GET&appid=" + appid + "&host=test.untangle.com&uri=/\'")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'Hi!' /tmp/capture_test_035a.out")
        assert (search == 0)
        foundUsername = findNameInHostTable(adUserName)
        assert(foundUsername)        

        # logout user to clean up test.
        # wget http://<internal IP>/capture/logout  
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_035b.log -O /tmp/capture_test_035b.out http://" + captureIP + "/capture/logout")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'logged out' /tmp/capture_test_035b.out")
        assert (search == 0)
        foundUsername = findNameInHostTable(adUserName)
        assert(not foundUsername)        

        # check extend ascii in login and password 
        result = remote_control.runCommand("wget -a /tmp/capture_test_035c.log -O /tmp/capture_test_035c.out  \'http://" + captureIP + "/capture/handler.py/authpost?username=britishguy&password=passwd%C2%A3&nonce=9abd7f2eb5ecd82b&method=GET&appid=" + appid + "&host=test.untangle.com&uri=/\'")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'Hi!' /tmp/capture_test_035c.out")
        assert (search == 0)

        # logout user to clean up test.
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_035d.log -O /tmp/capture_test_035d.out http://" + captureIP + "/capture/logout")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'logged out' /tmp/capture_test_035d.out")
        assert (search == 0)

    def test_040_captureRadiusLogin(self):
        global nodeData, node, nodeDataRD, nodeDataAD, nodeAD, captureIP
        if (radiusResult != 0):
            raise unittest2.SkipTest("No RADIUS server available")

        # Configure RADIUS settings
        nodeAD.setSettings(createRadiusSettings())
        attempts = 0
        while attempts < 3:
            testResultString = nodeAD.getRadiusManager().getRadiusStatusForSettings(createRadiusSettings(),"normal","passwd")
            if ("success" in testResultString):
                break
            else:
                attempts += 1
        print 'testResultString %s attempts %s' % (testResultString, attempts) # debug line
        assert ("success" in testResultString)
        # Create Internal NIC capture rule with basic AD login page
        nodeData['authenticationType']="RADIUS"
        nodeData['pageType'] = "BASIC_LOGIN"
        node.setSettings(nodeData)

        # check that basic captive page is shown
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_040.log -O /tmp/capture_test_040.out http://test.untangle.com/")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'username and password' /tmp/capture_test_040.out")
        assert (search == 0)

        # check if RADIUS login and password 
        appid = str(node.getNodeSettings()["id"])
        # print 'appid is %s' % appid  # debug line
        result = remote_control.runCommand("wget -a /tmp/capture_test_040a.log -O /tmp/capture_test_040a.out  \'http://" + captureIP + "/capture/handler.py/authpost?username=normal&password=passwd&nonce=9abd7f2eb5ecd82b&method=GET&appid=" + appid + "&host=test.untangle.com&uri=/\'",stdout=True)
        search = remote_control.runCommand("grep -q 'Hi!' /tmp/capture_test_040a.out")
        assert (search == 0)

        # logout user to clean up test.
        # wget http://<internal IP>/capture/logout  
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_040b.log -O /tmp/capture_test_040b.out http://" + captureIP + "/capture/logout")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'logged out' /tmp/capture_test_040b.out")
        assert (search == 0)

        # check if RADIUS login and password a second time.
        appid = str(node.getNodeSettings()["id"])
        # print 'appid is %s' % appid  # debug line
        result = remote_control.runCommand("wget -a /tmp/capture_test_040a.log -O /tmp/capture_test_040a.out  \'http://" + captureIP + "/capture/handler.py/authpost?username=normal&password=passwd&nonce=9abd7f2eb5ecd82b&method=GET&appid=" + appid + "&host=test.untangle.com&uri=/\'",stdout=True)
        search = remote_control.runCommand("grep -q 'Hi!' /tmp/capture_test_040a.out")
        assert (search == 0)

        # logout user to clean up test a second time.
        # wget http://<internal IP>/capture/logout  
        result = remote_control.runCommand("wget -4 -t 2 --timeout=5 -a /tmp/capture_test_040b.log -O /tmp/capture_test_040b.out http://" + captureIP + "/capture/logout")
        assert (result == 0)
        search = remote_control.runCommand("grep -q 'logged out' /tmp/capture_test_040b.out")
        assert (search == 0)

    @staticmethod
    def finalTearDown(self):
        global node, nodeAD
        uvmContext.localDirectory().setUsers(removeLocalDirectoryUser())
        if node != None:
            uvmContext.nodeManager().destroy( node.getNodeSettings()["id"] )
            node = None
        if nodeAD != None:
            uvmContext.nodeManager().destroy( nodeAD.getNodeSettings()["id"] )
            nodeAD = None

test_registry.registerNode("capture", CaptureTests)
