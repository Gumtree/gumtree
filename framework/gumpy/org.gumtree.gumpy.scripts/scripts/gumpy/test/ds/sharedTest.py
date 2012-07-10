#from nexus import array
from gumpy.nexus import *
import sys
array = None
AClass = None

###########################################################################
#    Test constructor from string
###########################################################################
def test_constructor():
    print 'TEST CONSTRUCTOR'
    try :
        crt1 = array.arange(12, [3, 4])
        pr = repr(crt1)
        crt2 = eval(pr)
        if crt2.tolist() == [[0, 1, 2, 3], [4, 5, 6, 7], [8, 9, 10, 11]] :
            print 'passed'
        else :
            print 'test_constructor: name=crt2, value failed'
    except :
        print 'test_constructor: exception thrown'
        print sys.exc_info()[0]


###########################################################################
#    Test concatenate
###########################################################################
def test_concatenate():
    print '\nTEST CONCATENATE'
    try :
        a = array.arange(12, [2, 2, 3])
        b = array.arange(12, 24, [2, 2, 3])
        c = array.arange(24, 36, [2, 2, 3])
        conc1 = array.concatenate((a, b, c), 0)
        if conc1.shape == [6, 2, 3] :
            print 'passed'
        else :
            print 'concatenate: name=conc1, axis=0, shape failed'
        if conc1.tolist() == [[[0, 1, 2], [3, 4, 5]], [[6, 7, 8], [9, 10, 11]], \
                    [[12, 13, 14], [15, 16, 17]], [[18, 19, 20], [21, 22, 23]], 
                    [[24, 25, 26], [27, 28, 29]], [[30, 31, 32], [33, 34, 35]]]:
            print 'passed'
        else :
            print 'concatenate: name=conc1 axis=0, value failed'
    except :
        print 'concatenate: axis=0, exception failed'
        print sys.exc_info()[0]
    
    try :
        conc2 = array.concatenate((a, b, c), 1)
        if conc2.shape == [2, 6, 3] :
            print 'passed'
        else :
            print 'concatenate: name=conc2, axis=1, shape failed'
        if conc2.tolist() == [[[0, 1, 2], [3, 4, 5], [12, 13, 14], [15, 16, 17], \
                    [24, 25, 26], [27, 28, 29]], [[6, 7, 8], [9, 10, 11], [18, 19, 20], 
                    [21, 22, 23], [30, 31, 32], [33, 34, 35]]]:
            print 'passed'
        else :
            print 'concatenate: name=conc2, axis=1, value failed'
    except :
        print 'concatenate: axis=1, exception failed'
        print sys.exc_info()[0]
    
    try :
        conc3 = array.concatenate((a, b, c), 2)
        if conc3.shape == [2, 2, 9] :
            print 'passed'
        else :
            print 'concatenate: name=conc3 axis=2, shape failed'
        if conc3.tolist() == [[[0, 1, 2, 12, 13, 14, 24, 25, 26], \
                    [3, 4, 5, 15, 16, 17, 27, 28, 29]], [[6, 7, 8, 18, 19, 20, 30, 31, 32], \
                    [9, 10, 11, 21, 22, 23, 33, 34, 35]]] :
            print 'passed'
        else :
            print 'concatenate: name=conc3 axis=2, value failed'
    except :
        print 'concatenate: axis=2, exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test iterator
###########################################################################
def test_iter() :
    print '\nTEST ITERATOR'
    try :
        vi1 = array.arange(24, [2, 3, 4])
        vit1 = vi1.item_iter()
        vi2 = []
        try :
            while True :
                vi2 += [vit1.next()]
        except StopIteration :
            pass
        if vi2 == [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, \
                   13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23] :
            print 'passed'
        else :
            print 'test_iter, name=vi2 : value failed'
        vit2 = iter(vi1)
        vi3 = []
        try :
            while True :
                vi3 += [vit2.next()]
        except StopIteration :
            pass
        vi4 = eval(repr(vi3))
        if [vi4[0].tolist(), vi4[1].tolist()] == [[[0, 1, 2, 3], \
                                                   [4, 5, 6, 7], \
                                                   [8, 9, 10, 11]], 
                                                   [[12, 13, 14, 15], \
                                                    [16, 17, 18, 19], \
                                                    [20, 21, 22, 23]]] :
            print 'passed'
        else :
            print 'test_iter, name=vi4 : value failed'

    except :
        print 'test_iter: exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test column_stack
###########################################################################
def test_column_statck() :
    print '\nTEST COLUMN_STACK'
    try :
        a = array.arange(6)
        b = array.arange(6, 12)
        c = array.arange(12, 18)
        cols = array.column_stack(a, b, c)
        if cols.shape == [6, 3] :
            print 'passed'
        else :
            print 'column_stack: name=cols shape failed'
        if cols.tolist() == [[0, 6, 12], [1, 7, 13], [2, 8, 14], [3, 9, 15], \
                                 [4, 10, 16], [5, 11, 17]] :
            print 'passed'
        else :
            print 'column_stack: name=cols value failed'
    except :
        print 'column_stack: exception failed'
        print sys.exc_info()[0]
    

###########################################################################
#    Test tolist
###########################################################################
def test_tolist():
    print '\nTEST TO_LIST'
    try :
        a = array.arange(48, [3, 2, 4, 2])
        if a.tolist() == [[[[0, 1], [2, 3], [4, 5], [6, 7]], [[8, 9], [10, 11], \
                    [12, 13], [14, 15]]], [[[16, 17], [18, 19], [20, 21], [22, 23]], \
                    [[24, 25], [26, 27], [28, 29], [30, 31]]], [[[32, 33], [34, 35], \
                    [36, 37], [38, 39]], [[40, 41], [42, 43], [44, 45], [46, 47]]]] :
            print 'passed'
        else :
            print 'tolist: name=a value failed'
    except :
        print 'tolist: exception failed'
        print sys.exc_info()[0]


###########################################################################
#    Test vstack
###########################################################################
def test_vstack():
    print '\nTEST VSTACK'
    try :
        vsa = AClass([1, 2])
        vsb = AClass([[3,4],[5,6]])
        vsc = array.vstack(vsa, vsb, vsb, vsa)
        if vsc.tolist() == [[1, 2], [3, 4], [5, 6], [3, 4], [5, 6], [1, 2]] :
            print 'passed'
        else :
            print 'vstack: name=vsc value failed'
    except :
        print 'vstack: exception failed'
        print sys.exc_info()[0]


###########################################################################
#    Test hstack
###########################################################################
def test_hstack():
    print '\nTEST HSTACK'
    try :
        hsa = AClass([[1], [2]])
        hsb = AClass([[3,4],[5,6]])
        hsc = array.hstack(hsa, hsb, hsb, hsa)
        if hsc.tolist() == [[1, 3, 4, 3, 4, 1], [2, 5, 6, 5, 6, 2]] :
            print 'passed'
        else :
            print 'hstack: name=hsc ndim>1, value failed'
        hsx = AClass([1, 2])
        hsy = AClass([3,4,5,6])
        hsz = array.hstack(hsx, hsy, hsy, hsx)
        if hsz.tolist() == [1, 2, 3, 4, 5, 6, 3, 4, 5, 6, 1, 2] :
            print 'passed'
        else :
            print 'hstack: name=hsz ndim=1, value failed'
    except :
        print 'hstack: exception failed'
        print sys.exc_info()[0]


###########################################################################
#    Test dstack
###########################################################################
def test_dstack():
    print '\nTEST DSTACK'
    try :
        ds1 = array.arange(12, [2, 3, 2])
        ds2 = array.arange(12, 30, [2, 3, 3])
        ds3 = array.dstack(ds1, ds2, ds2, ds1)
        if ds3.tolist() == [[[0, 1, 12, 13, 14, 12, 13, 14, 0, 1], [2, 3, 15, 16, \
                            17, 15, 16, 17, 2, 3], [4, 5, 18, 19, 20, 18, 19, 20, 4, 5]], \
                            [[6, 7, 21, 22, 23, 21, 22, 23, 6, 7], [8, 9, 24, 25, 26, 24, \
                            25, 26, 8, 9], [10, 11, 27, 28, 29, 27, 28, 29, 10, 11]]] :
            print 'passed'
        else :
            print 'dstack: name=ds3 ndim>2, value failed'
        dsa = AClass([[1], [2], [3]])
        dsb = AClass([[4], [5], [6]])
        dsc = array.dstack(dsa, dsb, dsb, dsa)
        if dsc.tolist() == [[[1, 4, 4, 1]], [[2, 5, 5, 2]], [[3, 6, 6, 3]]] :
            print 'passed'
        else :
            print 'dstack: name=dsc ndim=2, value failed'
        dsx = AClass([1, 2, 3])
        dsy = AClass([4, 5, 6])
        dsz = array.dstack(dsx, dsy, dsy, dsx)
        if dsz.tolist() == [[[1, 4, 4, 1], [2, 5, 5, 2], [3, 6, 6, 3]]] :
            print 'passed'
        else :
            print 'dstack: name=dsz ndim=1, value failed'
    except :
        print 'dstack: exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test array_split
###########################################################################
def test_array_split():
    print '\nTEST ARRAY_SPLIT'
    try :
        as1 = array.arange(8, [2, 4])
        as2 = array.array_split(as1, 2, axis = 0)
        if as2[0].tolist() == [[0, 1, 2, 3]] and as2[1].tolist() == [[4, 5, 6, 7]] :
            print 'passed'
        else :
            print 'array_split: name=as2 axis=0, value failed'
        as3 = array.array_split(as1, 4, axis = 1)
        if as3[0].tolist() == [[0], [4]] and as3[1].tolist() == [[1], [5]] \
                and as3[2].tolist() == [[2], [6]] and as3[3].tolist() == [[3], [7]]:
            print 'passed'
        else :
            print 'array_split: name=as3 axis=1, value failed'
        as4 = array.array_split(as1, [2,3], axis = 1)
        if as4[0].tolist() == [[0, 1], [4, 5]] \
                and as4[1].tolist() == [[2], [6]] and as4[2].tolist() == [[3], [7]]:
            print 'passed'
        else :
            print 'array_split: name=as4 list split, value failed'
    except :
        print 'array_split: exception failed'
        print sys.exc_info()[0]


###########################################################################
#    Test section iterator
###########################################################################
def test_section_iter():
    print '\nTEST SECTION_ITER'
    try :
        si = array.arange(48, (2, 4, 6))
        sia = si.section_iter([2,3])
        if sia.next().tolist() == [[0, 1, 2], [6, 7, 8]] :
            print 'passed'
        else :
            print 'section_iter: name=sia id=0, value failed'
        if sia.next().tolist() == [[3, 4, 5], [9, 10, 11]] :
            print 'passed'
        else :
            print 'section_iter: name=sia id=1, value failed'
        if sia.next().tolist() == [[12, 13, 14], [18, 19, 20]] :
            print 'passed'
        else :
            print 'section_iter: name=sia id=2, value failed'
        if sia.next().tolist() == [[15, 16, 17], [21, 22, 23]] :
            print 'passed'
        else :
            print 'section_iter: name=sia id=3, value failed'
        if sia.next().tolist() == [[24, 25, 26], [30, 31, 32]] :
            print 'passed'
        else :
            print 'section_iter: name=sia id=4, value failed'
        if sia.next().tolist() == [[27, 28, 29], [33, 34, 35]] :
            print 'passed'
        else :
            print 'section_iter: name=sia id=5, value failed'
        if sia.next().tolist() == [[36, 37, 38], [42, 43, 44]] :
            print 'passed'
        else :
            print 'section_iter: name=sia id=6, value failed'
        if sia.next().tolist() == [[39, 40, 41], [45, 46, 47]] :
            print 'passed'
        else :
            print 'section_iter: name=sia id=7, value failed'
        try :
            sia.next()
            print 'section_iter: name=sia raise StopIteration except failed'
        except StopIteration :
            print 'passed'
    except :
        print 'section_iter: exception failed'
        print sys.exc_info()[0]


###########################################################################
#    Test tile
###########################################################################
def test_tile():
    print '\nTEST TILE'
    try :
        t1 = AClass([10,20])
        t2 = array.tile(t1, (3, 2))
        if t2.tolist() == [[10, 20, 10, 20], [10, 20, 10, 20], [10, 20, 10, 20]] :
            print 'passed'
        else :
            print 'tile: case1, name=t2 value failed'
        t3 = AClass([0, 1, 2])
        t4 = array.tile(t3, 2)
        if t4.tolist() == [0, 1, 2, 0, 1, 2] :
            print 'passed'
        else :
            print 'tile: case2, name=t4 value failed'
        t5 = array.tile(t3, (2, 1, 2))
        if t5.tolist() == [[[0, 1, 2, 0, 1, 2]], [[0, 1, 2, 0, 1, 2]]] :
            print 'passed'
        else :
            print 'tile: case3, name=t5 value failed'
        t6 = AClass([[1, 2], [3, 4]])
        t7 = array.tile(t6, (2, 1))
        if t7.tolist() == [[1, 2], [3, 4], [1, 2], [3, 4]] :
            print 'passed'
        else :
            print 'tile: case4, name=t7 value failed'
        t8 = array.tile(42.0, (3, 2))
        if t8.tolist() == [[42.0, 42.0], [42.0, 42.0], [42.0, 42.0]] :
            print 'passed'
        else :
            print 'tile: case5, name=t8 value failed'
        t9 = array.tile([[1,2],[4,8]], (3, 2))
        if t9.tolist() == [[1, 2, 1, 2], [4, 8, 4, 8], [1, 2, 1, 2], [4, 8, 4, 8], \
                           [1, 2, 1, 2], [4, 8, 4, 8]] :
            print 'passed'
        else :
            print 'tile: case6, name= value failed'
    except :
        print 'tile: exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test __getitem__
###########################################################################
def test_getitem():
    print '\nTEST GETITEM'
    try :
        gi0 = array.arange(5)
        gi1 = gi0[0:3]
        if gi1.tolist() == [0, 1, 2] :
            print 'passed'
        else :
            print 'test_getitem: name=gi1, value failed'
        gi2 = gi0[-3:-1]
        if gi2.tolist() == [2, 3] :
            print 'passed'
        else :
            print 'test_getitem: name=gi2, value failed'
        gi3 = gi0[-4:-2]
        if gi3.tolist() == [1, 2] :
            print 'passed'
        else :
            print 'test_getitem: name=gi3, value failed'
        gi4 = gi0[-3:]
        if gi4.tolist() == [2, 3, 4] :
            print 'passed'
        else :
            print 'test_getitem: name=gi4, value failed'
        gi5 = gi0[-4:-3]
        if gi5.tolist() == [1] :
            print 'passed'
        else :
            print 'test_getitem: name=gi5, value failed'
        gi6 = gi0[-3:-4]
        if gi6.tolist() == [] :
            print 'passed'
        else :
            print 'test_getitem: name=gi6, value failed'
        gi7 = gi0[1:8]
        if gi7.tolist() == [1, 2, 3, 4] :
            print 'passed'
        else :
            print 'test_getitem: name=gi7, value failed'
        gi8 = gi0[8:10]
        if gi8.tolist() == [] :
            print 'passed'
        else :
            print 'test_getitem: name=gi8, value failed'
        gi9 = gi0[-6:-4]
        if gi9.tolist() == [0] or gi9.tolist() == []:
            print 'passed'
        else :
            print 'test_getitem: name=gi9, value failed'
        gi10 = gi0[-4:-2]
        if gi10.tolist() == [1, 2] :
            print 'passed'
        else :
            print 'test_getitem: name=gi10, value failed'
        
        gia = array.arange(48, [3, 2, 4, 2])
        gib = gia[0]
        gic = gia[2]
        gid = gia[-1]
        gie = gia[1:3]
        gif = gia[2:]
        gig = gia[1:3, :, 1:, 1]
        gih = gia[1:3, :, 1:]
        gii = gia[2, :, 1:]
        gij = gia[2, :, -1, 1]
        gik = gia[1, -1, -1, 1]
        if gib.tolist() == [[[0, 1], [2, 3], [4, 5], [6, 7]], [[8, 9], [10, 11], \
                            [12, 13], [14, 15]]] :
            print 'passed'
        else :
            print 'test_getitem: name=gib ndim=0 slice=0, value failed'
        if gic.tolist() == [[[32, 33], [34, 35], [36, 37], [38, 39]], [[40, 41], \
                            [42, 43], [44, 45], [46, 47]]] :
            print 'passed'
        else :
            print 'test_getitem: name=gic ndim=0 slice=2, value failed'
        if gid.tolist() == [[[32, 33], [34, 35], [36, 37], [38, 39]], [[40, 41], \
                            [42, 43], [44, 45], [46, 47]]] :
            print 'passed'
        else :
            print 'test_getitem: name=gid ndim=0 slice=-1, value failed'
        if gie.tolist() == [[[[16, 17], [18, 19], [20, 21], [22, 23]], [[24, 25], \
                            [26, 27], [28, 29], [30, 31]]], [[[32, 33], [34, 35], \
                            [36, 37], [38, 39]], [[40, 41], [42, 43], [44, 45], \
                            [46, 47]]]] :
            print 'passed'
        else :
            print 'test_getitem: name=gie ndim=0 slice=1:3, value failed'
        if gif.tolist() == [[[[32, 33], [34, 35], [36, 37], [38, 39]], [[40, 41], \
                            [42, 43], [44, 45], [46, 47]]]] :
            print 'passed'
        else :
            print 'test_getitem: name=gif ndim=0 slice=2:, value failed'
        if gig.tolist() == [[[[19], [21], [23]], [[27], [29], [31]]], [[[35], [37], \
                            [39]], [[43], [45], [47]]]] :
            print 'passed'
        else :
            print 'test_getitem: name=gig ndim=0 slice=[1:3, :, 1:, 1], value failed'
        if gih.tolist() == [[[[18, 19], [20, 21], [22, 23]], [[26, 27], [28, 29], \
                            [30, 31]]], [[[34, 35], [36, 37], [38, 39]], [[42, 43], \
                            [44, 45], [46, 47]]]] :
            print 'passed'
        else :
            print 'test_getitem: name=gih ndim=0 slice=[1:3, :, 1:], value failed'
        if gii.tolist() == [[[34, 35], [36, 37], [38, 39]], [[42, 43], [44, 45], \
                            [46, 47]]] :
            print 'passed'
        else :
            print 'test_getitem: name=gii ndim=0 slice=[2, :, 1:], value failed'
        if gij.tolist() == [[[39]], [[47]]] :
            print 'passed'
        else :
            print 'test_getitem: name=gij ndim=0 slice=[2, :, -1, 1], value failed'
        if gik == 31 :
            print 'passed'
        else :
            print 'test_getitem: name=gik ndim=0 index=[1, -1, -1, 1], value failed'
    except :
        print 'test_getitem: exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test max and min
###########################################################################
def test_max_min():
    print '\nTEST MAX & MIN'
    try :
        max1 = array.arange(48, [3, 4, 4])
        max2 = max1.max()
        max3 = max1.max(1)
        if max2 == 47 :
            print 'passed'
        else :
            print 'max: name=max2 axis=None, value failed'
        if max3.tolist() == [35, 39, 43, 47] :
            print 'passed'
        else :
            print 'max: name=max3 axis=1, value failed'
        min1 = max1.min(0)
        if min1.tolist() == [0, 16, 32] :
            print 'passed'
        else :
            print 'min: name=min1 axis=0, value failed'
    except :
        print 'max and min: exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test delete
###########################################################################
def test_delete():
    print '\nTEST DELETE'
    try :
        del1 = array.arange(81, [9, 9])
        del2 = array.delete(del1, array.s_[::2], 1)
        del3 = array.delete(del1, array.s_[1:7], 0)
        if del2.tolist() == [[1, 3, 5, 7], [10, 12, 14, 16], [19, 21, 23, 25], \
                             [28, 30, 32, 34], [37, 39, 41, 43], [46, 48, 50, 52], \
                             [55, 57, 59, 61], [64, 66, 68, 70], [73, 75, 77, 79]] :
            print 'passed'
        else :
            print 'delete: name=del2 axis=1, obj=slice, value failed'
        if del3.tolist() == [[0, 1, 2, 3, 4, 5, 6, 7, 8], \
                             [63, 64, 65, 66, 67, 68, 69, 70, 71], \
                             [72, 73, 74, 75, 76, 77, 78, 79, 80]] :
            print 'passed'
        else :
            print 'delete: name=del3 axis=0, obj=slice, value failed'
        del4 = array.arange(6)
        del5 = array.delete(del4, 3)
        if del5.tolist() == [0, 1, 2, 4, 5] :
            print 'passed'
        else :
            print 'delete single index: name=del5 value failed'
        del6 = array.arange(24, [2, 6, 2])
        del7 = array.delete(del6, [1, 2, 5], 1)
        if del7.tolist() == [[[0, 1], [6, 7], [8, 9]], [[12, 13], [18, 19], [20, 21]]] :
            print 'passed'
        else :
            print 'delete list of items: name=del7 value failed'
        del8 = array.delete(del6, [1, 2, 5])
        if del8.tolist() == [0, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23] :
            print 'passed'
        else :
            print 'delete list of items in flat: name=del8 value failed'
    except :
        print 'delete: exception failed'
        print sys.exc_info()[0]

###########################################################################
#    Test take
###########################################################################
def test_take():
    print '\nTEST TAKE'
    try :
        take1 = array.arange(81, [9, 9])
        take2 = take1.take(array.s_[::2], 1)
        take3 = take1.take(array.s_[1:4], 0)
        if take2.tolist() == [[0, 2, 4, 6, 8], [9, 11, 13, 15, 17], \
                              [18, 20, 22, 24, 26], [27, 29, 31, 33, 35], \
                              [36, 38, 40, 42, 44], [45, 47, 49, 51, 53], \
                              [54, 56, 58, 60, 62], [63, 65, 67, 69, 71], \
                              [72, 74, 76, 78, 80]] :
            print 'passed'
        else :
            print 'take: name=take2 axis=1, obj=slice, value failed'
        if take3.tolist() == [[9, 10, 11, 12, 13, 14, 15, 16, 17], \
                              [18, 19, 20, 21, 22, 23, 24, 25, 26], \
                              [27, 28, 29, 30, 31, 32, 33, 34, 35]] :
            print 'passed'
        else :
            print 'take: name=take3 axis=0, obj=slice, value failed'
        take4 = array.arange(6)
        take5 = take4.take([3,5])
        if take5.tolist() == [3, 5] :
            print 'passed'
        else :
            print 'take single index: name=take5 value failed'
        take6 = array.arange(24, [2, 6, 2])
        take7 = take6.take([1, 2, 5], 1)
        if take7.tolist() == [[[2, 3], [4, 5], [10, 11]], [[14, 15], [16, 17], [22, 23]]] :
            print 'passed'
        else :
            print 'take list of items: name=take7 value failed'
        take8 = take6.take([1, 2, 5])
        if take8.tolist() == [1, 2, 5] :
            print 'passed'
        else :
            print 'take list of items in flat: name=take8 value failed'
    except :
        print 'take: exception failed'
        print sys.exc_info()[0]
        
###########################################################################
#    Test put
###########################################################################
def test_put():
    print '\nTEST PUT'
    try :
        put1 = array.arange(12, [3, 4])
        put1.put(array.s_[::2], 1)
        if put1.tolist() == [[1, 1, 1, 3], [1, 5, 1, 7], [1, 9, 1, 11]] :
            print 'passed'
        else :
            print 'put: name=put1, value failed'
        put1.put(array.s_[1:7], [2, 3])
        if put1.tolist() == [[1, 2, 3, 2], [3, 2, 3, 7], [1, 9, 1, 11]] :
            print 'passed'
        else :
            print 'put: name=put1, value failed'
        put2 = array.arange(6)
        put2.put([3,5], [1, 2, 3])
        if put2.tolist() == [0, 1, 2, 1, 4, 2] :
            print 'passed'
        else :
            print 'put: name=put2 value failed'
        put3 = array.arange(24, [2, 6, 2])
        put3.put([1, 2, 5, 9], [1, 3])
        if put3.tolist() == [[[0, 1], [3, 3], [4, 1], [6, 7], [8, 3], [10, 11]], \
                             [[12, 13], [14, 15], [16, 17], [18, 19], [20, 21], [22, 23]]] :
            print 'passed'
        else :
            print 'put: name=put3 value failed'
        put4 = array.arange(4, [2, 2])
        put4.put(1, [7])
        if put4.tolist() == [[0, 7], [2, 3]] :
            print 'passed'
        else :
            print 'put: name=put4 value failed'
    except :
        print 'put: exception failed'
        print sys.exc_info()[0]
        
###########################################################################
#    Test equal
###########################################################################
def test_equal():
    print '\nTEST EQUAL'
    try :
        eq1 = array.arange(12, [3, 4])
        eq2 = array.arange(12, [3, 4])
        eq3 = eq1 == eq2
        if eq3.tolist() == [[True, True, True, True], [True, True, True, True], [True, True, True, True]] :
            print 'passed'
        else :
            print 'equal: name=eq3, value failed'
        eq4 = eq1 != eq2
        if eq4.tolist() == [[False, False, False, False], [False, False, False, False], 
                             [False, False, False, False]] :
            print 'passed'
        else :
            print 'equal: name=eq4, value failed'
        eq5 = eq1 == 3
        if eq5.tolist() == [[False, False, False, True], [False, False, False, False], 
                             [False, False, False, False]] :
            print 'passed'
        else :
            print 'equal: name=eq5, value failed'
        eq6 = eq1 != 3
        if eq6.tolist() == [[True, True, True, False], [True, True, True, True], [True, True, True, True]] :
            print 'passed'
        else :
            print 'equal: name=eq6, value failed'
    except :
        print 'put: exception failed'
        print sys.exc_info()[0]


def suite():
    print '############ SHARED TEST START ##############'
    test_constructor()
    test_concatenate()
    test_column_statck()
    test_iter()
    test_tolist()
    test_vstack()
    test_hstack()
    test_dstack()
    test_array_split()
    test_section_iter()
    test_tile()
    test_getitem()
    test_max_min()
    test_delete()
    test_take()
    test_put()
    test_equal()
    print '############ SHARED TEST END   ##############'
    
#suite()