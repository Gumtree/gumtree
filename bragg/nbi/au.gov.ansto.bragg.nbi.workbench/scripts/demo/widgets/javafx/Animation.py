from gumpy.commons.swt import swtFunction

from java.lang import Runnable

from javafx.animation import Timeline
from javafx.animation import TranslateTransition
from javafx.animation import TranslateTransitionBuilder
from javafx.application import Platform
from javafx.embed.swt import FXCanvas
from javafx.scene import Group
from javafx.scene import Scene
from javafx.scene.effect import Lighting
from javafx.scene.paint import Color
from javafx.scene.shape import Circle
from javafx.util import Duration

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
    createAnimation(fxCanvas)

@swtFunction
def dispose():
    pass

@fxFunction
def createAnimation(fxCanvas):
    circle = Circle(20, Color.CRIMSON)
    circle.setEffect(Lighting())
    circle.setTranslateX(20)
    circle.setTranslateY(20)
    group = Group()
    group.getChildren().add(circle)
    
    translateTransition = TranslateTransitionBuilder.create()\
        .duration(Duration.seconds(4)).node(circle).fromX(20).toX(380)\
        .cycleCount(Timeline.INDEFINITE).autoReverse(True).build()
    translateTransition.play()
    
    scene = Scene(group)
    fxCanvas.setScene(scene)
