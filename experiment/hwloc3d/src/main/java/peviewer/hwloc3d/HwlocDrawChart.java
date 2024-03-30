package peviewer.hwloc3d;

import java.util.ArrayList;
import java.util.List;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.IChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.rendering.view.modes.CameraMode;
import org.jzy3d.plot3d.text.ITextRenderer;
import org.jzy3d.plot3d.text.drawable.DrawableTextWrapper;
import org.jzy3d.plot3d.text.renderers.jogl.JOGLTextRenderer3d;

import xxx.peviewer.hwloc3d.xjcgenerated.Object;
import xxx.peviewer.hwloc3d.xjcgenerated.Topology;
import org.jzy3d.plot3d.rendering.view.ViewportConfiguration;
public class HwlocDrawChart {
	static Chart drawChart(Topology t) {
		// Create a chart to add surfaces to
		IChartFactory f = new AWTChartFactory();
		Chart chart = f.newChart(Quality.Fastest().setHiDPIEnabled(true));
		//Advanced and nicest quality creates clipping
		//possibly depth buffer issue
		
		
		//chart.getView().setMaximized(true);
		//chart.getView().setBackgroundColor(Color.ORANGE);
		chart.getView().setSquared(false);
		//chart.getView().setCameraMode(CameraMode.PERSPECTIVE);
		chart.getView().setAxisDisplayed(false);
		//chart.getView().setViewPositionMode(ViewPositionMode.TOP);
		//chart.getView().getCamera().projectionPerspective(null, new ViewportConfiguration(10, 10));
		
		ITextRenderer r = new JOGLTextRenderer3d();
		//must be big enough for text padding but small enough for legible text
		
	    double l = 11;
		double w = 7;
		double h = 0;
		
		//11x7 for haswell
		//
		
		Object top = t.getObject().get(0);	
		
		//draw package only
			//top = t.getObject().get(0).getObject().get(0);
		
		//draw bridge only
			//top = t.getObject().get(0).getObject().get(1);
			//recursiveBridgeDraw(top, new Coord3d(0,0,0), chart, r);
		
		recursiveDraw(top, new Coord3d(0,0,0), new Coord3d(l,w,h), chart, r);
		
		
		//System.out.println(countPCI(top));
		//should = 8
		
		ChartLauncher.openChart(chart, "HWLOC3D Chart");
		return chart;
	}
	
	//needs separate draw function for pci bridge, otherwise it breaks
	private static void recursiveBridgeDraw(Object o, Coord3d origin, Chart chart, ITextRenderer r) {
		double x = (double) origin.x;
		double y = (double) origin.y;
		
		//System.out.println(o.getType());
		
		Color color = Color.WHITE;
		//if bridge, draw square and line right
		if (o.getType().equals("Bridge")) {
			spawn(origin.add(new Coord3d(-.3,-.2,.1)), origin.add(new Coord3d(.1,.2,.1)), color, chart, r, null);
			origin = origin.add(new Coord3d(.1,0,0));
			drawLine(origin, origin.add(new Coord3d(1,0,0)), chart);
			origin = origin.add(new Coord3d(1,0,0));
		
			//loop over children
			List<Object> children = o.getObject();	
			
			for (int a = 0; a < children.size(); a++) {
				recursiveBridgeDraw(children.get(a),  origin, chart, r);
				//branch if multiple children and not last child
				if (children.size() > 1 && a != children.size()-1) {
					
					//must be in scope of bridge parent
					//how may pci were in prev child, parent of a bridge can only be a bridge
					//System.out.println(o.getType());
					
					//System.out.println(countPCI(children.get(a)));
					
					int depth = countPCI(children.get(a));
					//count nested pcis per child with helper function
					
					drawLine(new Coord3d(x, origin.y, 0), new Coord3d(x,y-1.2*depth,0), chart);
					//x will be const for each corner
					//y stacks
					y = y-1.2*depth;
				
					
					drawLine(new Coord3d(x, y, 0), new Coord3d(x+1,y,0), chart);
					//line down and right based on height after each child until last child
					//reset origin
					origin = new Coord3d(x+1,y,0);
				}
			}
		
		}
		//if pci, check for child, add if exists, get pci type, name
		else if (o.getType().equals("PCIDev")) {
			color = new Color(219,223,190);
			spawn(origin.add(new Coord3d(0,-.5,0)), origin.add(new Coord3d(1, .5, 0)), color, chart, r, "PCIDev");
		
			if (o.getObject().size() > 0) {
				//System.out.println(o.getObject().get(0).getName());
				spawn(origin.add(new Coord3d(.1,-.2,.1)), origin.add(new Coord3d(.9, .2, .1)), Color.GRAY, chart, r, o.getObject().get(0).getName());
			}
		}
				
	}

	//trace the code lol should have tested if it was working first
	private static int countPCI(Object o) {
		int count = 0;
		//System.out.println(o.getName());
		List<Object> children = o.getObject();	
		
		//should not check child inside loop, check the object
		if (o.getType().equals("PCIDev")) {
			return 1;
		} 
		
		for (int a = 0; a < children.size(); a++) {
			count += countPCI(children.get(a));
		}
		return count;
	}
	
	private static void drawLine(Coord3d origin, Coord3d coord3d, Chart chart) {
		List<Polygon> polygons = new ArrayList<Polygon>();
		
		Polygon polygon = new Polygon();
	    polygon.add(new Point(origin));
	    polygon.add(new Point(coord3d));
	    
	    polygons.add(polygon);
	    
	    Shape tempShape = new Shape(polygons);
	    
	    tempShape.setFaceDisplayed(true);
	    tempShape.setWireframeDisplayed(true);
	    tempShape.setWireframeColor(Color.BLACK);
	    tempShape.setWireframeWidth(2);
	    
	    chart.getScene().getGraph().add(tempShape);
	}


	public static void recursiveDraw(Object o, Coord3d origin, Coord3d dim, Chart chart, ITextRenderer r) {
		
	
		
		//color picking
		Color color = Color.WHITE;
		if (o.getType().equals("Package")) {color = new Color(219,233,180);}
		else if (o.getType().equals("Core")) {color = Color.GRAY;}
		else if (o.getType().equals("NUMANode")) {color = new Color(160,150,150);}
		//use .equals not ==
		
		//draw shape
		//top padding
		
		spawn(origin, dim, color, chart, r, o.getType());
		
		List<Object> children = o.getObject();	
		int num_children = children.size();
		
		for (int y =0; y<num_children; y++) {
			if (children.get(y).getType().equals("Bridge")) {
				num_children--;
				//draw bridge parts to the right
					recursiveBridgeDraw(children.get(y), dim.add(new Coord3d(.3, 0, 0)), chart, r);
				//draw bridge parts on bottom
				//scope is in very top loop so origin and dim are the og ones
				//recursiveBridgeDraw(children.get(y), new Coord3d(origin.x, origin.y-.5, origin.z), chart, r);
				
				
				//draw machine last to encapsulate everything?
			}
		}
		
		
		//helper function to calculate depth using pci depth
		//needs to calculate horizontal length by finding deepest chain
		//------------
		
		//System.out.println(o.getType());
		//System.out.println("origin: "+origin+ "      dim: "+dim);
		//System.out.println("has "+num_children+" children");
		
		//if no children take half as much space?
		//reduce child num and reduce height, use extra height for childless child
		//double padding + 
		
		//add top padding for text by shrinking dim.y
		dim = new Coord3d(dim.x, dim.y-.15, dim.z);
		
		//padding segments is 1 + num children
		//split vertically
		if (dim.x>dim.y && num_children>3) {
			
			double pad = (double) (dim.x*.05)/(num_children+1);
			
			double width = (double) (dim.x*.95/num_children);
			
			
			//top padding
			origin = origin.add(new Coord3d(pad, 0.15,0.15));
			dim = new Coord3d(width, dim.y-.1, .15);
				
				for (int a = 0; a < num_children; a++) {
					recursiveDraw(children.get(a),  origin,  dim, chart, r);
					origin = origin.add(new Coord3d(width,0,0));
					dim = dim.add(new Coord3d(width,0,0));
				}
				
			//partition horizontally
		} else {
			
			double pad = (double) (dim.y*.05)/(num_children+1);
			
			double width = (double) (dim.y*.95/num_children);
			
			//if other child is bridge, deincrement children, no need to allocate space
			//but then top layer doesn't run?
			
			
			origin = origin.add(new Coord3d(0.15,pad,0.15));
			
				dim = new Coord3d(dim.x-.1,width, .15);
				
				for (int a = 0; a < num_children; a++) {
					recursiveDraw(children.get(a),  origin,  dim, chart, r);
					origin = origin.add(new Coord3d(0,width,0));
					dim = dim.add(new Coord3d(0,width,0));
					}
		}
		}
		
	
	
	//helper method to add faces
	
	public static void addFace(List<Polygon> faceslist, Coord3d c1, Coord3d c2,Coord3d c3, Coord3d c4)
	  {
	    Polygon polygon = new Polygon();
	    polygon.add(new Point(c1));
	    polygon.add(new Point(c2));
	    polygon.add(new Point(c3));
	    polygon.add(new Point(c4));
	    faceslist.add(polygon);
	    
	  }
	
	//helper method to add wireframe, color, etc.
		public static void addColor(Shape shape, Color color)
		  {
			shape.setFaceDisplayed(true);
			shape.setColor(color);
			shape.setWireframeDisplayed(true);
			shape.setWireframeColor(Color.BLACK);
			shape.setWireframeWidth(2);
		   }
	
		//generate cube with color and add to chart
		public static void spawn(Coord3d spawn, Coord3d spawn2, Color color, Chart chart, ITextRenderer r, String s) {
			List<Polygon> temp = new ArrayList<Polygon>();
		    boxGen(temp, spawn, spawn2);
		    Shape tempShape = new Shape(temp);
		    addColor(tempShape, color);
		    chart.getScene().getGraph().add(tempShape);
			
			if (s != null) {
				DrawableTextWrapper txt = new DrawableTextWrapper(s, new Coord3d(spawn.x,spawn2.y-.2,spawn.z), Color.BLACK, r);
				chart.getScene().getGraph().add(txt);
			}
			
			
		}
		
	//helper method to create cubes and 3d rectangles
		//currently only in 2d
		public static void boxGen(List<Polygon> faceslist, Coord3d spawn, Coord3d spawn2)
		  {
			
			//spawn at spawnpoint to to final			
			//spawn2 > spawn
			
			//lwh --> xyz
			Coord3d o = spawn;
			Coord3d x = new Coord3d(spawn2.x, spawn.y, spawn.z);
		    Coord3d y = new Coord3d(spawn.x, spawn2.y, spawn.z);
		    Coord3d z = new Coord3d(0,0,spawn2.z);

		    //bottom
			addFace(faceslist, o, x, new Coord3d(spawn2.x, spawn2.y, spawn.z), y);
			//using push face makes the text separate for some reason
			
			//top
		    //pushFace(faceslist, o, x, new Coord3d(spawn2.x, spawn2.y, spawn.z), y, z);
			
			
			/*
			//l
			addFace(faceslist, o, y, y.add(z), z);
			//back l
			pushFace(faceslist, o, y, y.add(z), z, x);
			
			//r
			addFace(faceslist, o, x, x.add(z), z);
			//back r
			pushFace(faceslist, o, x, x.add(z), z,y);
			*/
		   }
		
	//helper method to add faces, increasing all coords by a given coord	
		public static void pushFace(List<Polygon> faceslist, Coord3d c1, Coord3d c2,Coord3d c3, Coord3d c4, Coord3d c5)
		  {
		    Polygon polygon = new Polygon();
		    polygon.add(new Point(c1.add(c5)));
		    polygon.add(new Point(c2.add(c5)));
		    polygon.add(new Point(c3.add(c5)));
		    polygon.add(new Point(c4.add(c5)));
		    faceslist.add(polygon);
		    
		  }
		
		
		static List<Polygon> list(double x, double y , double z, double width, double length, double height){

	        List<Polygon> polygons = new ArrayList<Polygon>();
	        Polygon polygon = new Polygon();

	        //下面
	        polygon.add(new Point(new Coord3d(x, y, z)));

	        polygon.add(new Point(new Coord3d(x, y+length, z)));
	        polygon.add(new Point(new Coord3d(x+width, y+length, z)));
	        polygon.add(new Point(new Coord3d(x+width, y, z)));
	        polygon.add(new Point(new Coord3d(x, y, z)));

	        //左边
	        polygon.add(new Point(new Coord3d(x, y, z+height)));
	        polygon.add(new Point(new Coord3d(x, y+length, z+height)));
	        polygon.add(new Point(new Coord3d(x, y+length, z)));
	        polygon.add(new Point(new Coord3d(x, y, z)));
	        polygon.add(new Point(new Coord3d(x, y, z+height)));

	        //上面
	        polygon.add(new Point(new Coord3d(x+width, y, z+height)));
	        polygon.add(new Point(new Coord3d(x+width, y+length, z+height)));
	        polygon.add(new Point(new Coord3d(x, y+length, z+height)));
	        polygon.add(new Point(new Coord3d(x, y, z+height)));
	        polygon.add(new Point(new Coord3d(x+width, y, z+height)));
	        polygon.add(new Point(new Coord3d(x+width, y, z)));

	        //后面

	        polygon.add(new Point(new Coord3d(x+width, y+length, z)));
	        polygon.add(new Point(new Coord3d(x+width, y+length, z+height)));
	        polygon.add(new Point(new Coord3d(x, y+length, z+height)));
	        polygon.add(new Point(new Coord3d(x, y, z+height)));
	        polygons.add(polygon);

	        return polygons;

	    }
		

}

