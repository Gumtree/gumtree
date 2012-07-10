package org.freehep.j3d.plot;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * Abstract class extended by other 3D Plot widgets.
 *
 * Defines default mouse behaviour etc.
 * @author Joy Kyriakopulos (joyk@fnal.gov)
 * @version $Id: Plot3D.java 8584 2006-08-10 23:06:37Z duns $
 */
public abstract class Plot3D extends Canvas3D
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5628170788775926251L;
	private static final float DEFAULT_CENTER_X = 0;
	private static final float DEFAULT_CENTER_Y = -0.3f;
	private static final float DEFAULT_CENTER_Z = 0;
	private static final double DEFAULT_ORIENTATION = -Math.PI / 4.;
	public enum ColorTheme{
		BLACK, WHITE, GRAY;
	}
	
   protected boolean init = false;
   protected boolean parallelProjection = false;
   protected SimpleUniverse universe;
   private TransformGroup objTransform;
   private Transform3D defaultTrans;
   private MouseRotate mouseRotate;
   private MouseTranslate mouseTranslate;
   private MouseZoom mouseZoom;
   private MouseWheelZoom mouseWheelZoom;
   private BranchGroup scene;
   private Node plot;

   Plot3D()
   {
      super(SimpleUniverse.getPreferredConfiguration());
   }
   protected void init()
   {
      plot = createPlot();
      scene = defineMouseBehaviour(plot);
      setupLights(scene); // Surface plot wants an extra light
      scene.compile();

      universe = new SimpleUniverse(this);
      universe.getViewingPlatform().setNominalViewingTransform();
      universe.addBranchGraph(scene);

      if (parallelProjection) {
          setProjectionPolicy(universe, parallelProjection);
      }
      init = true;
   }

   // addNotify is called when the Canvas3D is added to a container
   public void addNotify()
   {
      if (!init) init();
      super.addNotify(); // must call for Java3D to operate properly when overriding
   }

   public boolean getParallelProjection()
   {
      return parallelProjection;
   }

   public void setParallelProjection(boolean b)
   {
      if (parallelProjection != b) {
          parallelProjection = b;
          setProjectionPolicy(universe, parallelProjection);
      }
   }

   /**
     * Override to provide plot content
     */
   protected abstract Node createPlot();

   /**
    * Override to provide different mouse behaviour
    */
   protected BranchGroup defineMouseBehaviour(Node scene)
   {
      BranchGroup bg = new BranchGroup();
      bg.setCapability(BranchGroup.ALLOW_DETACH);
      Bounds bounds = getDefaultBounds();

      objTransform = new TransformGroup();
      objTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      objTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
      objTransform.addChild(scene);
      bg.addChild(objTransform);

      mouseRotate = new MouseRotate();
      mouseRotate.setTransformGroup(objTransform);
      mouseRotate.setSchedulingBounds(bounds);
      bg.addChild(mouseRotate);

      mouseTranslate = new MouseTranslate();
      mouseTranslate.setTransformGroup(objTransform);
      mouseTranslate.setSchedulingBounds(bounds);
      bg.addChild(mouseTranslate);

      mouseZoom = new MouseZoom();
      mouseZoom.setTransformGroup(objTransform);
      mouseZoom.setSchedulingBounds(bounds);
      mouseZoom.setFactor(-mouseZoom.getFactor());
      bg.addChild(mouseZoom);

      mouseWheelZoom = new MouseWheelZoom();
      mouseWheelZoom.setTransformGroup(objTransform);
      mouseWheelZoom.setSchedulingBounds(bounds);
      mouseWheelZoom.setFactor(-0.2);
      bg.addChild(mouseWheelZoom);

      // Set initial transformation
      defaultTrans = createDefaultOrientation();
      objTransform.setTransform(defaultTrans);

      Behavior keyBehavior = new PlotKeyNavigatorBehavior(objTransform,.1f,10f);
      objTransform.addChild(keyBehavior);
      keyBehavior.setSchedulingBounds(bounds);

      // set up a rotation animating behavior
      // rotator = setupZRotator(dynamicXform);
      // rotator.setSchedulingBounds(bounds);
      // rotator.setEnable(false);
      // dynamicXform.addChild(rotator);

      return bg;
   }

   protected void setupLights(BranchGroup root)
   {
      DirectionalLight lightD = new DirectionalLight();
      lightD.setDirection(new Vector3f(0.0f, -0.7f, -0.7f));
      lightD.setInfluencingBounds(getDefaultBounds());
      root.addChild(lightD);

      //  This second light is added for the Surface Plot, so you
      //  can see the "under" surface
      DirectionalLight lightD1 = new DirectionalLight();
      lightD1.setDirection(new Vector3f(0.0f, 0.7f, 0.7f));
      lightD1.setInfluencingBounds(getDefaultBounds());
      root.addChild(lightD1);

      DirectionalLight lightD2 = new DirectionalLight();
      lightD2.setDirection(new Vector3f(0.7f, -0.7f, 0.0f));
      lightD2.setInfluencingBounds(getDefaultBounds());
      root.addChild(lightD2);
      
      AmbientLight lightA = new AmbientLight();
      lightA.setInfluencingBounds(getDefaultBounds());
      root.addChild(lightA);
   }

   /**
    * Override to set a different initial transformation
    */
   protected Transform3D createDefaultOrientation()
   {
      Transform3D trans = new Transform3D();
      trans.setIdentity();
      trans.rotX(DEFAULT_ORIENTATION);
      trans.setTranslation(new Vector3f(DEFAULT_CENTER_X, DEFAULT_CENTER_Y, DEFAULT_CENTER_Z));
      return trans;
   }

   /**
    * Set the projection policy for the plot - either perspective or projection
    */
   protected void setProjectionPolicy(SimpleUniverse universe, boolean parallelProjection)
   {
        View view = universe.getViewer().getView();
        if (parallelProjection)
            view.setProjectionPolicy(View.PARALLEL_PROJECTION);
        else
            view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
   }

   public void setScreenScale(double scale) {
	   View view = universe.getViewer().getView();
	   view.setScreenScale(scale);
   }
   /**
     * Returns a bounds object that can be used for most behaviours,
     * lighting models, etc.
     */
   protected Bounds getDefaultBounds()
   {
      if (bounds == null)
      {
         Point3d center = new Point3d(0, 0, 0);
         bounds = new BoundingSphere(center, 10);
      }
      return bounds;
   }
   private Bounds bounds;
   
   public abstract void setData(Data3D data);
   
   public void cleanUp() {
	   universe.cleanup();
	   universe = null;
	   scene = null;
	   objTransform = null;
	   mouseRotate = null;
	   mouseTranslate = null;
	   mouseZoom = null;
	   mouseWheelZoom = null;
   }
   
   public abstract String getXAxisLabel();
   
   public abstract void setXAxisLabel(String xLabel);
   
   public abstract String getYAxisLabel();
   
   public abstract void setYAxisLabel(String yLabel);

   public abstract String getZAxisLabel();
   
   public abstract void setZAxisLabel(String zLabel);
   /**
    * @return the objTransform
    */
   public TransformGroup getObjTransform() {
	   return objTransform;
   }
   
   public void resetOrientation() {
	   Transform3D trans = new Transform3D();
	   getObjTransform().getTransform(trans);
	   trans.setRotation(new AxisAngle4d(1, 0, 0, DEFAULT_ORIENTATION));
//	   trans.setIdentity();
//	   Transform3D transformZ = new Transform3D();
//	   transformZ.rotX(-Math.PI / 4.);
//	   trans.mul(transformZ, trans);
//	   trans.set(translation);
	   getObjTransform().setTransform(trans);

   }
   
   public void zoomInDepth(double rate) {
	   Transform3D trans = new Transform3D();
	   getObjTransform().getTransform(trans);
//	   trans.mul(rate);
//	   trans.setIdentity();
	   Vector3d translation = new Vector3d();
	   translation.z = rate;
	   Transform3D transformZ = new Transform3D();
	   transformZ.set(translation);
	   trans.mul(transformZ, trans);
//	   trans.set(translation);
	   getObjTransform().setTransform(trans);
   }
   
   public void resetZoomDepth() {
	   Transform3D trans = new Transform3D();
	   getObjTransform().getTransform(trans);
	   Vector3d translation = new Vector3d();
	   trans.get(translation);
	   translation.z = DEFAULT_CENTER_Z;
	   trans.setTranslation(translation);
//	   Transform3D transformZ = new Transform3D();
//	   transformZ.set(translation);
//	   trans.mul(trans, transformZ);
	   getObjTransform().setTransform(trans);
   }
   
   public void resetCenter() {
	   Transform3D trans = new Transform3D();
	   getObjTransform().getTransform(trans);
	   Vector3d translation = new Vector3d();
	   trans.get(translation);
	   translation.x = DEFAULT_CENTER_X;
	   translation.y = DEFAULT_CENTER_Y;
	   trans.setTranslation(translation);
//	   Transform3D transformZ = new Transform3D();
//	   transformZ.set(translation);
//	   trans.mul(trans, transformZ);
	   getObjTransform().setTransform(trans);
   }

   public void resetToDefault() {
	   getObjTransform().setTransform(defaultTrans);
   }
   
   public abstract AbstractPlotBuilder getBuilder();
   
   public void repaint(Binned2DData data) {
//	   Node plot = createPlot();
//	   BranchGroup newScene = defineMouseBehaviour(plot);
//	   setupLights(newScene); // Surface plot wants an extra light
//	   newScene.compile();
//
////	   universe.getViewingPlatform().setNominalViewingTransform();
//	   universe.getLocale().replaceBranchGraph(scene, newScene);
//	   scene = newScene;
////	   if (parallelProjection) {
////		   setProjectionPolicy(universe, parallelProjection);
////	   }
//	   super.repaint();
	   getBuilder().updatePlot(new NormalizedBinned2DData(data));
   }
   
   public void setRenderStyle(RenderStyle style) {
   }
   
   public RenderStyle getRenderStyle(){
	   return null;
   }
   
   public SimpleUniverse getUniverse() {
	   return universe;
   }
   
   public abstract void setLogZscaling(boolean b);
   
   public abstract boolean getLogZscaling();
   
   public abstract void setColorTheme(ColorTheme colorTheme);
	
   public abstract ColorTheme getColorTheme();
   
   public abstract void applyColorTheme();
   
   public Node getPlot() {
	   return plot;
   }
   
   public abstract void toggleOutsideBoxEnabled();
   
   
}

