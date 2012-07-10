from echidna.reduction import *

#reduction.do_stitching = False
#reduction.do_integration = False

start_id = 4918
stop_id = 4920
save_path = DatasetFactory.__path__ + '/' + 'reduced'

viewer = browser.DataBrowser()
for id in xrange(start_id, stop_id + 1) :
    ds = ECH[id]
    print ds.title + ' loaded'
    viewer.add(ds)
    res = reduce(ds)
    new_title = ds.title.split('.')[0] + '.reduced.hdf'
    res.title = new_title
    viewer.add(res)
    print 'export result ... ',
    res.save_copy(save_path + '\\' + new_title)
    print 'done'
    
#viewer.remove(ECH[4918])
#viewer.remove(viewer[0])