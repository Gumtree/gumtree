from au.gov.ansto.bragg.quokka.msw.schedule import PythonInstrumentActionExecuter

from gumpy.commons.logger import log

from bragg.quokka import quokka
from bragg.quokka.config import ConfigSystem


# create config system for multi drive
quokka.config = ConfigSystem()


def slog(text):
    global __CONSOLE_WRITER__
    log(text, __CONSOLE_WRITER__)


def setConsoleWriter(id):
    global __CONSOLE_WRITER__
    __CONSOLE_WRITER__ = PythonInstrumentActionExecuter.getObject(id)
    quokka.setConsoleWriter(__CONSOLE_WRITER__)


def deferredCall(target, id):
    info = PythonInstrumentActionExecuter.getObject(id)
    if info is not None:
        try:
            target(info)
        except:
            info.interrupted = True
            raise

    else:
        slog('error: %s' % id)


def initiate(id):
    deferredCall(quokka.initiate, id)


def cleanUp(id):
    deferredCall(quokka.cleanUp, id)


def setParameters(id):
    deferredCall(quokka.setParameters, id)


def preAcquisition(id):
    deferredCall(quokka.preAcquisition, id)


def doAcquisition(id):
    deferredCall(quokka.doAcquisition, id)


def postAcquisition(id):
    deferredCall(quokka.postAcquisition, id)


def customAction(id):
    deferredCall(quokka.customAction, id)
