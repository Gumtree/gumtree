from au.gov.ansto.bragg.quokka.msw.schedule import PythonInstrumentActionExecuter

from bragg.quokka import quokka
from bragg.quokka.config import ConfigSystem


# create config system for multi drive
quokka.config = ConfigSystem()


def deferredCall(target, id):
    info = PythonInstrumentActionExecuter.getObject(id)
    if info is not None:
        try:
            target(info)
        except:
            info.interrupted = True
            raise

    else:
        quokka.slog('error: %s' % id, f_err=True)


def initiate(id):
    quokka.sinit()
    deferredCall(quokka.initiate, id)


def cleanUp(id):
    try:
        deferredCall(quokka.cleanUp, id)
    finally:
        quokka.sclose()


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
