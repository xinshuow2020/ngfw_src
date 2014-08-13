import unittest2
import time
import sys
import datetime
import random
import string
from jsonrpc import ServiceProxy
from jsonrpc import JSONRPCException
from uvm import Manager
from uvm import Uvm
from untangle_tests import ClientControl
from untangle_tests import TestDict

uvmContext = Uvm().getUvmContext()
defaultRackId = 1
clientControl = ClientControl()
node = None

def flushEvents():
    reports = uvmContext.nodeManager().node("untangle-node-reporting")
    if (reports != None):
        reports.flushEvents()

def nukepatterns():
    settings = node.getSettings()
    patterns = settings["patterns"]
    patterns["list"] = [];
    settings["patterns"] = patterns
    node.setSettings(settings)

def addPatterns(definition, blocked=False, log=True, protocol="protocol", description="description", category="category"):
    newPatterns = { 
                "alert": False, 
                "blocked": blocked, 
                "category": category, 
                "definition": definition, 
                "description": description, 
                "javaClass": "com.untangle.node.protofilter.ProtoFilterPattern", 
                "log": log, 
                "protocol": protocol, 
                "quality": ""
    }

    settings = node.getSettings()
    patterns = settings["patterns"]
    patterns["list"].append(newPatterns)
    settings["patterns"] = patterns
    node.setSettings(settings)

class ProtofilterTests(unittest2.TestCase):

    @staticmethod
    def nodeName():
        return "untangle-node-protofilter"

    def setUp(self):
        global node
        if node == None:
            if (uvmContext.nodeManager().isInstantiated(self.nodeName())):
                print "ERROR: Node %s already installed" % self.nodeName();
                raise Exception('node %s already instantiated' % self.nodeName())
            node = uvmContext.nodeManager().instantiate(self.nodeName(), defaultRackId)
            node.start() # must be called since ips doesn't auto-start
            flushEvents()

    # verify client is online
    def test_010_clientIsOnline(self):
        result = clientControl.isOnline()
        assert (result == 0)

    def test_020_testHttpPatternLog(self):
        nukepatterns()
        
        addPatterns(definition="http/(0\\.9|1\\.0|1\\.1) [1-5][0-9][0-9] [\\x09-\\x0d -~]*(connection:|content-type:|content-length:|date:)|post [\\x09-\\x0d -~]* http/[01]\\.[019]",
                    protocol="HTTP", 
                    category="Web", 
                    description="HyperText Transfer Protocol")
        result = clientControl.runCommand("wget -q -O /dev/null -t 1 --timeout=3 http://test.untangle.com/")
        nukepatterns()
        assert (result == 0)
        time.sleep(3);
        flushEvents()
        query = None;
        for q in node.getEventQueries():
            if q['name'] == 'All Events': query = q;
        assert(query != None)
        events = uvmContext.getEvents(query['query'],defaultRackId,1)
        assert(events != None)
        found = clientControl.check_events( events.get('list'), 5,
                                            'c_client_addr', ClientControl.hostIP,
                                            'protofilter_protocol', 'HTTP',
                                            'protofilter_blocked', False )
        assert( found )

    def test_030_testHttpPatternBlocked(self):
        nukepatterns()
        
        addPatterns(definition="http/(0\\.9|1\\.0|1\\.1) [1-5][0-9][0-9] [\\x09-\\x0d -~]*(connection:|content-type:|content-length:|date:)|post [\\x09-\\x0d -~]* http/[01]\\.[019]",
                    protocol="HTTP",
                    blocked=True,
                    category="Web", 
                    description="HyperText Transfer Protocol")
        result = clientControl.runCommand("wget -q -O /dev/null -t 1 --timeout=3 http://test.untangle.com/")
        assert (result != 0)
        time.sleep(3);
        flushEvents()
        query = None;
        for q in node.getEventQueries():
            if q['name'] == 'All Events': query = q;
        assert(query != None)
        events = uvmContext.getEvents(query['query'],defaultRackId,1)
        assert(events != None)
        found = clientControl.check_events( events.get('list'), 5,
                                            'c_client_addr', ClientControl.hostIP,
                                            'protofilter_protocol', 'HTTP',
                                            'protofilter_blocked', True )
        assert( found )

    def test_040_testFtpPatternBlock(self):
        nukepatterns()
        
        addPatterns(definition="^220[\x09-\x0d -~]*ftp",
                    protocol="FTP", 
                    blocked=True,
                    category="Web", 
                    description="File Transfer Protocol")
        result = clientControl.runCommand("wget -q -O /dev/null -4 -t 2 ftp://test.untangle.com")
        assert (result != 0)
        time.sleep(3);
        flushEvents()
        query = None;
        for q in node.getEventQueries():
            if q['name'] == 'All Events': query = q;
        assert(query != None)
        events = uvmContext.getEvents(query['query'],defaultRackId,1)
        assert(events != None)
        found = clientControl.check_events( events.get('list'), 5,
                                            'c_client_addr', ClientControl.hostIP,
                                            'protofilter_protocol', 'FTP',
                                            'protofilter_blocked', True )
        assert( found )

    def test_050_testDnsUdpPatternLog(self):
        nukepatterns()
        
        addPatterns(definition="^.?.?.?.?[\x01\x02].?.?.?.?.?.?[\x01-?][a-z0-9][\x01-?a-z]*[\x02-\x06][a-z][a-z][fglmoprstuvz]?[aeop]?(um)?[\x01-\x10\x1c]",
                    protocol="DNS", 
                    blocked=False,
                    category="Web", 
                    description="Domain Name System")
        result = clientControl.runCommand("host -R 1 www.google.com 8.8.8.8")
        assert (result == 0)
        time.sleep(3);
        flushEvents()
        query = None;
        for q in node.getEventQueries():
            if q['name'] == 'All Events': query = q;
        assert(query != None)
        events = uvmContext.getEvents(query['query'],defaultRackId,1)
        assert(events != None)
        found = clientControl.check_events( events.get('list'), 5,
                                            'c_client_addr', ClientControl.hostIP,
                                            'protofilter_protocol', 'DNS',
                                            'protofilter_blocked', False )
        assert( found )

    def test_060_testDnsUdpPatternBlock(self):
        nukepatterns()
        
        addPatterns(definition="^.?.?.?.?[\x01\x02].?.?.?.?.?.?[\x01-?][a-z0-9][\x01-?a-z]*[\x02-\x06][a-z][a-z][fglmoprstuvz]?[aeop]?(um)?[\x01-\x10\x1c]",
                    protocol="DNS", 
                    blocked=True,
                    category="Web", 
                    description="Domain Name System")
        result = clientControl.runCommand("host -R 1 www.google.com 8.8.8.8")
        assert (result != 0)
        time.sleep(3);
        flushEvents()
        query = None;
        for q in node.getEventQueries():
            if q['name'] == 'All Events': query = q;
        assert(query != None)
        events = uvmContext.getEvents(query['query'],defaultRackId,1)
        assert(events != None)
        found = clientControl.check_events( events.get('list'), 5,
                                            'c_client_addr', ClientControl.hostIP,
                                            'protofilter_protocol', 'DNS',
                                            'protofilter_blocked', True )
        assert( found )

    @staticmethod
    def finalTearDown(self):
        global node
        if node != None:
            uvmContext.nodeManager().destroy( node.getNodeSettings()["id"] )
            node = None
        

TestDict.registerNode("protofilter", ProtofilterTests)