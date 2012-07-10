from java.util import Random
from org.gumtree.data import DataType
is_jython = True
try :
    from jarray import zeros
    print 'Jython'
except :
    print 'CPython'
    is_jython = False
if not is_jython:
    from jep import *
    def jdoubles(length) :
        return jarray(length, JDOUBLE_ID)

    def jints(length) :
        return jarray(length, JINT_ID)
    
    def jbooleans(length):
        return jarray(length, JBOOLEAN_ID)
    
    def jbytes(length):
        return jarray(length, JBYTE_ID)
    
    def jchars(length):
        return jarray(length, JCHAR_ID)
    
    def jfloats(length):
        return jarray(length, JFLOAT_ID)
    
    def jlongs(length):
        return jarray(length, JLONG_ID)
    
    def jshorts(length):
        return jarray(length, JSHORT_ID)
    
    def jdoublecopy(seq):
        ja = jdoubles(len(seq))
        i = 0
        for val in seq :
            ja[i] = float(val)
            i += 1
        return ja
    
    def jintcopy(seq):
        ja = jints(len(seq))
        i = 0
        for val in seq :
            ja[i] = int(val)
            i += 1
        return ja
    
    def jlongcopy(seq):
        ja = jlongs(len(seq))
        i = 0
        for val in seq :
            ja[i] = long(val)
            i += 1
        return ja
    
    def jfloatcopy(seq):
        ja = jfloats(len(seq))
        i = 0
        for val in seq :
            ja[i] = float(val)
            i += 1
        return ja
    
    def jbytecopy(seq):
        ja = jbytes(len(seq))
        i = 0
        for val in seq :
            ja[i] = val
            i += 1
        return ja
    
    def jcharcopy(seq):
        ja = jchars(len(seq))
        i = 0
        for val in seq :
            ja[i] = val
            i += 1
        return ja
    
    def jbooleancopy(seq):
        ja = jbooleans(len(seq))
        i = 0
        for val in seq :
            ja[i] = val
            i += 1
        return ja
    
    def jshortcopy(seq):
        ja = jshorts(len(seq))
        i = 0
        for val in seq :
            ja[i] = val
            i += 1
        return ja

else :
    from jarray import zeros as jarray
    
    def jdoubles(length) :
        return jarray(length, 'd')
    
    def jints(length) :
        return jarray(length, 'i')
    
    def jbooleans(length):
        return jarray(length, 'z')
    
    def jbytes(length):
        return jarray(length, 'b')
    
    def jchars(length):
        return jarray(length, 'c')
    
    def jfloats(length):
        return jarray(length, 'f')
    
    def jlongs(length):
        return jarray(length, 'l')
    
    def jshorts(length):
        return jarray(length, 'h')
    
    def jdoublecopy(seq):
        ja = jdoubles(len(seq))
        i = 0
        for val in seq :
            ja[i] = float(val)
            i += 1
        return ja
    
    def jintcopy(seq):
        ja = jints(len(seq))
        i = 0
        for val in seq :
            ja[i] = int(val)
            i += 1
        return ja
    
    def jlongcopy(seq):
        ja = jlongs(len(seq))
        i = 0
        for val in seq :
            ja[i] = long(val)
            i += 1
        return ja
    
    def jfloatcopy(seq):
        ja = jfloats(len(seq))
        i = 0
        for val in seq :
            ja[i] = float(val)
            i += 1
        return ja
    
    def jbytecopy(seq):
        ja = jbytes(len(seq))
        i = 0
        for val in seq :
            ja[i] = val
            i += 1
        return ja
    
    def jcharcopy(seq):
        ja = jchars(len(seq))
        i = 0
        for val in seq :
            ja[i] = val
            i += 1
        return ja
    
    def jbooleancopy(seq):
        ja = jbooleans(len(seq))
        i = 0
        for val in seq :
            ja[i] = val
            i += 1
        return ja
    
    def jshortcopy(seq):
        ja = jshorts(len(seq))
        i = 0
        for val in seq :
            ja[i] = val
            i += 1
        return ja

def get_inf():
    if is_jython :
        return float('inf')
    else :
        return 1e30000

def get_nan():
    if is_jython :
        return float('nan')
    else :
        return 1e30000 * 0