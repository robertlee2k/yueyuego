/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 * PrefuseGraph.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 * Copyright (C) Jeffrey Heer (original prefuse demo)
 */

package wekacall.gui.visualize.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.GraphMLReader;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.ForceSimulator;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JValueSlider;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import weka.gui.graphvisualizer.BIFParser;
import weka.gui.graphvisualizer.GraphEdge;
import weka.gui.graphvisualizer.GraphNode;
import weka.gui.visualize.plugins.GraphVisualizePlugin;

/**
 * Displays a graph in <a
 * href="http://www.cs.cmu.edu/~fgcozman/Research/InterchangeFormat/"
 * target="_blank">XML BIF</a> format as <a href="http://prefuse.org/"
 * target="_blank">Prefuse</a> graph.
 * <p/>
 * Based on the <code>prefuse.demos.GraphView</code> demo.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a> (original prefuse demo)
 * @author fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 10237 $
 * @see prefuse.demos.GraphView
 */
public class PrefuseGraph implements Serializable, GraphVisualizePlugin {

  /** for serialization. */
  private static final long serialVersionUID = 5541844748101135174L;

  /**
   * Turns the <a
   * href="http://www.cs.cmu.edu/~fgcozman/Research/InterchangeFormat/"
   * target="_blank">XML BIF</a> format into <a
   * href="http://graphml.graphdrawing.org/specification/"
   * target="_blank">GraphML XML</a> format.
   * 
   * @author fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 10237 $
   */
  public static class BIFToGraphML {

    /**
     * Replaces certain characters with their character entities.
     * 
     * @param s the string to process
     * @return the processed string
     */
    protected String sanitize(String s) {
      String result;

      result = s;
      result = result.replaceAll("&", "&amp;").replaceAll("\"", "&quot;")
        .replaceAll("'", "&apos;").replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
      // in addition, replace some other entities as well
      result = result.replaceAll("\n", "&#10;").replaceAll("\r", "&#13;")
        .replaceAll("\t", "&#9;");

      return result;
    }

    /**
     * Writes the header of the GraphML file.
     * 
     * @param writer the writer to use
     * @throws Exception if an error occurs
     */
    protected void writeHeader(BufferedWriter writer) throws Exception {
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      writer.newLine();
      writer.newLine();
      writer
        .write("<!-- This file was generated by Weka (http://www.cs.waikato.ac.nz/ml/weka/). -->");
      writer.newLine();
      writer.newLine();
      writer
        .write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
      writer.newLine();
      writer
        .write("<key id=\"node-label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>");
      writer.newLine();
      writer
        .write("<graph id=\"" + new Date() + "\" edgedefault=\"directed\">");
      writer.newLine();
    }

    /**
     * Writes the node as GraphML.
     * 
     * @param writer the writer to use
     * @param node the node to write as GraphML
     * @throws Exception if an error occurs
     */
    protected void writeNodes(BufferedWriter writer, GraphNode node)
      throws Exception {
      writer.write("<node id=\"" + node.ID + "\">");
      writer.newLine();
      writer
        .write("<data key=\"node-label\">" + sanitize(node.lbl) + "</data>");
      writer.newLine();
      writer.write("</node>");
      writer.newLine();
    }

    /**
     * Writes the edge as GraphML.
     * 
     * @param writer the writer to use
     * @param nodes the nodes
     * @param edge the edge to writge
     * @throws Exception if an error occurs
     */
    protected void writeEdges(BufferedWriter writer,
      ArrayList<GraphNode> nodes, GraphEdge edge) throws Exception {
      writer.write("<edge id=\"" + edge.hashCode() + "\" source=\""
        + nodes.get(edge.src).ID + "\" target=\"" + nodes.get(edge.dest).ID
        + "\"/>");
      writer.newLine();
    }

    /**
     * Writes the footer of the GraphML file.
     * 
     * @param writer the writer to use
     * @throws Exception if an error occurs
     */
    protected void writeFooter(BufferedWriter writer) throws Exception {
      writer.write("</graph>");
      writer.newLine();
      writer.write("</graphml>");
      writer.newLine();
    }

    /**
     * Parses the incoming data and writes the generated output.
     * 
     * @param input the string read the BIF data from
     * @return the generated GraphML
     * @throws Exception if parsing or writing fails
     */
    public String convert(String input) throws Exception {
      BIFParser parser;
      ArrayList<GraphNode> nodes;
      ArrayList<GraphEdge> edges;
      StringWriter output;
      BufferedWriter writer;
      int i;

      // parse bif format
      nodes = new ArrayList<GraphNode>();
      edges = new ArrayList<GraphEdge>();
      parser = new BIFParser(input, nodes, edges);
      parser.parse();

      // generate GraphML output
      output = new StringWriter();
      writer = new BufferedWriter(output);
      writeHeader(writer);
      for (i = 0; i < nodes.size(); i++) {
        writeNodes(writer, nodes.get(i));
      }
      for (i = 0; i < edges.size(); i++) {
        writeEdges(writer, nodes, edges.get(i));
      }
      writeFooter(writer);
      writer.flush();

      return output.toString();
    }
  }

  /**
   * A panel for displaying a prefuse graph.
   * <p/>
   * Based on the <code>prefuse.demos.GraphView</code> demo.
   * 
   * @author fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 10237 $
   * @see prefuse.demos.GraphView
   */
  public final static class GraphPanel extends JPanel {

    /** for serialization. */
    private static final long serialVersionUID = 5943939093143764654L;

    /** the constant for "graph". */
    public final static String GRAPH = "graph";

    /** the constant for "graph.nodes". */
    public final static String GRAPH_NODES = "graph.nodes";

    /** the constant for "graph.edges". */
    public final static String GRAPH_EDGES = "graph.edges";

    /** the constant for "label". */
    public final static String LABEL = "label";

    /** the constant for "draw". */
    public final static String DRAW = "draw";

    /** the constant for "layout". */
    public final static String LAYOUT = "layout";

    /** for visualizing the graph. */
    protected Visualization m_vis;

    /**
     * Initializes the panel.
     * 
     * @param graph the graph to display
     */
    public GraphPanel(Graph graph) {
      super(new BorderLayout());

      m_vis = new Visualization();

      // --------------------------------------------------------------------
      // set up the renderers

      LabelRenderer tr = new LabelRenderer();
      tr.setRoundedCorner(8, 8);
      m_vis.setRendererFactory(new DefaultRendererFactory(tr));

      // --------------------------------------------------------------------
      // register the data with a visualization

      // adds graph to visualization and sets renderer label field
      DefaultRendererFactory drf = (DefaultRendererFactory) m_vis
        .getRendererFactory();
      ((LabelRenderer) drf.getDefaultRenderer()).setTextField(LABEL);

      // --------------------------------------------------------------------
      // create actions to process the visual data

      int hops = 30;
      final GraphDistanceFilter filter = new GraphDistanceFilter(GRAPH, hops);

      ColorAction fill = new ColorAction(GRAPH_NODES, VisualItem.FILLCOLOR,
        ColorLib.rgb(200, 200, 255));
      fill.add(VisualItem.FIXED, ColorLib.rgb(255, 100, 100));
      fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));

      ActionList draw = new ActionList();
      draw.add(filter);
      draw.add(fill);
      draw.add(new ColorAction(GRAPH_NODES, VisualItem.STROKECOLOR, 0));
      draw.add(new ColorAction(GRAPH_NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(
        0, 0, 0)));
      draw.add(new ColorAction(GRAPH_EDGES, VisualItem.FILLCOLOR, ColorLib
        .gray(200)));
      draw.add(new ColorAction(GRAPH_EDGES, VisualItem.STROKECOLOR, ColorLib
        .gray(200)));

      ActionList animate = new ActionList(Activity.INFINITY);
      animate.add(new ForceDirectedLayout(GRAPH));
      animate.add(fill);
      animate.add(new RepaintAction());

      // finally, we register our ActionList with the Visualization.
      // we can later execute our Actions by invoking a method on our
      // Visualization, using the name we've chosen below.
      m_vis.putAction(DRAW, draw);
      m_vis.putAction(LAYOUT, animate);

      m_vis.runAfter(DRAW, LAYOUT);

      // --------------------------------------------------------------------
      // set up a display to show the visualization

      Display display = new Display(m_vis);
      display.setSize(700, 700);
      display.pan(350, 350);
      display.setForeground(Color.GRAY);
      display.setBackground(Color.WHITE);

      // main display controls
      display.addControlListener(new FocusControl(1));
      display.addControlListener(new DragControl());
      display.addControlListener(new PanControl());
      display.addControlListener(new ZoomControl());
      display.addControlListener(new WheelZoomControl());
      display.addControlListener(new ZoomToFitControl());
      display.addControlListener(new NeighborHighlightControl());

      display.setForeground(Color.GRAY);
      display.setBackground(Color.WHITE);

      // --------------------------------------------------------------------
      // launch the visualization

      // create a panel for editing force values
      ForceSimulator fsim = ((ForceDirectedLayout) animate.get(0))
        .getForceSimulator();
      JForcePanel fpanel = new JForcePanel(fsim);

      final JValueSlider slider = new JValueSlider("Distance", 0, hops, hops);
      slider.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          filter.setDistance(slider.getValue().intValue());
          m_vis.run(DRAW);
        }
      });
      slider.setBackground(Color.WHITE);
      slider.setPreferredSize(new Dimension(300, 30));
      slider.setMaximumSize(new Dimension(300, 30));

      Box cf = new Box(BoxLayout.Y_AXIS);
      cf.add(slider);
      cf.setBorder(BorderFactory.createTitledBorder("Connectivity Filter"));
      fpanel.add(cf);

      // fpanel.add(opanel);

      fpanel.add(Box.createVerticalGlue());

      // create a new JSplitPane to present the interface
      JSplitPane split = new JSplitPane();
      split.setLeftComponent(display);
      split.setRightComponent(fpanel);
      split.setOneTouchExpandable(true);
      split.setContinuousLayout(false);
      split.setDividerLocation(700);

      add(split, BorderLayout.CENTER);

      // update graph
      m_vis.removeGroup(GRAPH);
      VisualGraph vg = m_vis.addGraph(GRAPH, graph);
      m_vis.setValue(GRAPH_EDGES, null, VisualItem.INTERACTIVE, Boolean.FALSE);
      VisualItem f = (VisualItem) vg.getNode(0);
      m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
      f.setFixed(false);

      // fix selected focus nodes
      TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS);
      focusGroup.addTupleSetListener(new TupleSetListener() {
        @Override
        public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
          for (Tuple element : rem) {
            ((VisualItem) element).setFixed(false);
          }
          for (Tuple element : add) {
            ((VisualItem) element).setFixed(false);
            ((VisualItem) element).setFixed(true);
          }
          if (ts.getTupleCount() == 0) {
            ts.addTuple(rem[0]);
            ((VisualItem) rem[0]).setFixed(false);
          }
          m_vis.run(DRAW);
        }
      });

      m_vis.run(DRAW);
    }
  }

  /**
   * Get the minimum version of Weka, inclusive, the class is designed to work
   * with. eg: <code>3.5.0</code>
   * 
   * @return the minimum version
   */
  @Override
  public String getMinVersion() {
    return "3.5.9";
  }

  /**
   * Get the maximum version of Weka, exclusive, the class is designed to work
   * with. eg: <code>3.6.0</code>
   * 
   * @return the maximum version
   */
  @Override
  public String getMaxVersion() {
    return "3.8.0";
  }

  /**
   * Get the specific version of Weka the class is designed for. eg:
   * <code>3.5.1</code>
   * 
   * @return the version the plugin was designed for
   */
  @Override
  public String getDesignVersion() {
    return "3.5.9";
  }

  /**
   * Get a JMenu or JMenuItem which contain action listeners that perform the
   * visualization of the graph in XML BIF format. Exceptions thrown because of
   * changes in Weka since compilation need to be caught by the implementer.
   * 
   * @see NoClassDefFoundError
   * @see IncompatibleClassChangeError
   * 
   * @param bif the graph in XML BIF format
   * @param name the name of the item (in the Explorer's history list)
   * @return menuitem for opening visualization(s), or null to indicate no
   *         visualization is applicable for the input
   */
  @Override
  public JMenuItem getVisualizeMenuItem(String bif, String name) {
    JMenuItem result;

    final String bifF = bif;
    final String nameF = name;
    result = new JMenuItem("Prefuse graph");
    result.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        display(bifF, nameF);
      }
    });

    return result;
  }

  /**
   * Displays the error.
   * 
   * @param msg the error to display
   */
  protected void displayError(String msg) {
    JOptionPane.showMessageDialog(null, msg, "Error displaying graph",
      JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Converts the XML BIF format to GraphML.
   * 
   * @param bif the graph in XML BIF format
   * @return the graph in GraphML or null in case of an error
   */
  protected String convert(String bif) {
    String result;
    BIFToGraphML d2gml;

    d2gml = new BIFToGraphML();
    try {
      result = d2gml.convert(bif);
    } catch (Exception e) {
      result = null;
      e.printStackTrace();
      displayError(e.toString());
    }

    return result;
  }

  /**
   * Parses the graph in GraphML and returns the built graph.
   * 
   * @param graphml the graph in GraphML
   * @return the graph or null in case of an error
   */
  protected Graph parse(String graphml) {
    ByteArrayInputStream inStream;
    Graph result;

    try {
      inStream = new ByteArrayInputStream(graphml.getBytes());
      result = new GraphMLReader().readGraph(inStream);
    } catch (Exception e) {
      result = null;
      e.printStackTrace();
      displayError(e.toString());
    }

    return result;
  }

  /**
   * Displays the graph.
   * 
   * @param bif the graph in XML BIF format
   * @param name the name of the graph
   */
  protected void display(String bif, String name) {
    String graphml;
    Graph graph;
    JPanel panel;
    JFrame frame;

    // convert bif graph
    graphml = convert(bif);
    if (graphml == null) {
      return;
    }

    // parse graph
    graph = parse(graphml);
    if (graph == null) {
      return;
    }

    // display graph
    panel = new GraphPanel(graph);
    frame = new JFrame("Prefuse graph [" + name + "]");
    frame.setSize(1000, 600);
    frame.setContentPane(panel);
    frame.setVisible(true);
  }
}
