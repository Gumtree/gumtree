from nexus import browser
from nexus.dataset import Dataset
from nexus import *
from nexus.dataset import df as ECH
import echidna

#viewer = browser.DataBrowser(True)
#d1 = dataset.arange(48, [2, 3, 4, 2])
#print d1.stth
#DatasetFactory.__path__ = 'W:\\commissioning\\'
ds1 = ECH[4918]
print (ds1[0,0,10,:10] * 1.2).tolist()
#print ds1.sum(0)
#print ds1.shape
#print ds1[0, 0, :10, :10]
#ds1[0, 0, :10, :10] *= 2.2
#print ds1[0, 0, :10, :10]
#bgd = ECH['backgroundFile']
#print bgd.sum(0)
#print res
#ds1.nomal = 'time'
#ds1.schi = 5.2
#ds1.save('schi')
#ds1.save()
#print ds1.schi
#d3 = d2[2:10, 0]
#d4 = d3[1, 0, 1:]
#d4.stth = 3.3
#print d4.stth.tolist()
##print d3.__iNXroot__.getDataItem('stth')
#stth = d3.stth
#d3.stth = 3.3
#print d2.stth


#eff = ECH['efficiencyMap']
#print eff.shape


##d1.title = 'd1'
#viewer.add(ds1)
#print ds1.nomal
#viewer.add(eff)
#viewer.add(bgd)