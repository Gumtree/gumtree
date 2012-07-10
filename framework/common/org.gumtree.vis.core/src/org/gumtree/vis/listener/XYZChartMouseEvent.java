package org.gumtree.vis.listener;

import java.awt.event.MouseEvent;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;

public class XYZChartMouseEvent extends ChartMouseEvent {

	private double x;
	private double y;
	private double z;
	
	public XYZChartMouseEvent(JFreeChart chart, MouseEvent trigger,
			ChartEntity entity) {
		super(chart, trigger, entity);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6793580642092161526L;

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return the z
	 */
	public double getZ() {
		return z;
	}

	public void setXYZ(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
