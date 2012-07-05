import unittest

class TestImportModules(unittest.TestCase):

	def test_import(self):
		from gumpy.nexus import array
		from gumpy.nexus import browser
		from gumpy.nexus import data
		from gumpy.nexus import dataset
		from gumpy.nexus import fitting
		from gumpy.nexus import nutils
		from gumpy.nexus import simpledata

def getSuite():
	return unittest.TestLoader().loadTestsFromTestCase(TestImportModules)
