from au.gov.ansto.bragg.nbi.ui.scripting.pyobj import ScriptParameter
from au.gov.ansto.bragg.nbi.ui.scripting.pyobj import ScriptAction
from au.gov.ansto.bragg.nbi.ui.scripting.pyobj import ScriptObjectGroup

class Par:
    
    __model__ = None
    def __init__(self, ptype, default = None, options = None, command = None):
        self.__par__ = ScriptParameter()
        self.__par__.setTypeName(ptype)
        self.__par__.setValue(default)
        self.__par__.setOptions(options)
        self.__par__.setCommand(command)
        if not Par.__model__ is None:
            Par.__model__.addControl(self.__par__)
    
    def __getattr__(self, name):
        if name == 'name' :
            return self.__par__.getName()
        elif name == 'value' :
            return self.__par__.getValue()
        elif name == 'options' :
            return self.__par__.getOptions()
        elif name == 'command' :
            return self.__par__.getCommand()
        elif name == 'type' :
            return self.__par__.getType()
        elif name != '__par__' :
            return self.__par__.getProperty(name)
            
    
    def __setattr__(self, name, value):
        if name == 'name' :
            self.__par__.setName(value)
        elif name == 'value' :
            self.__par__.setValue(value)
        elif name == 'options' :
            self.__par__.setOptions(value)
        elif name == 'command' :
            self.__par__.setCommand(value)
        elif name == '__par__' :
            self.__dict__[name] = value
        else :
            self.__par__.setProperty(name, str(value))
            
    def __str__(self):
        return 'Par_' + self.name
    
    def __repr__(self):
        return 'au.gov.ansto.bragg.wombat.ui.script.pyobj.ScriptParameter'
    
def is_par(__name__to__test__):
    return eval('isinstance(' + __name__to__test__ + ', Par)')

def set_name_to_par(__name__to__test__):
    eval(__name__to__test__ + '.name = "' + __name__to__test__ + '"')
    
def set_name_to_all_pars():
    for name in globals().keys() :
        if is_par(name) :
            set_name_to_par(name)
            
class Act:
    __model__ = None
    def __init__(self, command, text = 'Run'):
        self.__act__ = ScriptAction()
        self.__act__.setCommand(command)
        self.__act__.setText(text)
        if not Act.__model__ is None:
            Act.__model__.addControl(self.__act__)
    
    def __getattr__(self, name):
        if name == 'name' :
            return self.__act__.getName()
        elif name == 'text' :
            return self.__act__.getText()
        elif name == 'command' :
            return self.__act__.getCommand()
        elif name != '__act__' :
            return self.__act__.getProperty(name)
    
    def __setattr__(self, name, value):
        if name == 'name' :
            self.__act__.setName(value)
        elif name == 'text' :
            self.__act__.setText(value)
        elif name == 'command' :
            self.__act__.setCommand(value)
        elif name == '__act__' :
            self.__dict__[name] = value
        else :
            self.__act__.setProperty(name, str(value))
            
    def __str__(self):
        return 'Act_' + self.name
    
    def __repr__(self):
        return 'au.gov.ansto.bragg.wombat.ui.script.pyobj.ScriptAction'
    
    def set_running_status(self):
        self.__act__.setBusyStatus()
        
    def set_done_status(self):
        self.__act__.setDoneStatus()
    
    def set_error_status(self):
        self.__act__.setErrorStatus()

    def set_interrupt_status(self):
        self.__act__.setInterruptStatus()
        
    def __run__(self):
        self.set_running_status()
#        try:
#            exec(str(self.command))
#        except:
#            print 'failed to run the command: ' + self.name
        exec(str(self.command))
        self.set_done_status()
        
def is_act(__name__to__test__):
    return eval('isinstance(' + __name__to__test__ + ', Act)')

def set_name_to_all_acts():
    for name in globals().keys() :
        if is_act(name) :
            set_name_to_par(name)
                    
class Group():
    __model__ = None
    def __init__(self, name):
        self.__group__ = ScriptObjectGroup(name)
        if not Group.__model__ is None:
            Group.__model__.addControl(self.__group__)
        
    def add(self, *objs):
        for obj in objs :
            if isinstance(obj, Par) :
                self.__group__.addObject(obj.__par__)
            elif isinstance(obj, Act) :
                self.__group__.addObject(obj.__act__)
        
    def remove(self, obj):
        self.__group__.removeObject(obj)
        
    def __getattr__(self, name):
        if name == 'name' :
            return self.__group__.getName()
        elif name != '__group__' :
            return self.__group__.getProperty(name)
            
    def __setattr__(self, name, value):
        if name == 'name' :
            self.__group__.setName(value)
        elif name == '__group__' :
            self.__dict__[name] = value
        else :
            self.__group__.setProperty(name, str(value))
                        
    def __str__(self):
        return 'Group_' + self.name
    
    def __repr__(self):
        return 'au.gov.ansto.bragg.wombat.ui.script.pyobj.ScriptObjectGroup'
        