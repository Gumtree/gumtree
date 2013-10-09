# Initial import

###############################################################################
#
# Import sics
#
###############################################################################

from gumpy.commons import sics

###############################################################################
#
# Quokka related import
#
###############################################################################

# Import logger
from gumpy.commons.logger import log

# Import all
from bragg.quokka import quokka

# Import enums to global scope
from bragg.quokka.quokka import guideConfig, action, hmMode, scanMode, dataType, saveType, attenuationLevels

# Import functions to global scope
from bragg.quokka.quokka import getAttValue, driveAtt, setSafeAttenuation
from bragg.quokka.quokka import getDetPosition, driveDet, getDetOffsetValue, driveDetOffset
from bragg.quokka.quokka import getL1Value, getL2Value, getLambdaValue
from bragg.quokka.quokka import getBsPosition, getAllBsPosition, getBsRailPosition, getBsxValue, getBszValue, driveBs, getBsRailPosition, driveBsRailIn, driveBsRailOut, driveBsx, driveBsz
from bragg.quokka.quokka import getGuideConfig, driveGuide
from bragg.quokka.quokka import getDhv1Value, driveDhv1
from bragg.quokka.quokka import getEntRotApValue, driveEntRotAp
from bragg.quokka.quokka import getSamplePosition, driveSample, setSample, driveToLoadPosition
from bragg.quokka.quokka import getJulaboValue, driveJulabo
from bragg.quokka.quokka import driveFlipper
from bragg.quokka.quokka import driveHistmem, getMaxBinRate, scan
from bragg.quokka.quokka import selBs, selBsxz
from bragg.quokka.quokka import printQuokkaSettings
