from gumpy.commons.swt import swtFunction

from java.lang import Runnable

from javafx.application import Platform
from javafx.collections import FXCollections
from javafx.embed.swt import FXCanvas
from javafx.scene import Group
from javafx.scene import Scene
from javafx.scene.chart import PieChart

from org.eclipse.swt import SWT

def fxFunction(function):
    def internalRun(*args, **kwargs):
        class InternalRunnable(Runnable):
            def run(self):
                function(*args, **kwargs)
        Platform.runLater(InternalRunnable())
    return internalRun

@swtFunction
def create(parent):
    fxCanvas = FXCanvas(parent, SWT.NONE)
    createChart(fxCanvas)

@swtFunction
def dispose():
    pass

@fxFunction
def createChart(fxCanvas):
    pieChartData = FXCollections.observableArrayList(
                PieChart.Data('Grapefruit', 13),
                PieChart.Data('Oranges', 25),
                PieChart.Data('Plums', 10),
                PieChart.Data('Pears', 22),
                PieChart.Data('Apples', 30))
    chart = PieChart(pieChartData)
    chart.setTitle('Imported Fruits')    
    group = Group()
    group.getChildren().add(chart)
    scene = Scene(group)
    fxCanvas.setScene(scene)
