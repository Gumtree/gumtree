'''
    @author: nxi
'''

class NotSupportedError(Exception):
    def __init__(self, text = 'not supported', e = None):
        Exception(text, e)
        
class NotImplementedError(Exception):
    def __init__(self, text = 'not implemented', e = None):
        Exception(text, e)
        
class IllegalArgumentError(Exception):
    def __init__(self, text = 'illegal argument', e = None):
        Exception(text, e)
        
class AxisError(Exception):
    def __init__(self, text = 'axis error', e = None):
        Exception(text, e)
