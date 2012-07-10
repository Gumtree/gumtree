from jep import *
from java.util import Random
from org.gumtree.data.impl.utils import JUtils
from org.gumtree.data import DataType

def jdoubles(length) :
#    ns = JUtils.create1DJavaArray(DataType.INT, len(shape))
#    i = 0;
#    for val in shape :
#        ns[i] = val
#        i += 1
#    return JUtils.createJavaArray(DataType.DOUBLE, ns)
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