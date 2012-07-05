from java.io import FileWriter
from org.python.core import PyFileWriter
from unittest import TestCase
import unittest
import xmlrunner

class Test1(TestCase):
	def setUp(self):
		pass

	def test_123(self):
		self.assertEqual(1, 1)

def run(file='jython_test.xml'):
	# Create writer
	fileWriter = FileWriter(file)
	pyFileWriter = PyFileWriter(fileWriter)

	# Run test
	suite = unittest.TestSuite([unittest.TestLoader().loadTestsFromTestCase(Test1)])
	runner = xmlrunner.XMLTestRunner(pyFileWriter)
	s = runner.run(suite)

	# Close writer
	pyFileWriter.close()
	fileWriter.close()
