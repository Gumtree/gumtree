#from nexus.simpledata import SimpleData
#from nexus.array import Array
from gumpy.nexus import *
import sys

###########################################################################
#    Test creating simple data
###########################################################################
def test_new_simpledata():
    print 'TEST SIMPLEDATA CREATION'
    try :
        crt1 = simpledata.arange(12, [3, 4])
        pr = repr(crt1)
        crt2 = eval(pr)
        if crt2.tolist() == [[0, 1, 2, 3], [4, 5, 6, 7], [8, 9, 10, 11]] :
            print 'passed'
        else :
            print 'test_new_simpledata: name=crt2, value failed'
    except :
        print 'test_new_simpledata: exception thrown'
        print sys.exc_info()[0]


        
def suite():
    import sharedTest
    sharedTest.array = simpledata
    sharedTest.AClass = SimpleData
    sharedTest.suite()
    print '\n############ TEST SIMPLEDATA START ##############'
    test_new_simpledata()
    print '############ TEST SIMPLEDATA END   ##############'
    
suite()