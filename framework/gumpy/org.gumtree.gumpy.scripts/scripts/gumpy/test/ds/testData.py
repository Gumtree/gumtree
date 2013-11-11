#from nexus.data import Data
#from nexus.array import Array
#from nexus import data
#from nexus import SimpleData
print 'load testData'
from gumpy.nexus import *
import sys
from gumpy.commons import jutils

data = data
Data = Data

###########################################################################
#    Test creating data
###########################################################################
def test_new_data():
    print 'TEST DATA CREATION'
    try :
        crt1 = data.arange(12, [3, 4])
        pr = repr(crt1)
        crt2 = eval(pr)
        if crt2.tolist() == [[0, 1, 2, 3], [4, 5, 6, 7], [8, 9, 10, 11]] :
            print 'passed'
        else :
            print 'test_new_data: name=crt2, value failed'
    except :
        print 'test_new_data: exception thrown'
        print sys.exc_info()[0]

###########################################################################
#    Test data with var
###########################################################################
def test_var():
    print '\nTEST DATA WITH VAR'
    try :
        var1 = data.arange(12, [3, 4])
        var1.set_var(Array(var1))
        var2 = var1.err[2]
        print var2
        if (var2 == Data([2.8284270763397217, 3.0, 3.1622776985168457, 3.316624879837036])).all() :
            print 'passed'
        else :
            print 'test_var: name=var2, value failed'
        var3 = eval(repr(var1))
        if (var3.err[2] == Data([2.8284270763397217, 3.0, 3.1622776985168457, 3.316624879837036])).all() :
            print 'passed'
        else :
            print 'test_var: name=var3, value failed'
    except :
        print 'test_var: exception thrown'
        print sys.exc_info()[0]

###########################################################################
#    Test data with axes
###########################################################################
def test_axes():
    print '\nTEST DATA WITH AXES'
    try :
        axes1 = data.arange(12, [3, 4])
        axes1.set_var(Array(axes1))
        axes1.set_axes([data.arange(3), data.arange(4)])
        axes2 = axes1.axes[1]
        if axes2.tolist() == [0, 1, 2, 3] :
            print 'passed'
        else :
            print 'test_axes: name=axes2, value failed'
        axes3 = eval(repr(axes1))
        if axes3.axes[0].tolist() == [0, 1, 2] :
            print 'passed'
        else :
            print 'test_axes: name=axes3, value failed'
    except :
        print 'test_axes: exception thrown'
        print sys.exc_info()[0]

###########################################################################
#    Test getitem
###########################################################################
def test_getitem():
    print '\nTEST GET ITEM'
    try :
        ti1 = data.arange(12, [3, 4])
        ti1.set_var(Array(ti1))
        ti1.set_axes([data.arange(3), data.arange(4)],anames = ['sx', 'stth'], aunits=['mm', 'degrees'])
        ti2 = eval(repr(ti1))
        ti4 = ti2[2, 1:3].axes
        if [ti4[0].tolist()] == [[1, 2]] :
            print 'passed'
        else :
            print 'test_getitem: name=ti4, value failed'
        ti5 = eval(repr(ti1[1:3, 1:]))
        ti3 = ti5.axes
        if [ti3[0].tolist(), ti3[1].tolist()] == [[1, 2], [1, 2, 3]] :
            print 'passed'
        else :
            print 'test_getitem: name=ti3, value failed'
    except :
        print 'test_getitem: exception thrown'
        print sys.exc_info()[0]

###########################################################################
#    Test section iterator
###########################################################################
def test_section_iter():
    print '\nTEST SECTION ITERATOR'
    try :
        si0 = Data(data.arange(16, [4, 4]), var=data.arange(16, [4, 4]), \
        axes = [data.arange(4), data.arange(4)], \
        anames = ['sx', 'stth'], aunits=['mm', 'degrees'])
        si1 = si0.section_iter([2,2])
        sid = 0
        si2 = si1.next()
        if si2.tolist() == [[0, 1], [4, 5]] :
            print 'passed'
        else :
            print 'test_section_iter: name=si2, iter=0, value failed'
        if si2.axes[0].tolist() == [0, 1] and si2.axes[1].tolist() == [0, 1] :
            print 'passed'
        else :
            print 'test_section_iter: name=si2, iter=0, axes failed'
        sid += 1
        si2 = si1.next()
        if si2.tolist() == [[2, 3], [6, 7]] :
            print 'passed'
        else :
            print 'test_section_iter: name=si2, iter=0, value failed'
        if si2.axes[0].tolist() == [0, 1] and si2.axes[1].tolist() == [2, 3] :
            print 'passed'
        else :
            print 'test_section_iter: name=si2, iter=0, axes failed'
        sid += 1
        si2 = si1.next()
        if si2.tolist() == [[8, 9], [12, 13]] :
            print 'passed'
        else :
            print 'test_section_iter: name=si2, iter=0, value failed'
        if si2.axes[0].tolist() == [2, 3] and si2.axes[1].tolist() == [0, 1] :
            print 'passed'
        else :
            print 'test_section_iter: name=si2, iter=0, axes failed'
        sid += 1
        si2 = si1.next()
        if si2.tolist() == [[10, 11], [14, 15]] :
            print 'passed'
        else :
            print 'test_section_iter: name=si2, iter=0, value failed'
        if si2.axes[0].tolist() == [2, 3] and si2.axes[1].tolist() == [2, 3] :
            print 'passed'
        else :
            print 'test_section_iter: name=si2, iter=0, axes failed'
        sid += 1
        si2 = si1.next()
    except StopIteration :
        if sid == 4 :
            print 'passed'
        else :
            print 'test_section_iter: id=4, failed to catch StopIteration excption'
    except :
        print 'test_section_iter: exception thrown'
        print sys.exc_info()[0]
        
###########################################################################
#    Test get_section
###########################################################################
def test_get_section():
    print '\nTEST GET SECTION'
    try :
        sec1 = Data(data.arange(32, [4, 8]), var=data.arange(32, [4, 8]), \
                   axes = [data.arange(4), data.arange(8)], \
                   anames = ['sx', 'stth'], aunits=['mm', 'degrees'])
        sec2 = sec1.get_section([1,2],[2,3], [1,2])
        if sec2.tolist() == [[10, 12, 14], [18, 20, 22]] :
            print 'passed'
        else :
            print 'test_get_section: name=sec2, value failed'
        sec3 = sec2.axes
        if [sec3[0].tolist(), sec3[1].tolist()] == [[1, 2], [2, 4, 6]] :
            print 'passed'
        else :
            print 'test_get_section: name=sec3, value failed'
    except :
        print 'test_get_section: exception thrown'
        print sys.exc_info()[0]
        
###########################################################################
#    Test get_slice
###########################################################################
def test_get_slice():
    print '\nTEST GET SLICE'
    try :
        gs1 = Data(data.arange(32, [4, 8]), var=data.arange(32, [4, 8]), \
                  axes = [data.arange(4), data.arange(8)], \
                  anames = ['sx', 'stth'], aunits=['mm', 'degrees'])
        
        #print gs2.tolist()
        #print [gs2.axes[0].tolist(), gs2.axes[1].tolist()]
        gs2 = gs1.get_slice(1, 3)
        gs3 = gs1.get_slice(0, 3)
        if gs2.tolist() == [3, 11, 19, 27] :
            print 'passed'
        else :
            print 'test_get_slice: name=gs2, value failed'
        if gs2.axes[0].tolist() == [0, 1, 2, 3] :
            print 'passed'
        else :
            print 'test_get_slice: name=gs2.axes, value failed'
        if gs3.tolist() == [24, 25, 26, 27, 28, 29, 30, 31] :
            print 'passed'
        else :
            print 'test_get_slice: name=gs3, value failed'
        if gs3.axes[0].tolist() == [0, 1, 2, 3, 4, 5, 6, 7] :
            print 'passed'
        else :
            print 'test_get_slice: name=gs3.axes, value failed'
    except :
        print 'test_get_slice: exception thrown'
        print sys.exc_info()[0]

###########################################################################
#    Test take
###########################################################################
def test_take():
    print '\nTEST TAKE'
    try :
        tk1 = Data(data.arange(32, [4, 8]), var=data.arange(32, [4, 8]), \
                   axes = [data.arange(4), data.arange(8)], \
                   anames = ['sx', 'stth'], aunits=['mm', 'degrees'])
        tk2 = tk1.take(s_[::2], 1)
        if tk2.tolist() == [[0, 2, 4, 6], [8, 10, 12, 14], \
                            [16, 18, 20, 22], [24, 26, 28, 30]] :
            print 'passed'
        else :
            print 'test_take: name=tk2, value failed'
        if tk2.var.tolist() == [[0, 2, 4, 6], [8, 10, 12, 14], \
                            [16, 18, 20, 22], [24, 26, 28, 30]] :
            print 'passed'
        else :
            print 'test_take: name=tk2, value failed'
        if [tk2.axes[0].tolist(), tk2.axes[1].tolist()] == [[0, 1, 2, 3], \
                                                            [0, 2, 4, 6]] :
            print 'passed'
        else :
            print 'test_take: name=tk2.axes, value failed'
        tk3 = Data(data.arange(24, [2, 6, 2]), var=data.arange(24, [2, 6, 2]), \
                   axes = [data.arange(2), data.arange(6), data.arange(2)], \
                   anames = ['index', 'sx', 'stth'], aunits=['', 'mm', 'degrees'])
        tk4 = tk3.take([1, 2, 5], 1)
        if tk4.tolist() == [[[2, 3], [4, 5], [10, 11]], [[14, 15], [16, 17], \
                                                         [22, 23]]] :
            print 'passed'
        else :
            print 'test_take: name=tk4 value failed'
        if tk4.var.tolist() == [[[2, 3], [4, 5], [10, 11]], [[14, 15], [16, 17], \
                                                         [22, 23]]] :
            print 'passed'
        else :
            print 'test_take: name=tk4, value failed'
        if [tk4.axes[0].tolist(), tk4.axes[1].tolist(), tk4.axes[2].tolist()] == \
            [[0, 1], [1, 2, 5], [0, 1]] :
            print 'passed'
        else :
            print 'test_take: name=tk4.axes, value failed'
    except :
        print 'test_take: exception thrown'
        print sys.exc_info()[0]

###########################################################################
#    Test iterator
###########################################################################
def test_iter():
    print '\nTEST ITERATOR'
    try :
        ti1 = Data(data.arange(32, [4, 8]), var=data.arange(32, [4, 8]), \
                   axes = [data.arange(4), data.arange(8)], \
                   anames = ['sx', 'stth'], aunits=['mm', 'degrees'])
        ti2 = []
        for item in ti1 :
            ti2 += [item]
        ti3 = eval(repr(ti2))
        ti4 = []
        for item in ti3 :
            ti4 += [item.tolist()]
        if ti4 == [[0, 1, 2, 3, 4, 5, 6, 7], \
                   [8, 9, 10, 11, 12, 13, 14, 15], \
                   [16, 17, 18, 19, 20, 21, 22, 23], \
                   [24, 25, 26, 27, 28, 29, 30, 31]] :
            print 'passed'
        else :
            print 'test_iter: name=ti4, value failed'
        if ti3[3].var.tolist() == [24, 25, 26, 27, 28, 29, 30, 31] :
            print 'passed'
        else :
            print 'test_iter: name=ti3, value failed'
        if ti3[3].axes[0].tolist() == [0, 1, 2, 3, 4, 5, 6, 7] :
            print 'passed'
        else :
            print 'test_iter: name=tk3.axes, value failed'
        ti5 = []
        for item in ti3[3] :
            ti5 += [item]
        if ti5 == [24, 25, 26, 27, 28, 29, 30, 31] :
            print 'passed'
        else :
            print 'test_iter: name=ti5, value failed'
    except :
        print 'test_iter: exception thrown'
        print sys.exc_info()[0]
              
###########################################################################
#    Test max and min
###########################################################################
def test_get_max_min():
    print '\nTEST GET MAX AND MIN'
    try :
        mm1 = Data(data.arange(81*3, [3, 9, 9]))
        mm2 = mm1.max(1)
        if mm2.tolist() == [170, 179, 188, 197, 206, 215, 224, 233, 242] :
            print 'passed'
        else :
            print 'test_get_max_min: name=mm2, value failed'
        if mm2.var.tolist() == [170, 179, 188, 197, 206, 215, 224, 233, 242] :
            print 'passed'
        else :
            print 'test_get_max_min: name=mm2.var, value failed'
        if mm2.axes[0].tolist() == range(9) :
            print 'passed'
        else :
            print 'test_get_max_min: name=mm2.axes, value failed'
        mm3 = mm1.min(1)
        if mm3.tolist() == [0, 9, 18, 27, 36, 45, 54, 63, 72] :
            print 'passed'
        else :
            print 'test_get_max_min: name=mm3, value failed'
        if mm3.var.tolist() == [0, 9, 18, 27, 36, 45, 54, 63, 72] :
            print 'passed'
        else :
            print 'test_get_max_min: name=mm3.var, value failed'
        if mm3.axes[0].tolist() == range(9) :
            print 'passed'
        else :
            print 'test_get_max_min: name=mm3.axes, value failed'
        mm4 = mm1.min(2)
        if mm4.var.tolist() == [0, 1, 2, 3, 4, 5, 6, 7, 8] :
            print 'passed'
        else :
            print 'test_get_max_min: name=mm4, value failed'
        mm5 = mm1.max(0)
        if mm5.var.tolist() == [80, 161, 242] :
            print 'passed'
        else :
            print 'test_get_max_min: name=mm5, value failed'
    except :
        print 'test_get_max_min: exception thrown'
        print sys.exc_info()[0]
        
###########################################################################
#    Test fill
###########################################################################
def test_fill():
    print '\nTEST FILL'
    try :
        fi1 = data.arange(48, [3, 4, 4])
        fi1.fill(4)
        if fi1[0,1].tolist() == [4, 4, 4, 4] and \
                fi1[0,1].var.tolist() == [4, 4, 4, 4] : 
            print 'passed'
        else :
            print 'test_fill: name=fi1, value failed'
        fi1.fill(6, 3)
        if fi1[0,1].tolist() == [6, 6, 6, 6] and \
                fi1[0,1].var.tolist() == [3, 3, 3, 3] : 
            print 'passed'
        else :
            print 'test_fill: name=fi1, value failed'
    except :
        print 'test_fill: exception thrown'
        print sys.exc_info()[0]

###########################################################################
#    Test put
###########################################################################
def test_put():
    print '\nTEST PUT'
    try :
        put1 = data.arange(12, [3, 4])
        put1.put(array.s_[::2], 1, 2.5)
        if put1.tolist() == [[1, 1, 1, 3], [1, 5, 1, 7], [1, 9, 1, 11]] :
            print 'passed'
        else :
            print 'put: name=put1, value failed'
        if put1.var.tolist() == [[2.5, 1.0, 2.5, 3.0], [2.5, 5.0, 2.5, 7.0], \
                                 [2.5, 9.0, 2.5, 11.0]] :
            print 'passed'
        else :
            print 'put: name=put1.var, value failed'
        put1.put(array.s_[1:7], [2, 3], [1, 1.5])
        if put1.tolist() == [[1, 2, 3, 2], [3, 2, 3, 7], [1, 9, 1, 11]] :
            print 'passed'
        else :
            print 'put: name=put1, value failed'
        if put1.var.tolist() == [[2.5, 1.0, 1.5, 1.0], [1.5, 1.0, 1.5, 7.0], \
                                 [2.5, 9.0, 2.5, 11.0]] :
            print 'passed'
        else :
            print 'put: name=put1.var, value failed'
    except :
        print 'put: exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test delete
###########################################################################
def test_delete():
    print '\nTEST DELETE'
    try :
        del1 = data.arange(81, [9, 9])
        del2 = data.delete(del1, array.s_[::2], 1)
        del3 = data.delete(del1, array.s_[1:7], 0)
        if del2.tolist() == [[1, 3, 5, 7], [10, 12, 14, 16], [19, 21, 23, 25], \
                             [28, 30, 32, 34], [37, 39, 41, 43], [46, 48, 50, 52], \
                             [55, 57, 59, 61], [64, 66, 68, 70], [73, 75, 77, 79]] :
            print 'passed'
        else :
            print 'delete: name=del2 axis=1, obj=slice, value failed'
        if del2.tolist() == del2.var.tolist() :
            print 'passed'
        else :
            print 'delete: name=del2 axis=1, obj=slice, var failed'
        if del2.axes[1].tolist() == [1, 3, 5, 7] :
            print 'passed'
        else :
            print 'delete: name=del2 axis=1, obj=slice, axes failed'
        if del3.tolist() == [[0, 1, 2, 3, 4, 5, 6, 7, 8], \
                             [63, 64, 65, 66, 67, 68, 69, 70, 71], \
                             [72, 73, 74, 75, 76, 77, 78, 79, 80]] :
            print 'passed'
        else :
            print 'delete: name=del3 axis=0, obj=slice, value failed'
        if del3.tolist() == del3.var.tolist() :
            print 'passed'
        else :
            print 'delete: name=del3 axis=0, obj=slice, var failed'
        if del3.axes[0].tolist() == [0, 7, 8] :
            print 'passed'
        else :
            print 'delete: name=del3 axis=0, obj=slice, axes failed'
        del4 = data.arange(6)
        del5 = data.delete(del4, 3)
        if del5.tolist() == [0, 1, 2, 4, 5] :
            print 'passed'
        else :
            print 'delete single index: name=del5 value failed'
        if del5.tolist() == del5.var.tolist() :
            print 'passed'
        else :
            print 'delete: name=del5, obj=slice, var failed'
        if del5.axes[0].tolist() == [0, 1, 2, 3, 4] :
            print 'passed'
        else :
            print 'delete: name=del5, obj=slice, axes failed'
        del6 = data.arange(24, [2, 6, 2])
        del7 = data.delete(del6, [1, 2, 5], 1)
        if del7.tolist() == [[[0, 1], [6, 7], [8, 9]], \
                             [[12, 13], [18, 19], [20, 21]]] :
            print 'passed'
        else :
            print 'delete list of items: name=del7 value failed'
        if del7.tolist() == del7.var.tolist() :
            print 'passed'
        else :
            print 'delete: name=del7 axis=1, obj=slice, var failed'
        if del7.axes[1].tolist() == [0, 3, 4] :
            print 'passed'
        else :
            print 'delete: name=del7 axis=1, obj=slice, axes failed'
        del8 = data.delete(del6, [1, 2, 5])
        if del8.tolist() == [0, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, \
                             17, 18, 19, 20, 21, 22, 23] :
            print 'passed'
        else :
            print 'delete list of items in flat: name=del8 value failed'
        if del8.tolist() == del8.var.tolist() :
            print 'passed'
        else :
            print 'delete: name=del7 axis=1, obj=slice, var failed'
        if del8.axes[0].tolist() == [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, \
                                     12, 13, 14, 15, 16, 17, 18, 19, 20] :
            print 'passed'
        else :
            print 'delete: name=del7 axis=1, obj=slice, axes failed'
    except :
        print 'delete: exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test appending
###########################################################################
def test_append():
    print '\nTEST APPEND'
    try :
        ap1 = data.append(data.asarray([[1, 2, 3], [4, 5, 6]]), [[7, 8, 9]], axis=0)
        if ap1.tolist() == [[1, 2, 3], [4, 5, 6], [7, 8, 9]] :
            print 'passed'
        else :
            print 'test_append: name=ap1, value failed'
        if ap1.tolist() == ap1.var.tolist() :
            print 'passed'
        else :
            print 'test_append: name=ap1, var failed'
        if ap1.axes[0].tolist() == [0, 1, 2] :
            print 'passed'
        else :
            print 'test_append: name=ap1, axes failed'
    except :
        print 'test_append: name=ap1, exception failed'
        print sys.exc_info()[0]
    try :
        ap2 = data.append(ap1, ap1, axis=1)
        if ap2.tolist() == [[1.0, 2.0, 3.0, 1.0, 2.0, 3.0], \
                            [4.0, 5.0, 6.0, 4.0, 5.0, 6.0], \
                            [7.0, 8.0, 9.0, 7.0, 8.0, 9.0]] :
            print 'passed'
        else :
            print 'test_append: name=ap2, value failed'
        if ap2.tolist() == ap2.var.tolist() :
            print 'passed'
        else :
            print 'test_append: name=ap2, var failed'
        if ap2.axes[1].tolist() == [0, 1, 2, 3, 4, 5] :
            print 'passed'
        else :
            print 'test_append: name=ap2, axes failed'
    except :
        print 'test_append: name=ap1, exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test column_stack
###########################################################################
def test_column_stack():
    print '\nTEST COLUMN STACK'
    try :
        cs1 = data.arange(6)
        cs1.set_var(array.arange(0, 0.6, 0.1))
        cs2 = data.arange(6, 12)
        cs3 = data.arange(12, 18)
        cs3.set_var(array.arange(1.2, 1.8, 0.1))
        cols = data.column_stack(cs1, cs2, cs3)
        if cols.shape == [6, 3] :
            print 'passed'
        else :
            print 'test_column_stack: name=cols shape failed'
        if cols.tolist() == [[0, 6, 12], [1, 7, 13], [2, 8, 14], \
                             [3, 9, 15], [4, 10, 16], [5, 11, 17]] :
            print 'passed'
        else :
            print 'test_column_stack: name=cols, value failed'
        if cols.var == Array([[0, 6, 12], [1, 7, 13], [2, 8, 14], [3, 9, 15], [4, 10, 16], [5, 11, 17]]) :
            print 'passed'
        else :
            print 'test_column_stack: name=cols, var failed'
        if cols.axes[0].tolist() == [0, 1, 2, 3, 4, 5] :
            print 'passed'
        else :
            print 'test_column_stack: name=cols, axes failed'
    except :
        print 'test_column_stack: name=cols, exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test array_split
###########################################################################
def test_array_split():
    print '\nTEST ARRAY_SPLIT'
    try :
        as1 = data.arange(8, [2, 4])
        as1.set_axes([array.arange(0, 0.2, 0.1), array.arange(0, 0.4, 0.1)], 
             ['sx', 'stth'], ['mm', 'degress'])
        as1.set_var(array.arange(0, 0.8, 0.1, [2, 4]))
        as2 = data.array_split(as1, 2, axis = 0)
        if as2[0].tolist() == [[0, 1, 2, 3]] and as2[1].tolist() == [[4, 5, 6, 7]] :
            print 'passed'
        else :
            print 'array_split: name=as2 axis=0, value failed'
        
        if (as2[1].var == Array([[0.4, 0.5, 0.6, 0.7]])).all() :
            print 'passed'
        else :
            print 'test_column_stack: name=cols, var failed'
        if (as2[1].axes[0] == Array([0.1])).all() :
            print 'passed'
        else :
            print 'test_column_stack: name=cols, axes failed'
    except :
        print 'array_split: exception failed'
        print sys.exc_info()[0]
    try :
        as3 = data.array_split(as1, 4, axis = 1)
        if as3[0].tolist() == [[0], [4]] and as3[1].tolist() == [[1], [5]] \
                and as3[2].tolist() == [[2], [6]] and as3[3].tolist() == [[3], [7]]:
            print 'passed'
        else :
            print 'array_split: name=as3 axis=1, value failed'
        as4 = data.array_split(as1, [2,3], axis = 1)
        if as4[0].tolist() == [[0, 1], [4, 5]] \
                and as4[1].tolist() == [[2], [6]] and as4[2].tolist() == [[3], [7]]:
            print 'passed'
        else :
            print 'array_split: name=as4 list split, value failed'
    except :
        print 'array_split: exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test tile
###########################################################################
def test_tile():
    print '\nTEST TILE'
    try :
        t1 = Data([10,20])
        t1.set_var([3, 4])
        t2 = data.tile(t1, (3, 2))
        if t2.tolist() == [[10, 20, 10, 20], [10, 20, 10, 20], [10, 20, 10, 20]] :
            print 'passed'
        else :
            print 'tile: case1, name=t2 value failed'
        if t2.var.tolist() == [[3, 4, 3, 4], [3, 4, 3, 4], [3, 4, 3, 4]] :
            print 'passed'
        else :
            print 'tile: case1, name=t2 var failed'
    except :
        print 'test_tile: exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test adding
###########################################################################
def test_add():
    print '\nTEST ADDING'
    try :
        add1 = data.arange(12, [3, 4])
        add1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        add2 = add1 + data.arange(4)
        if add2.tolist() == [[0.0, 2.0, 4.0, 6.0], [4.0, 6.0, 8.0, 10.0], \
                             [8.0, 10.0, 12.0, 14.0]] :
            print 'passed'
        else :
            print 'test_add: case1, name=add2 value failed'
        if add2.var.tolist() == add2.tolist() :
            print 'passed'
        else :
            print 'test_add: case1, name=add2 var failed'
    except :
        print 'test_add: name=add2 exception failed'
        print sys.exc_info()[0]
    try :
        add3 = add1 + 3
        if add3.tolist() == [[3.0, 4.0, 5.0, 6.0], [7.0, 8.0, 9.0, 10.0], \
                             [11.0, 12.0, 13.0, 14.0]] :
            print 'passed'
        else :
            print 'test_add: case1, name=add3 value failed'
        if add3.var.tolist() == [[0.0, 1.0, 2.0, 3.0], [4.0, 5.0, 6.0, 7.0], \
                                 [8.0, 9.0, 10.0, 11.0]] :
            print 'passed'
        else :
            print 'test_add: case1, name=add3 var failed'
    except :
        print 'test_add: name=add3 exception failed'
        print sys.exc_info()[0]
    try :
        add4 = add1 + data.arange(0, 1.2, 0.1, [3, 4])
        if add4.tolist() == [[0.0, 1.1, 2.2, 3.3], [4.4, 5.5, 6.6, 7.7], [8.8, 9.9, 11.0, 12.1]] :
            print 'passed'
        else :
            print 'test_add: case1, name=add4 value failed'
        if add4.var.tolist() == add4.tolist() :
            print 'passed'
        else :
            print 'test_add: case1, name=add4 var failed'
    except :
        print 'test_add: name=add4 exception failed'
        print sys.exc_info()[0]
    try :
        add5 = add1 + [3, 4, 3, 4]
        if add5.tolist() == [[3, 5, 5, 7], [7, 9, 9, 11], [11, 13, 13, 15]] :
            print 'passed'
        else :
            print 'test_add: case1, name=add5 value failed'
        if add5.var.tolist() == [[0.0, 1.0, 2.0, 3.0], [4.0, 5.0, 6.0, 7.0], \
                                 [8.0, 9.0, 10.0, 11.0]] :
            print 'passed'
        else :
            print 'test_add: case1, name=add5 var failed'
    except :
        print 'test_add: name=add5 exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test subtracting
###########################################################################
def test_sub():
    print '\nTEST SUB'
    try :
        sub1 = data.arange(12, [3, 4])
        sub1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        sub2 = sub1 - data.arange(3, 4, 0.25)
        if sub2.tolist() == [[-3.0, -2.25, -1.5, -0.75], [1.0, 1.75, 2.5, 3.25], \
                             [5.0, 5.75, 6.5, 7.25]] :
            print 'passed'
        else :
            print 'test_sub: case1, name=sub2 value failed'
        if sub2.var.tolist() == [[3.0, 4.25, 5.5, 6.75], [7.0, 8.25, 9.5, 10.75], \
                                 [11.0, 12.25, 13.5, 14.75]] :
            print 'passed'
        else :
            print 'test_sub: case1, name=sub2 var failed'
    except :
        print 'test_sub: name=sub2 exception failed'
        print sys.exc_info()[0]
    try :
        sub3 = sub1 - 3
        if sub3.tolist() == [[-3.0, -2.0, -1.0, 0.0], [1.0, 2.0, 3.0, 4.0], \
                             [5.0, 6.0, 7.0, 8.0]] :
            print 'passed'
        else :
            print 'test_sub: case1, name=sub3 value failed'
        if sub3.var.tolist() == [[0.0, 1.0, 2.0, 3.0], [4.0, 5.0, 6.0, 7.0], \
                                 [8.0, 9.0, 10.0, 11.0]] :
            print 'passed'
        else :
            print 'test_sub: case1, name=sub3 var failed'
    except :
        print 'test_sub: name=sub3 exception failed'
        print sys.exc_info()[0]
    try :
        sub4 = sub1 - data.arange(0.1, 1.3, 0.1, [3, 4])
        if (sub4 == Array([[-0.1, 0.8, 1.7, 2.6], [3.5, 4.4, 5.3, 6.2], [7.1, 8.0, 8.9, 9.8]])).all() :
            print 'passed'
        else :
            print 'test_sub: case1, name=sub4 value failed'
        if (sub4.err == Array([[0.3162277638912201, 1.095445156097412, 1.5165750980377197, \
                                  1.8439089059829712], [2.1213202476501465, 2.366431951522827, \
                                    2.5884358882904053, 2.7928481101989746], [2.9832868576049805, \
                                    3.1622776985168457, 3.3316662311553955, 3.492849826812744]])).all() :
            print 'passed'
        else :
            print 'test_sub: case1, name=sub4 var failed'
    except :
        print 'test_sub: name=sub4 exception failed'
        print sys.exc_info()[0]
    try :
        sub5 = sub1 - [3, 4, 3, 4]
        if (sub5 == Array([[-3, -3, -1, -1], [1, 1, 3, 3], [5, 5, 7, 7]])).all() :
            print 'passed'
        else :
            print 'test_sub: case1, name=sub5 value failed'
        if (sub5.var == Array([[0.0, 1.0, 2.0, 3.0], [4.0, 5.0, 6.0, 7.0], \
                                 [8.0, 9.0, 10.0, 11.0]])).all() :
            print 'passed'
        else :
            print 'test_sub: case1, name=sub5 var failed'
    except :
        print 'test_sub: name=sub5 exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test multiply
###########################################################################
def test_mul():
    print '\nTEST MUL'
    try :
        mul1 = data.arange(12, [3, 4])
        mul1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        mul2 = mul1 * data.arange(3, 4, 0.25)
        if (mul2 == Array([[0.0, 3.25, 7.0, 11.25], [12.0, 16.25, 21.0, 26.25], \
                             [24.0, 29.25, 35.0, 41.25]])).all() :
            print 'passed'
        else :
            print 'test_mul: case1, name=mul2 value failed'
        if (mul2.var == Array([[0.0, 13.8125, 38.5, 75.9375], \
                                 [84.0, 134.0625, 199.5, 282.1875], \
                                 [264.0, 358.3125, 472.5, 608.4375]])).all() :
            print 'passed'
        else :
            print 'test_mul: case1, name=mul2 var failed'
    except :
        print 'test_mul: name=mul2 exception failed'
        print sys.exc_info()[0]
    try :
        mul3 = mul1 * 3
        if (mul3 == Array([[0.0, 3.0, 6.0, 9.0], [12.0, 15.0, 18.0, 21.0], \
                             [24.0, 27.0, 30.0, 33.0]])).all() :
            print 'passed'
        else :
            print 'test_mul: case1, name=mul3 value failed'
        if (mul3.var == Array([[0.0, 9.0, 18.0, 27.0], \
                                 [36.0, 45.0, 54.0, 63.0], \
                                 [72.0, 81.0, 90.0, 99.0]])).all() :
            print 'passed'
        else :
            print 'test_mul: case1, name=mul3 var failed'
    except :
        print 'test_mul: name=mul3 exception failed'
        print sys.exc_info()[0]
    try :
        mul4 = mul1 * data.arange(0.1, 1.3, 0.1, [3, 4])
        if (mul4 == Array([[0.0, 0.20000000298023224, 0.6000000238418579, 1.2000000178813934], \
                             [2.0, 3.0000001192092896, 4.199999928474426, 5.600000083446503], \
                             [7.199999809265137, 9.0, 11.000000238418579, 13.200000524520874]])).all() :
            print 'passed'
        else :
            print 'test_mul: case1, name=mul4 value failed'
        if (mul4.err == Array([[0.0, 0.4898979663848877, 1.1747339963912964, 2.0199010372161865], \
                                 [3.0, 4.098780632019043, 5.304714679718018, 6.609084606170654], \
                                 [8.004998207092285, 9.486832618713379, 11.049886703491211, 12.690154075622559]])).all() :
            print 'passed'
        else :
            print 'test_mul: case1, name=mul4 var failed'
    except :
        print 'test_mul: name=mul4 exception failed'
        print sys.exc_info()[0]
    try :
        mul5 = mul1 * [3, 4, 3, 4]
        if (mul5 == Array([[0.0, 4.0, 6.0, 12.0], [12.0, 20.0, 18.0, 28.0], \
                             [24.0, 36.0, 30.0, 44.0]])).all() :
            print 'passed'
        else :
            print 'test_mul: case1, name=mul5 value failed'
        if (mul5.var == Array([[0.0, 16.0, 18.0, 48.0], \
                                 [36.0, 80.0, 54.0, 112.0], \
                                 [72.0, 144.0, 90.0, 176.0]])).all() :
            print 'passed'
        else :
            print 'test_mul: case1, name=mul5 var failed'
    except :
        print 'test_mul: name=mul5 exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test divide
###########################################################################
def test_div():
    print '\nTEST DIV'
    try :
        div1 = data.arange(12, [3, 4])
        div1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        div2 = div1 / data.arange(3, 4, 0.25)
        if (div2 == Array([[0.0, 0.3076923076923077, 0.5714285714285714, 0.8], \
                             [1.3333333333333333, 1.5384615384615385, 1.7142857142857142, \
                              1.8666666666666667], [2.6666666666666665, 2.769230769230769, \
                                                    2.857142857142857, 2.933333333333333]])).all() :
            print 'passed'
        else :
            print 'test_div: case1, name=div2 value failed'
        if (div2.err == Array([[0.0, 0.3518596291542053, 0.5065172910690308, 0.6196773648262024], \
                                 [1.0183501243591309, 1.096192717552185, 1.1530160903930664, \
                                  1.1945555210113525], [1.8053418397903442, 1.7921082973480225, \
                                                        1.7744542360305786, 1.7540640830993652]])).all() :
            print 'passed'
        else :
            print 'test_div: case1, name=div2 var failed'
    except :
        print 'test_div: name=div2 exception failed'
        print sys.exc_info()[0]
    try :
        div3 = div1 / 3.
        if (div3 == Array([[0.0, 0.3333333333333333, 0.6666666666666666, 1.0], \
                             [1.3333333333333333, 1.6666666666666665, 2.0, 2.333333333333333], \
                             [2.6666666666666665, 3.0, 3.333333333333333, 3.6666666666666665]])).all() :
            print 'passed'
        else :
            print 'test_div: case1, name=div3 value failed'
        if (div3.err == Array([[0.0, 0.3333333432674408, 0.4714045226573944, 0.5773502588272095], \
                                 [0.6666666865348816, 0.7453559637069702, 0.8164966106414795, \
                                  0.8819171190261841], [0.9428090453147888, 1.0, 1.054092526435852, \
                                                        1.1055415868759155]])).all() :
            print 'passed'
        else :
            print 'test_div: case1, name=div3 var failed'
    except :
        print 'test_div: name=div3 exception failed'
        print sys.exc_info()[0]
    try :
        div4 = div1 / data.arange(0.1, 1.3, 0.1, [3, 4])
        if (div4 == Array([[0.0, 4.999999925494195, 6.666666401757145, 7.499999888241293], \
                             [8.0, 8.333333002196431, 8.571428717399133, 8.749999869614841], \
                             [8.888889124364034, 9.0, 9.090908893868948, 9.166666302416074]])).all() :
            print 'passed'
        else :
            print 'test_div: case1, name=div4 value failed'
        if (div4.err == Array([[0.0, 12.247448921203613, 13.05260013830081, 12.624381065368652], \
                                 [12.0, 11.385499954223633, 10.825948715209961, 10.32669448852539], \
                                 [9.88271427154541, 9.486832618713379, 9.132137298583984, \
                                  8.812605857849121]])).all() :
            print 'passed'
        else :
            print 'test_div: case1, name=div4 var failed'
    except :
        print 'test_div: name=div4 exception failed'
        print sys.exc_info()[0]
    try :
        div5 = div1 / [3, 4, 3, 4]
        if (div5 == Array([[0.0, 0.25, 0.6666666666666666, 0.75], \
                             [1.3333333333333333, 1.25, 2.0, 1.75], \
                             [2.6666666666666665, 2.25, 3.3333333333333335, 2.75]])).all() :
            print 'passed'
        else :
            print 'test_div: case1, name=div5 value failed'
        if (div5.err == Array([[0.0, 0.25, 0.4714045226573944, 0.4330126941204071], \
                                 [0.6666666865348816, 0.55901700258255, 0.8164966106414795, \
                                  0.6614378094673157], [0.9428090453147888, 0.75, \
                                                        1.054092526435852, 0.829156219959259]])).all() :
            print 'passed'
        else :
            print 'test_div: case1, name=div5 var failed'
    except :
        print 'test_div: name=div5 exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test power
###########################################################################
def test_pow():
    print '\nTEST POW'
    try :
        pow1 = data.arange(12, [3, 4])
        pow1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        pow2 = pow1 ** 2
        if pow2.tolist() == [[0.0, 1.0, 4.0, 9.0], [16.0, 25.0, 36.0, 49.0], \
                             [64.0, 81.0, 100.0, 121.0]] :
            print 'passed'
        else :
            print 'test_pow: case1, name=pow2 value failed'
        if pow2.var.tolist() == [[0.0, 4.0, 32.0, 108.0], [256.0, 500.0, 864.0, 1372.0], \
                                 [2048.0, 2916.0, 4000.0, 5324.0]] :
            print 'passed'
        else :
            print 'test_pow: case1, name=pow2 var failed'
    except :
        print 'test_pow: name=pow2 exception failed'
        print sys.exc_info()[0]
    try :
        pow3 = pow1 ** 0.5
        if (pow3 == Array([[0.0, 1.0, 1.4142135623730951, 1.7320508075688772], \
                             [2.0, 2.23606797749979, 2.449489742783178, 2.6457513110645907], \
                             [2.8284271247461903, 3.0, 3.1622776601683795, 3.3166247903554]])).all() :
            print 'passed'
        else :
            print 'test_pow: case1, name=pow3 value failed'
        if pow3.var.tolist()[1:] == [[0.25, 0.25, 0.25, 0.25], \
                                 [0.25, 0.25, 0.25, 0.25]] :
            print 'passed'
        else :
            print 'test_pow: case1, name=pow3 var failed'
    except :
        print 'test_pow: name=pow3 exception failed'
        print sys.exc_info()[0]
    try :
        pow4 = pow1 ** (-1.5)
        if (pow4 == Array([[jutils.get_inf(), 1.0, 0.35355339059327373, 0.19245008972987526], \
                             [0.125, 0.08944271909999159, 0.06804138174397717, 0.05399492471560389], \
                             [0.044194173824159216, 0.037037037037037035, 0.03162277660168379, \
                              0.02741012223434215]])).all() :
            print 'passed'
        else :
            print 'test_pow: case1, name=pow4 value failed'
        if (pow4[1:].err == Array([[0.09375, 0.05999999865889549, 0.0416666679084301, 0.030612245202064514], \
                                     [0.0234375, 0.018518518656492233, 0.014999999664723873, 0.012396694160997868]])).all() :
            print 'passed'
        else :
            print 'test_pow: case1, name=pow4 var failed'
    except :
        print 'test_pow: name=pow4 exception failed'
        print sys.exc_info()[0]
    try :
        pow5 = pow1 ** (-0.3)
        if (pow5 == Array([[jutils.get_inf(), 1.0, 0.8122523963562356, 0.7192230933248644], \
                             [0.6597539553864471, 0.6170338627200096, 0.5841906810678655, 0.5577898253032461], \
                             [0.5358867312681466, 0.5172818579717866, 0.5011872336272722, 0.48705969722582854]])).all() :
            print 'passed'
        else :
            print 'test_pow: case1, name=pow5 value failed'
        if (pow5[1:].err == Array([[0.09896309673786163, 0.0827837809920311, \
                                        0.0715484544634819, 0.06324741989374161], [0.05683937296271324, \
                                        0.05172818526625633, 0.04754679650068283, 0.044056206941604614]])).all() :
           print 'passed'
        else :
            print 'test_pow: case1, name=pow5 var failed'
    except :
        print 'test_pow: name=pow5 exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test exp
###########################################################################
def test_exp():
    print '\nTEST EXP'
    try :
        exp1 = data.arange(12, [3, 4])
        exp1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        exp2 = data.exp(exp1)
        if (exp2 == Array([[1.0, 2.7182818284590455, 7.38905609893065, 20.085536923187668], \
                             [54.598150033144236, 148.4131591025766, 403.4287934927351, 1096.6331584284585], \
                             [2980.9579870417283, 8103.083927575384, 22026.465794806718, 59874.14171519782]])).all() :
            print 'passed'
        else :
            print 'test_exp: case1, name=exp2 value failed'
        if (exp2.err == Array([[0.0, 2.7182817459106445, 10.449703216552734, 34.78917044826171], \
                                 [109.19630006628847, 331.861912508853, 988.1946916038476, 2901.4186166689974], \
                                 [8431.422428277627, 24309.251782726154, 69653.80071538023, 198580.06271387744]])).all() :
            print 'passed'
        else :
            print 'test_exp: case1, name=exp2 var failed'
    except :
        print 'test_exp: name=exp2 exception failed'
        print sys.exc_info()[0]
        
###########################################################################
#    Test log10
###########################################################################
def test_log10():
    print '\nTEST LOG10'
    try :
        log1 = data.arange(12, [3, 4])
        log1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        log2 = data.log10(log1)
        if (log2[1:] == Array([[0.6020599913279624, 0.6989700043360189, 0.7781512503836436, 0.8450980400142568], \
                                 [0.9030899869919435, 0.9542425094393249, 1.0, 1.0413926851582251]])).all() :
            print 'passed'
        else :
            print 'test_log10: case1, name=log2 value failed'
        if (log2[1:].err == Array([[0.21714724600315094, 0.19422239065170288, 0.17729997634887695, 0.16414788365364075], \
                                     [0.1535462886095047, 0.14476482570171356, 0.13733597099781036, 0.13094471395015717]])).all() :
            print 'passed'
        else :
            print 'test_log10: case1, name=log2 var failed'
    except :
        print 'test_log10: name=log2 exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test ln
###########################################################################
def test_ln():
    print '\nTEST LN'
    try :
        ln1 = data.arange(12, [3, 4])
        ln1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        ln2 = data.ln(ln1)
        if (ln2[1:] == Array([[1.3862943611198906, 1.6094379124341003, 1.791759469228055, 1.9459101490553132], \
                                [2.0794415416798357, 2.1972245773362196, 2.302585092994046, 2.3978952727983707]])).all() :
            print 'passed'
        else :
            print 'test_ln: case1, name=ln2 value failed'
        if (ln2[1:].err == Array([[0.5, 0.4472135901451111, 0.40824830532073975, 0.37796446681022644], \
                                    [0.3535533845424652, 0.3333333432674408, 0.3162277638912201, 0.30151134729385376]])).all() :
            print 'passed'
        else :
            print 'test_ln: case1, name=ln2 var failed'
    except :
        print 'test_ln: name=ln2 exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test sqrt
###########################################################################
def test_sqrt():
    print '\nTEST SQRT'
    try :
        sqrt1 = data.arange(12, [3, 4])
        sqrt1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        sqrt2 = data.sqrt(sqrt1)
        if (sqrt2 == Array([[0.0, 1.0, 1.4142135623730951, 1.7320508075688772], \
                              [2.0, 2.23606797749979, 2.449489742783178, 2.6457513110645907], \
                              [2.8284271247461903, 3.0, 3.1622776601683795, 3.3166247903554]])).all() :
            print 'passed'
        else :
            print 'test_sqrt: case1, name=sqrt2 value failed'
        if sqrt2.err.tolist() == [[0.0, 0.5, 0.5, 0.5], [0.5, 0.5, 0.5, 0.5], \
                                      [0.5, 0.5, 0.5, 0.5]] :
            print 'passed'
        else :
            print 'test_sqrt: case1, name=sqrt2 var failed'
    except :
        print 'test_sqrt: name=sqrt2 exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test sin
###########################################################################
def test_sin():
    print '\nTEST TRIGONOMETRY '
    try :
        sin1 = data.arange(12, [3, 4])
        sin1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        sin2 = data.sin(sin1)
        if (sin2 == Array([[0.0, 0.8414709848078965, 0.9092974268256817, 0.1411200080598672], \
                             [-0.7568024953079282, -0.9589242746631385, -0.27941549819892586, 0.6569865987187891], \
                             [0.9893582466233818, 0.4121184852417566, -0.5440211108893698, -0.9999902065507035]])).all() :
            print 'passed'
        else :
            print 'test_sin: case1, name=sin2 value failed'
        if (sin2.err == Array([[0.0, 0.5403022766113281, 0.5885205268859863, 1.7147172689437866], \
                                 [1.3072872161865234, 0.63428795337677, 2.3519272804260254, 1.9946378469467163], \
                                 [0.41153624653816223, 2.7333908081054688, 2.653377056121826, 0.014678379520773888]])).all() :
            print 'passed'
        else :
            print 'test_sin: case1, name=sin2 var failed'
    except :
        print 'test_sin: name=sin2 exception failed'
        print sys.exc_info()[0]
    try :
        cos1 = data.arange(12, [3, 4])
        cos1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        cos2 = data.cos(cos1)
        if (cos2 == Array([[1.0, 0.5403023058681398, -0.4161468365471424, -0.9899924966004454], \
                             [-0.6536436208636119, 0.28366218546322625, 0.9601702866503661, 0.7539022543433046], \
                             [-0.14550003380861354, -0.9111302618846769, -0.8390715290764524, 0.004425697988050785]])).all() :
            print 'passed'
        else :
            print 'test_cos: case1, name=cos2 value failed'
        if (cos2.err == Array([[0.0, 0.8414709568023682, 1.2859407663345337, 0.24442702531814575], \
                                 [1.513604998588562, 2.1442198753356934, 0.684425413608551, 1.7382231950759888], \
                                 [2.798327684402466, 1.2363554239273071, 1.7203458547592163, 3.316592216491699]])).all() :
            print 'passed'
        else :
            print 'test_cos: case1, name=cos2 var failed'
    except :
        print 'test_cos: name=cos2 exception failed'
        print sys.exc_info()[0]
    try :
        tan1 = data.arange(12, [3, 4])
        tan1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        tan2 = data.tan(tan1)
        if (tan2 == Array([[0.0, 1.5574077246549023, -2.185039863261519, -0.1425465430742778], \
                             [1.1578212823495777, -3.380515006246586, -0.29100619138474915, 0.8714479827243187], \
                             [-6.799711455220379, -0.45231565944180985, 0.6483608274590866, -225.95084645419513]])).all() :
            print 'passed'
        else :
            print 'test_tan: case1, name=tan2 value failed'
        if (tan2.err == Array([[0.0, 3.425518820814759, 8.166233668912485, 1.7672452432984176], \
                               [4.6811002437232405, 27.789588314203037, 2.656923810242557, 4.65499196944717], \
                               [133.6037982693126, 3.6137683673288383, 4.491609893951571, 169329.56564159924]])).all() :
            print 'passed'
        else :
            print 'test_tan: case1, name=tan2 var failed'
    except :
        print 'test_tan: name=tan2 exception failed'
        print sys.exc_info()[0]
    try :
        asin1 = data.arange(0, 1, 0.1, [2, 5])
        asin1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        asin2 = data.arcsin(asin1)
        if (asin2 == Array([[0.0, 0.10016742265918284, 0.2013579238320176, 0.3046926665119266, 0.41151685257088794], \
                              [0.5235987755982989, 0.6435011385956071, 0.7753974799181138, 0.9272952378698274, 1.119769460301664]])).all() :
            print 'passed'
        else :
            print 'test_asin: case1, name=asin2 value failed'
        if (asin2.err == Array([[0.0, 0.31782087683677673, 0.4564354717731476, 0.5741692781448364, 0.6900655627250671], \
                                  [0.8164966106414795, 0.9682458639144897, 1.1715583801269531, 1.49071204662323, 2.1764285564422607]])).all() :
            print 'passed'
        else :
            print 'test_asin: case1, name=asin2 var failed'
    except :
        print 'test_asin: name=asin2 exception failed'
        print sys.exc_info()[0]
    try :
        acos1 = data.arange(0, 1, 0.1, [2, 5])
        acos1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        acos2 = data.arccos(acos1)
        if (acos2 == Array([[1.5707963267948966, 1.4706289041357137, 1.369438402962879, 1.26610366028297, 1.1592794742240087], \
                              [1.0471975511965979, 0.9272951881992895, 0.7953988468767829, 0.6435010889250692, 0.45102686649323265]])).all() :
            print 'passed'
        else :
            print 'test_acos: case1, name=acos2 value failed'
        if (acos2.err == Array([[0.0, 0.31782087683677673, 0.4564354717731476, 0.5741692781448364, 0.6900655627250671], \
                                  [0.8164966106414795, 0.9682458639144897, 1.1715583801269531, 1.49071204662323, 2.1764285564422607]])).all() :
            print 'passed'
        else :
            print 'test_acos: case1, name=acos2 var failed'
    except :
        print 'test_acos: name=acos2 exception failed'
        print sys.exc_info()[0]
    try :
        atan1 = data.arange(10, [2, 5])
        atan1.set_axes([data.arange(3), data.arange(4)], ['sx', 'stth'], ['mm', 'degrees'])
        atan2 = data.arctan(atan1)
        if (atan2 == Array([[0.0, 0.7853981633974483, 1.1071487177940904, 1.2490457723982544, 1.3258176636680326], \
                              [1.373400766945016, 1.4056476493802699, 1.4288992721907328, 1.446441332248135, 1.460139105621001]])).all() :
            print 'passed'
        else :
            print 'test_atan: case1, name=atan2 value failed'
        if (atan2.err == Array([[0.0, 0.5, 0.2828427255153656, 0.17320507764816284, 0.11764705926179886], \
                                  [0.08600261807441711, 0.06620242446660995, 0.052915025502443314, 0.04351426288485527, 0.03658536449074745]])).all() :
            print 'passed'
        else :
            print 'test_atan: case1, name=atan2 var failed'
    except :
        print 'test_atan: name=atan2 exception failed'
        print sys.exc_info()[0]

                
def suite():
    import sharedTest
    sharedTest.array = data
    sharedTest.AClass = Data
    sharedTest.suite()
    print '\n############ TEST DATA START ##############'
    test_new_data()
    test_var()
    test_axes()
    test_getitem()
    test_section_iter()
    test_get_section()
    test_get_slice()
    test_take()
    test_iter()
    test_get_max_min()
    test_fill()
    test_put()
    test_delete()
    test_append()
    test_column_stack()
    test_array_split()
    test_tile()
    test_add()
    test_sub()
    test_mul()
    test_div()
    test_pow()
    test_exp()
    test_log10()
    test_ln()
    test_sqrt()
    test_sin()
    print '############ TEST DATA END   ##############'
    
#suite()