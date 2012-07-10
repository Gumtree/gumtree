from echidna.reduction import *
from vis import *

#reduction.do_stitching = False
#reduction.do_integration = False

start_id = 4918
stop_id = 4920
save_path = DatasetFactory.__path__ + '/' + 'reduced'

p1 = plot()
for id in xrange(start_id, stop_id + 1) :
    ds = ECH[id]
    print ds.title + ' loaded'
    image(ds[0].get_reduced())
    res = reduce(ds)
    new_title = __ds__.title.split('.')[0] + '.reduced.hdf'
    res.title = new_title
    p1.add_dataset(res)
    print 'export result ... ',
    res.save_copy(save_path + '\\' + new_title)
    print 'done'
    
#viewer.remove(ECH[4918])
#viewer.remove(viewer[0])