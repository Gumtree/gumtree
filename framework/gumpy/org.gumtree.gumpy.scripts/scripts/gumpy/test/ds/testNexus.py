from gumpy.nexus import *

# create a dataset instance with given shape
ds = instance([3, 4, 4])

# fill data by slicing
for block in ds :
    for row in block :
        row.copy_from(arange(4))

# math calculation 
ds += arange(48, [3, 4, 4]) * 2.0

# array manipulation 
dss = split(ds, 2, axis = 1)
ds = dss[0]

# interactive with python list
ds[0] *= sin(asarray([[1, 2, 2, 3], [2, 1, 3, 2]]))

# construct from repr
new_ds = eval(repr(ds))

print new_ds