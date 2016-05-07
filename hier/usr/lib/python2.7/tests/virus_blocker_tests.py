import unittest2
import time
import sys
import os
import platform
from jsonrpc import ServiceProxy
from jsonrpc import JSONRPCException
from uvm import Manager
from uvm import Uvm
import remote_control
from tests.virus_blocker_base_tests import VirusBlockerBaseTests
import test_registry

#
# Just extends the virus base tests
#
class VirusBlockTests(VirusBlockerBaseTests):

    @staticmethod
    def nodeName():
        return "untangle-node-virus-blocker"

    @staticmethod
    def shortName():
        return "virus_blocker"

    @staticmethod
    def displayName():
        return "Virus Blocker"

    # verify daemon is running
    def test_009_bdamserverIsRunning(self):
        if platform.machine().startswith('arm'):
            raise unittest2.SkipTest('bdam not supported on ARM')

        # check that server is running
        time.sleep(1) 
        result = os.system("pidof bdamserver >/dev/null 2>&1")
        assert ( result == 0 )

        # give it up to 20 minutes to download signatures for the first time
        print "Waiting for server to start..."
        for i in xrange(1200):
            time.sleep(1) 
            result = os.system("cat /var/log/bdamserver.log | grep -q 'Server is started' >/dev/null 2>&1")
            if result == 0:
                break
        print "Number of sleep cycles waiting for bdamserver %d" % i
        # do a scan - this forces it to wait until the signatures are done downloading
        result = os.system("touch /tmp/bdamtest ; bdamclient -p 127.0.0.1:1344 /tmp/bdamtest >/dev/null 2>&1")
        assert (result == 0)

    # test the cloud scanner using our special test virus
    def test_010_httpCloudBlocked(self):
        result = remote_control.runCommand("wget -q -O - http://test.untangle.com/test/UntangleVirus.exe 2>&1 | grep -q blocked")
        assert (result == 0)

test_registry.registerNode("virus-blocker", VirusBlockTests)

