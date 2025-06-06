from au.gov.ansto.bragg.nbi.scripting import ScriptParameter
from au.gov.ansto.bragg.nbi.scripting import ScriptAction
from au.gov.ansto.bragg.nbi.scripting import ScriptObjectGroup
from au.gov.ansto.bragg.nbi.scripting import ScriptObjectTab

class Par:
    
    def __init__(self, model, ptype, default = None, options = None, command = None):
        self.__par__ = ScriptParameter()
        self.__par__.setTypeName(ptype)
        self.__par__.setValue(default)
        self.__par__.setOptions(options)
        self.__par__.setCommand(command)
        self.__model__ = model
        if not model is None:
            model.addControl(self.__par__)
    
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
            return str(self.__par__.getType())
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
        elif name == '__par__' or name == '__model__' :
            self.__dict__[name] = value
        else :
            self.__par__.setProperty(name, str(value))
            
    def moveBeforeObject(self, obj):
        if isinstance(obj, Par):
            tgt = obj.__par__
        elif isinstance(obj, Act):
            tgt = obj.__act__
        elif isinstance(obj, Group):
            tgt = obj.__group__
        elif isinstance(obj, Tab):
            tgt = obj.__tab__
        else:
            raise Exception, 'Illegal type: target object must be a PyScript object'
        if not self.__model__ is None:
            self.__model__.moveObject1BeforeObject2(self.__par__, tgt)

    def moveAfterObject(self, obj):
        if isinstance(obj, Par):
            tgt = obj.__par__
        elif isinstance(obj, Act):
            tgt = obj.__act__
        elif isinstance(obj, Group):
            tgt = obj.__group__
        elif isinstance(obj, Tab):
            tgt = obj.__tab__
        else:
            raise Exception, 'Illegal type: target object must be a PyScript object'
        if not self.__model__ is None:
            self.__model__.moveObject1AfterObject2(self.__par__, tgt)
            
    def __str__(self):
        return 'Par_' + self.name
    
    def __repr__(self):
        return 'au.gov.ansto.bragg.wombat.ui.script.pyobj.ScriptParameter'
    
    def dispose(self):
        if not self.__model__ is None:
            self.__model__.removeControl(self.__par__)
    
def is_par(__name__to__test__):
    return eval('isinstance(' + __name__to__test__ + ', Par)')

def set_name_to_par(__name__to__test__):
    eval(__name__to__test__ + '.name = "' + __name__to__test__ + '"')
    
def set_name_to_all_pars():
    for name in globals().keys() :
        if is_par(name) :
            set_name_to_par(name)
            
class Act:
    def __init__(self, model, command, text = 'Run'):
        self.__act__ = ScriptAction()
        self.__act__.setCommand(command)
        self.__act__.setText(text)
        self.__model__ = model
        if not model is None:
            model.addControl(self.__act__)
    
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
        elif name == '__act__' or name == '__model__' :
            self.__dict__[name] = value
        else :
            self.__act__.setProperty(name, str(value))
            
    def __str__(self):
        return 'Act_' + self.name
    
    def __repr__(self):
        return 'au.gov.ansto.bragg.wombat.ui.script.pyobj.ScriptAction'

    def moveBeforeObject(self, obj):
        if isinstance(obj, Par):
            tgt = obj.__par__
        elif isinstance(obj, Act):
            tgt = obj.__act__
        elif isinstance(obj, Group):
            tgt = obj.__group__
        elif isinstance(obj, Tab):
            tgt = obj.__tab__
        else:
            raise Exception, 'Illegal type: target object must be a PyScript object'
        if not self.__model__ is None:
            self.__model__.moveObject1BeforeObject2(self.__act__, tgt)

    def moveAfterObject(self, obj):
        if isinstance(obj, Par):
            tgt = obj.__par__
        elif isinstance(obj, Act):
            tgt = obj.__act__
        elif isinstance(obj, Group):
            tgt = obj.__group__
        elif isinstance(obj, Tab):
            tgt = obj.__tab__
        else:
            raise Exception, 'Illegal type: target object must be a PyScript object'
        if not self.__model__ is None:
            self.__model__.moveObject1AfterObject2(self.__act__, tgt)
            
    def set_running_status(self):
        self.__act__.setBusyStatus()
        
    def set_done_status(self):
        self.__act__.setDoneStatus()
    
    def set_error_status(self):
        self.__act__.setErrorStatus()

    def set_interrupt_status(self):
        self.__act__.setInterruptStatus()
        
    def clear_status(self):
        self.__act__.clearStatus()
        
    def __run__(self):
        self.set_running_status()
#        try:
#            exec(str(self.command))
#        except:
#            print 'failed to run the command: ' + self.name
        exec(str(self.command))
        self.set_done_status()
        
    def dispose(self):
        if not self.__model__ is None:
            self.__model__.removeControl(self.__act__)

def is_act(__name__to__test__):
    return eval('isinstance(' + __name__to__test__ + ', Act)')

def set_name_to_all_acts():
    for name in globals().keys() :
        if is_act(name) :
            set_name_to_par(name)
                    
class Group():
    def __init__(self, model, name):
        self.__group__ = ScriptObjectGroup(name)
        self.__model__ = model
        if not model is None:
            model.addControl(self.__group__)
        
    def add(self, *objs):
        for obj in objs :
            if isinstance(obj, Par) :
                self.__group__.addObject(obj.__par__)
            elif isinstance(obj, Act) :
                self.__group__.addObject(obj.__act__)
            elif isinstance(obj, Group) :
                self.__group__.addObject(obj.__group__)
            elif isinstance(obj, Tab) :
                self.__group__.addObject(obj.__tab__)
        
    def insert(self, idx, *objs):
        for obj in objs:
            if isinstance(obj, Par) :
                self.__group__.insertObject(idx, obj.__par__)
            elif isinstance(obj, Act) :
                self.__group__.insertObject(idx, obj.__act__)
            elif isinstance(obj, Group) :
                self.__group__.insertObject(idx, obj.__group__)
            elif isinstance(obj, Tab) :
                self.__group__.insertObject(idx, obj.__tab__)
            idx += 1

    def remove(self, *objs):
        for obj in objs:
            if isinstance(obj, Par) :
                self.__group__.removeObject(obj.__par__)
            elif isinstance(obj, Act) :
                self.__group__.removeObject(obj.__act__)
            elif isinstance(obj, Group) :
                self.__group__.removeObject(obj.__group__)
            elif isinstance(obj, Tab) :
                self.__group__.removeObject(obj.__tab__)
        
    def moveBeforeObject(self, obj):
        if isinstance(obj, Par):
            tgt = obj.__par__
        elif isinstance(obj, Act):
            tgt = obj.__act__
        elif isinstance(obj, Group):
            tgt = obj.__group__
        elif isinstance(obj, Tab):
            tgt = obj.__tab__
        else:
            raise Exception, 'Illegal type: target object must be a PyScript object'
        if not self.__model__ is None:
            self.__model__.moveObject1BeforeObject2(self.__group__, tgt)

    def moveAfterObject(self, obj):
        if isinstance(obj, Par):
            tgt = obj.__par__
        elif isinstance(obj, Act):
            tgt = obj.__act__
        elif isinstance(obj, Group):
            tgt = obj.__group__
        elif isinstance(obj, Tab):
            tgt = obj.__tab__
        else:
            raise Exception, 'Illegal type: target object must be a PyScript object'
        if not self.__model__ is None:
            self.__model__.moveObject1AfterObject2(self.__group__, tgt)
        
    def __getattr__(self, name):
        if name == 'name' :
            return self.__group__.getName()
        elif name != '__group__' :
            return self.__group__.getProperty(name)
            
    def __setattr__(self, name, value):
        if name == 'name' :
            self.__group__.setName(value)
        elif name == '__group__' or name == '__model__' :
            self.__dict__[name] = value
        else :
            self.__group__.setProperty(name, str(value))
                        
    def __str__(self):
        return 'Group_' + self.name
    
    def __repr__(self):
        return 'au.gov.ansto.bragg.wombat.ui.script.pyobj.ScriptObjectGroup'
    
    def dispose(self):
        if not self.__model__ is None:
            self.__model__.removeControl(self.__group__)
    

class Tab():
    def __init__(self, model, name):
        self.__tab__ = ScriptObjectTab(name)
        self.__model__ = model
        if not model is None:
            model.addControl(self.__tab__)
        
    def add(self, *objs):
        for obj in objs :
            if isinstance(obj, Par) :
                self.__tab__.addObject(obj.__par__)
            elif isinstance(obj, Act) :
                self.__tab__.addObject(obj.__act__)
            elif isinstance(obj, Group) :
                self.__tab__.addObject(obj.__group__)
            elif isinstance(obj, Tab) :
                self.__tab__.addObject(obj.__tab__)
        
    def insert(self, idx, *objs):
        for obj in objs:
            if isinstance(obj, Par) :
                self.__tab__.insertObject(idx, obj.__par__)
            elif isinstance(obj, Act) :
                self.__tab__.insertObject(idx, obj.__act__)
            elif isinstance(obj, Group) :
                self.__tab__.insertObject(idx, obj.__group__)
            elif isinstance(obj, Tab) :
                self.__tab__.insertObject(idx, obj.__tab__)
            idx += 1

    def remove(self, *objs):
        for obj in objs:
            if isinstance(obj, Par) :
                self.__tab__.removeObject(obj.__par__)
            elif isinstance(obj, Act) :
                self.__tab__.removeObject(obj.__act__)
            elif isinstance(obj, Group) :
                self.__tab__.removeObject(obj.__group__)
            elif isinstance(obj, Tab) :
                self.__tab__.removeObject(obj.__tab__)
        
    def moveBeforeObject(self, obj):
        if isinstance(obj, Par):
            tgt = obj.__par__
        elif isinstance(obj, Act):
            tgt = obj.__act__
        elif isinstance(obj, Group):
            tgt = obj.__group__
        elif isinstance(obj, Tab):
            tgt = obj.__tab__
        else:
            raise Exception, 'Illegal type: target object must be a PyScript object'
        if not self.__model__ is None:
            self.__model__.moveObject1BeforeObject2(self.__tab__, tgt)

    def moveAfterObject(self, obj):
        if isinstance(obj, Par):
            tgt = obj.__par__
        elif isinstance(obj, Act):
            tgt = obj.__act__
        elif isinstance(obj, Group):
            tgt = obj.__group__
        elif isinstance(obj, Tab):
            tgt = obj.__tab__
        else:
            raise Exception, 'Illegal type: target object must be a PyScript object'
        if not self.__model__ is None:
            self.__model__.moveObject1AfterObject2(self.__tab__, tgt)
        
    def __getattr__(self, name):
        if name == 'name' :
            return self.__tab__.getName()
        elif name != '__tab__' :
            return self.__tab__.getProperty(name)
            
    def __setattr__(self, name, value):
        if name == 'name' :
            self.__tab__.setName(value)
        elif name == '__tab__' or name == '__model__' :
            self.__dict__[name] = value
        else :
            self.__tab__.setProperty(name, str(value))
                        
    def __str__(self):
        return 'Tab_' + self.name
    
    def __repr__(self):
        return 'au.gov.ansto.bragg.nbi.scripting.ScriptObjectGroup'
    
    def dispose(self):
        if not self.__model__ is None:
            self.__model__.removeControl(self.__tab__)
    
        