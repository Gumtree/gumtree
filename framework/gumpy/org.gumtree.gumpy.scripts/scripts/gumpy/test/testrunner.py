from java.io import FileWriter
from org.python.core import PyFileWriter
import sys
import gumpy
import unittest
import xmlrunner

def runTests(suite):
	unittest.TextTestRunner(verbosity=2).run(suite)

def runTestsWithXMLResult(suite, writer=sys.stdout):
	runner = xmlrunner.XMLTestRunner(writer)
	s = runner.run(suite)

def runTestsWithXMLResultAsFile(suite, file='jython_test.xml'):
	# Create writer
	fileWriter = FileWriter(file)
	pyFileWriter = PyFileWriter(fileWriter)
	
	# Run test
	runTestsWithXMLResult(suite, pyFileWriter)
	
	# Close writer
	pyFileWriter.close()
	fileWriter.close()

def getSuite():
	return unittest.TestSuite([
		# Commons
		gumpy.test.commons.testCore.getSuite(),\
		gumpy.test.commons.testLogger.getSuite(),\
		gumpy.test.commons.testJutils.getSuite(),\
		# Lib
		gumpy.test.lib.testEnum.getSuite(),\
		gumpy.test.lib.testODict.getSuite(),\
		#Nexus
		gumpy.test.nexus.testMultiarray.getSuite(),\
		gumpy.test.nexus.testImportModules.getSuite()])
