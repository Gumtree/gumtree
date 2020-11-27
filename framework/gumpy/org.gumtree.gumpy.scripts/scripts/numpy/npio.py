import numpy as np
import pickle

def load(file, mmap_mode=None, allow_pickle=None, fix_imports=None, encoding='ASCII'):
    ''' Load arrays or pickled objects from .npy, .npz or pickled files.
    
        Parameters
    
            file : file-like object, string, or pathlib.Path
                The file to read. File-like objects must support the seek() and read() methods. Pickled files require that the file-like object support the readline() method as well.
    
            mmap_mode : not supported
    
            allow_pickle : not supported
    
            fix_imports : not supported
    
            encoding : not supported
            
        Returns
    
            result : array, tuple, dict, etc.
                Data stored in the file. For .npz files, the returned instance of 
                NpzFile class must be closed to avoid leaking file descriptors.
    
        Raises
    
            IOError
                If the input file does not exist or cannot be read.
    
            ValueError
                The file contains an object array, but allow_pickle=False given.
    
    '''
    try :
        f = open(file, 'rb')
        r = pickle.load(f)
        return eval(r)
    finally:
        f.close()


def dumps(objs):
    ''' Returns the pickle of the array as a string. pickle.loads or numpy.loads 
        will convert the string back to an array. 
        
        Parameters:    None
    '''
    if type(objs) is Tuple:
        r = '('
        for o in objs:
            o = asanarray(o)
            r += o.dumps() + ','
        r += ')'
        return r
    else:
        objs = asanyarray(objs)
        return objs.dumps()
        
def loads(input):
    ''' Covert pickle string to array. See numpy.dumps for how to pickle an array.
        
        Parameters:    None
    '''
    return eval(input)

def save(file, arr, allow_pickle=True, fix_imports=True):
    ''' Save an array to a binary file in NumPy .npy format.
    
        Parameters
    
            file : file, str, or pathlib.Path
                File or filename to which the data is saved. If file is a file-object, 
                then the filename is unchanged. If file is a string or Path, a .npy 
                extension will be appended to the filename if it does not already have 
                one.
                
            arr : array_like
                Array data to be saved.
            
            allow_pickle : not supported
    
            fix_imports : not supported
    
    
    '''
    arr = np.asanyarray(arr)
    arr.dump(file)

def savez(file, *args, **kwds):
    ''' Save several arrays into a single file in pkl format.
    
        If arguments are passed in with no keywords, the corresponding variable names, 
        in the .pkl file, are 'arr_0', 'arr_1', etc. If keyword arguments are given, 
        the corresponding variable names, in the .npz file will match the keyword names.
    
        Parameters
    
            file : str or file
    
                Either the filename (string) or an open file (file-like object) where 
                the data will be saved. If file is a string or a Path, the .pkl extension 
                will be appended to the filename if it is not already there.
                
            args : Arguments, optional
                Arrays to save to the file. Since it is not possible for Python to know 
                the names of the arrays outside savez, the arrays will be saved with 
                names "arr_0", "arr_1", and so on. These arguments can be any expression.
            
            kwds : Keyword arguments, optional
                Arrays to save to the file. Arrays will be saved in the file with the keyword names.
    
        Returns
    
            None
    
    '''   
    pass

