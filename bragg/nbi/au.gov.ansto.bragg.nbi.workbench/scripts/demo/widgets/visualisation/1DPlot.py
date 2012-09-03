from gumpy.commons.swt import swtFunction
from gumpy.nexus import dataset

from org.eclipse.swt import SWT

from org.gumtree.data.ui.viewers import PlotViewer
from org.gumtree.vis.nexus.utils import NXFactory

@swtFunction
def create(parent):
    # Create plot
    plotviewer = PlotViewer(parent, SWT.NONE)
    # Create dataset
    ds = dataset.rand(100)
    # Wrap to plottable dataset
    pds = NXFactory.createSingleXYDataset('Plot', ds.__iNXDataset__)
    # Plot
    plotviewer.setDataset(pds)

@swtFunction
def dispose():
    pass