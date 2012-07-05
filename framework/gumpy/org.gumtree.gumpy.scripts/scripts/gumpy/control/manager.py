import script
from param import Par
from au.gov.ansto.bragg.nbi.ui.scripting.pyobj import ScriptModel

__model__ = ScriptModel.getModel(script.__script_model_id__)
__script__ = script.Script(__model__)

print 'manager imported'