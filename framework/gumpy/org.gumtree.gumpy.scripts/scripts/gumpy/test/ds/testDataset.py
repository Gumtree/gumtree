#from nexus.dataset import df as ECH
#from nexus import browser
#from nexus.array import Array
#from nexus.data import Data
#from nexus.simpledata import SimpleData
#from nexus.dataset import Dataset
#from nexus import array
#from nexus import data


#viewer = browser.DataBrowser(True)
#d1 = ECH[4918]
##da1 = d1.data
##da1[0,0, 30:].fill(33)
##viewer.add(d1)
##viewer.select(d1)
#d2 = Dataset(data.arange(48, [6, 8]), anames=['sx', 'stth'], aunits = ['mm', 'degrees'])
#d2.title= 'arrange data 48'
#viewer.add(d2)
#viewer.select(d2)
#print 'done'

#import echidna
#from nexus import dataset
#from nexus.dataset import Dataset
#from nexus.dataset import df as ECH
#from nexus.data import Data
#from nexus.array import Array
#from nexus import data
#from nexus import SimpleData
from gumpy.nexus import *
from echidna import *
import sys


###########################################################################
#    Test loading files
###########################################################################
def test_factory():
    print '\nTEST FACTORY'
    try :
        ds1 = ECH[4918]
        if (ds1[0,0,10,:10] == Array([0.0, 0.0, 8.06142024405325, 1.0076775305066563, \
                                        2.0153550610133126, 3.0230325915199687, 2.0153550610133126, \
                                        2.0153550610133126, 0.0, 0.0])).all() :
            print 'passed'
        else :
            print 'test_factory: name=ds1 slice val failed'
        if ds1.shape == [50, 1, 128, 128] :
            print 'passed'
        else :
            print 'test_factory: name=ds1 shape attr failed'
        if ds1.stth[49] == 5.200595855712891 :
            print 'passed'
        else :
            print 'test_factory: name=ds1 stth attr failed'
        if str(ds1.phone) == 'x9522' :
            print 'passed'
        else :
            print 'test_factory: name=ds1 phone attr failed'
    except :
        print 'test_factory: name=ds1 exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test math 
###########################################################################
def test_math():
    print '\nTEST MATH'
    ds1 = ECH[4918]
    try :
        ds2 = ds1[0,0,10,:10] * 1.2
        if (ds2 == Array([0.0, 0.0, 9.673704292863901, 1.2092130366079876, 2.4184260732159752, \
                            3.627639109823962, 2.4184260732159752, 2.4184260732159752, 0.0, 0.0])).all() :
            print 'passed'
        else :
            print 'test_math: name=ds2 multiplying value failed'
        if (ds2.err == Array([0.0, 0.0, 3.420171022415161, 1.2092130184173584, 1.7100855112075806, \
                                2.094418525695801, 1.7100855112075806, 1.7100855112075806, 0.0, 0.0])).all() :
            print 'passed'
        else :
            print 'test_math: name=ds3 multiplying err failed'
    except :
        print 'test_math: name=ds2 exception failed'
        print sys.exc_info()[0]
    try :
        ds3 = ds1[0,0,10,:10] + 1.2
        if (ds3 == Array([1.2, 1.2, 9.26142024405325, 2.2076775305066563, 3.2153550610133124, \
                            4.223032591519969, 3.2153550610133124, 3.2153550610133124, 1.2, 1.2])).all() :
            print 'passed'
        else :
            print 'test_math: name=ds3 adding value failed'
        if (ds3.err == Array([0.0, 0.0, 2.850142478942871, 1.0076775550842285, 1.4250712394714355, \
                                1.7453486919403076, 1.4250712394714355, 1.4250712394714355, 0.0, 0.0])).all() :
            print 'passed'
        else :
            print 'test_math: name=ds3 adding value failed'
    except :
        print 'test_factory: name=ds3 exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test get item 
###########################################################################
def test_getitem():
    print '\nTEST GETITEM'
    ds1 = ECH[4918]
    try :
        gi1 = ds1[1]
        if gi1.stth == 2.8005080223083496 :
            print 'passed'
        else :
            print 'test_getitem: name=gi1 item=1 value failed'
    except :
        print 'test_getitem: name=gi1 exception failed'
        print sys.exc_info()[0]
    try :
        gi2 = ds1[2, 0]
        if gi2.stth == 2.850482940673828 :
            print 'passed'
        else :
            print 'test_getitem: name=gi2 item=1 value failed'
    except :
        print 'test_getitem: name=gi2 exception failed'
        print sys.exc_info()[0]
    try :
        gi3 = ds1[2:10, 0][1]
        if gi3.stth == 2.9005119800567627 :
            print 'passed'
        else :
            print 'test_getitem: name=gi3 item=1 value failed'
    except :
        print 'test_getitem: name=gi3 exception failed'
        print sys.exc_info()[0]
    try :
        gi4 = ds1[2:10, 0][1, 0]
        if gi4.stth == 2.9005119800567627 :
            print 'passed'
        else :
            print 'test_getitem: name=gi4 item=1 value failed'
    except :
        print 'test_getitem: name=gi4 exception failed'
        print sys.exc_info()[0]
    try :
        gi5 = ds1[2:10, 0][1, 0, 1:]
        if gi5.stth == 2.9005119800567627 :
            print 'passed'
        else :
            print 'test_getitem: name=gi5 item=1 value failed'
    except :
        print 'test_getitem: name=gi5 exception failed'
        print sys.exc_info()[0]
    try :
        gi5.stth = 4.3
        if (ds1.stth == 4.3)[3] :
            print 'passed'
        else :
            print 'test_getitem: name=gi5 item=1 value failed'
    except :
        print 'test_getitem: name=gi5 exception failed'
        print sys.exc_info()[0]
        
def suite():
    import testData
    testData.data = dataset
    testData.Data = Dataset
    testData.suite()
    print '\n############ TEST DATASET START ##############'
    test_factory()
    test_math()
    test_getitem()
    print '\n############ TEST DATASET END ##############'
    
suite()