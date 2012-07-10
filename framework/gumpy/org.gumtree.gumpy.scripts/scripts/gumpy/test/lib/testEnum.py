from gumpy.lib.enum import Enum
import unittest

class TestEnum(unittest.TestCase):
    
    def test_enum(self):
        weekdays = Enum('MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN')
        self.assertEqual(weekdays.MON.index, 0)
        self.assertEqual(weekdays.SUN.index, 6)
        self.assertEqual(weekdays.TUE.key, 'TUE')
        self.assertEqual(weekdays.FRI.key, 'FRI')
    
def getSuite():
    return unittest.TestLoader().loadTestsFromTestCase(TestEnum)
