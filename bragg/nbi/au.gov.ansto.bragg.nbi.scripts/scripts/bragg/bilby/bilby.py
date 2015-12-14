from gumpy.commons import sics

def att_pos(val = None):
    if not val is None :
        sics.drive('att', val)
    return sics.get_raw_value('att')

def nguide(val1, val2, val3):
    return sics.run_command('nguide ' + str(val1) + ' ' + str(val2) + ' ' + str(val3))

def sapmot(val = None):
    if not val is None:
        sics.run_command('pdrive sapmot ' + str(val))
    return sics.get_raw_value('posname sapmot', str)

def samx(val = None):
    if not val is None :
        sics.drive('samx', val)
    return sics.get_raw_value('samx') 

def samz(val = None):
    if not val is None :
        sics.drive('samz', val)
    return sics.get_raw_value('samz')

def som(val = None):
    if not val is None :
        sics.drive('som', val)
    return sics.get_raw_value('som')
 
def sam_pos(val = None):
    if not val is None :
        sics.drive('som', val)
    return sics.get_raw_value('som')

def det(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('det', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('det')

def curtaindet(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('curtaindet', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('curtaindet')

def curtainl(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('curtainl', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('curtainl')

def curtainr(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('curtainr', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('curtainr')

def curtainu(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('curtainu', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('curtainu')

def curtaind(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('curtaind', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('curtaind')

def bs3(val):
    if not val is None:
        if val.lower() == 'in':
            sics.drive('bs3', 65)
        elif val.lower() == 'out':
            sics.drive('bs3', 0)
    cur = sics.get_raw_value('bs3')
    if cur >= 63 and cur <= 67:
        return 'IN'
    else :
        return 'OUT'
    
def bs4(val):
    if not val is None:
        if val.lower() == 'in':
            sics.drive('bs4', 65)
        elif val.lower() == 'out':
            sics.drive('bs4', 0)
    cur = sics.get_raw_value('bs4')
    if cur >= 63 and cur <= 67:
        return 'IN'
    else :
        return 'OUT'

def bs5(val):
    if not val is None:
        if val.lower() == 'in':
            sics.drive('bs5', 65)
        elif val.lower() == 'out':
            sics.drive('bs5', 0)
    cur = sics.get_raw_value('bs5')
    if cur >= 63 and cur <= 67:
        return 'IN'
    else :
        return 'OUT'