package a4;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import functional.Supplier;
import graph.GraphPanel;

/** An instance is a JFrame with a flu-spreading tree and a panel to display information
 *
 * @author Mshnik */
public class FluJFrame extends JFrame {

	private static final long serialVersionUID= 1L; // Used for serializing

	/** Return a new FluFrame to display dt and s, the number of time steps to create it. */
	public static FluJFrame show(FluTree dt, int s) {
		return new FluJFrame(dt, s);
	}

	/** Store the screen width and height of the current screen's size. <br>
	 * This method is called when the class is initially loaded */
	static {
		Dimension s= Toolkit.getDefaultToolkit().getScreenSize();
		SCREEN_WIDTH= s.width;
		SCREEN_HEIGHT= s.height;
	}

	/** Width and height of the screen. */
	private static final int SCREEN_WIDTH, SCREEN_HEIGHT;

	/** Space between left of screen and window. */
	private static final int X_OFFSET= 50;

	/** Space between top of screen and window. */
	private static final int Y_OFFSET= 50;

	/** Minimum size of Info panel in windows. */
	private final int INFO_PANEL_MIN_WIDTH;

	/** Height of color bar. */
	private static final int WIDTH_COLOR_PANEL_HEIGHT= 38;

	/** Color for dead people in trees. */
	private static final Color DEAD_COLOR= Color.RED;

	/** Color of immune people in trees. */
	private static final Color IMMUNE_COLOR= new Color(14, 186, 11, 255);

	/** FluTree being displayed. */
	private FluTree root;

	/** Number of time steps it took to create tree root. */
	private int steps;

	/** Split pane that takes up center space. */
	private final JSplitPane splitPane= new JSplitPane();

	/** GraphPanel that shows the FluTree. */
	private GraphPanel<Person, PersonConnection> graphPanel;

	/** Display the width of the selected node. */
	private JLabel selectedDepthLabel= new JLabel();

	/** Display the width at the selected depth. */
	private JLabel selectedWidthLabel= new JLabel();

	/** Colored panel that highlights a particular depth level. */
	// private ColoredPanel widthColorPanel= new ColoredPanel(new Color(63, 191, 191, 178));
	private ColoredPanel widthColorPanel= new ColoredPanel(new Color(230, 191, 191, 178));

	/** Label displaying the "Depth". */
	private JLabel depthHeaderLabel= new JLabel("Depth");

	/** Labels that count up the depth labels. */
	private JLabel[] depthLabels;

	/** Display the number of frames in the simulation. */
	private JLabel framesLabel= new JLabel();

	/** Display the selected person. */
	private JLabel selectedPersonLabel= new JLabel();

	/** Display the parent of the selected person. */
	private JLabel selectedPersonParentLabel= new JLabel();

	/** Display the number of children of the selected person. */
	private JLabel selectedPersonChildrenCountLabel= new JLabel();

	/** Display the size of the subtree represented by the selected person. */
	private JLabel selectedPersonSubtreeCountLabel= new JLabel();

	/** Display the maximum depth of the subtree represented by the selected person, */
	private JLabel selectedPersonMaxDepthLabel= new JLabel();

	/** Display the maximum width of the subtree represented by the selected person. */
	private JLabel selectedPersonMaxWidthLabel= new JLabel();

	/** Display the previously selected person. */
	private JLabel twoPersonSelectedLabel= new JLabel();

	/** Display the flu route from the previously selected person to the selected person. */
	private JLabel twoPersonFluRouteLabel= new JLabel();

	/** Display the common ancestor of the previously selected person and the selected person. */
	private JLabel twoPersonCommonAncestorLabel= new JLabel();

	/** The previously selected person. */
	private Person previouslySelectedPerson;

	/** The currently selected person. */
	private Person selectedPerson;

	/** The circle that represents the currently selected person. */
	private GraphPanel<Person, PersonConnection>.Circle selectedCircle;

	/** The flu route from the previously selected person to the currently selected person. */
	private List<Person> fluRoutePrevToSelected;

	/** Constructor: a visible GUI for dt, with s time steps needed to construct it. */
	private FluJFrame(FluTree dt, int s) {
		super();
		setTitle("A4 Spreading the flu");

		root= dt;
		steps= s;
		getContentPane().add(splitPane, BorderLayout.CENTER);
		splitPane.setResizeWeight(1.0);

		fixGraphPanel(dt);
		splitPane.setRightComponent(graphPanel); // was left
		pack();

		JPanel infoPanel= new JPanel();
		fixInformationPanel(infoPanel);
		splitPane.setLeftComponent(infoPanel); // was right

		// Double pack so that infoPanel.getWidth() can be used here
		pack();
		INFO_PANEL_MIN_WIDTH= infoPanel.getWidth();
		infoPanel.setMinimumSize(new Dimension(INFO_PANEL_MIN_WIDTH, 0));
		graphPanel
			.setPreferredSize(new Dimension(SCREEN_WIDTH - INFO_PANEL_MIN_WIDTH - X_OFFSET * 2,
				SCREEN_HEIGHT - Y_OFFSET * 2));
		pack();
		setLocationRelativeTo(null);

		moveCirclesToCorrectLocations();

		splitPane.addPropertyChangeListener(new PropertyChangeListener() {
			/** Handle fixing layout when splitpane line moves */
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt != null &&
					evt.getPropertyName().equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
					widthColorPanel
						.setSize(new Dimension(graphPanel.getWidth(), widthColorPanel.getHeight()));
					layoutNodes();
				}
			}
		});
		createAndAddCircleMouseListener();

		// Apply colors
		colorNodes(dt);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	/** Create and add a circle mouse listener for selecting people to the graphPanel */
	private void createAndAddCircleMouseListener() {
		graphPanel.addCircleMouseListener(new MouseListener() {
			@Override
			@SuppressWarnings("unchecked")
			/** Handle change of info when a circle is clicked */
			public void mouseClicked(MouseEvent e) {
				selectedCircle= (GraphPanel<Person, PersonConnection>.Circle) e.getSource();

				Person p= selectedCircle.getRepresents();
				previouslySelectedPerson= selectedPerson;
				selectedPerson= p;

				try {
					fluRoutePrevToSelected= root.node(previouslySelectedPerson)
						.fluRouteTo(selectedPerson);
				} catch (Exception excp) {}

				widthColorPanel.setLocation(0,
					selectedCircle.getY1() - widthColorPanel.getHeight() / 2);

				populateLabels();
			}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		});
	}

	/** Move circles on the graphPanel to correct locations. */
	public void moveCirclesToCorrectLocations() {
		// Move circles to correct locations
		widthColorPanel.setSize(new Dimension(graphPanel.getWidth(), WIDTH_COLOR_PANEL_HEIGHT));
		widthColorPanel.setLocation(0, -WIDTH_COLOR_PANEL_HEIGHT);
		layoutNodes();
		addComponentListener(new ComponentListener() {
			/** Handle fixing layout when the window is resized */
			@Override
			public void componentResized(ComponentEvent e) {
				widthColorPanel
					.setSize(new Dimension(graphPanel.getWidth(), widthColorPanel.getHeight()));
				layoutNodes();

				if (selectedCircle != null) {
					widthColorPanel.setLocation(0,
						selectedCircle.getY1() - widthColorPanel.getHeight() / 2);
				}
			}

			@Override
			public void componentMoved(ComponentEvent e) {}

			@Override
			public void componentShown(ComponentEvent e) {}

			@Override
			public void componentHidden(ComponentEvent e) {}
		});

	}

	/** Store in field graphPanel the graph for dt. */
	private void fixGraphPanel(FluTree dt) {
		graphPanel= new GraphPanel<>(new Network(dt));
		graphPanel.setLinesBendable(false);
		graphPanel.setCirclesDraggable(false);
		graphPanel.setCircleStringToDrawFunc((c) -> c.getRepresents().name());
		graphPanel.setLineStringToDrawFunc((l) -> "");
		graphPanel.setLineStrokeFunc(this::getStrokeFor);

		graphPanel.add(widthColorPanel);

		try {
			int depth= dt.maxDepth();
			depthLabels= new JLabel[depth + 1];
			for (int i= 0; i <= depth; i= i + 1) {
				depthLabels[i]= new JLabel(i + "");
				depthLabels[i].setSize(new Dimension(50, 50));
				graphPanel.add(depthLabels[i]);
				graphPanel.setComponentZOrder(depthLabels[i], 0);
			}
			graphPanel.add(depthHeaderLabel);
			depthHeaderLabel.setSize(new Dimension(50, 20));
			depthHeaderLabel.setLocation(20, 2);
		} catch (Exception e) {
			depthLabels= null;
		}
	}

	/** Place in infoPanel all the labels that give information about the selected person(s) */
	private void fixInformationPanel(JPanel infoPanel) {
		infoPanel.setBackground(new Color(240, 235, 255));
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

//		infoPanel.setBackground(new Color(216, 255, 235));
//		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

		infoPanel.add(new JLabel("                                                             "));
		infoPanel.add(framesLabel);
		infoPanel.add(new JLabel("                                                             "));
		infoPanel.add(selectedDepthLabel);
		infoPanel.add(selectedWidthLabel);
		infoPanel.add(new JLabel("  "));
		infoPanel.add(selectedPersonLabel);
		infoPanel.add(selectedPersonParentLabel);
		infoPanel.add(selectedPersonChildrenCountLabel);
		infoPanel.add(selectedPersonSubtreeCountLabel);
		infoPanel.add(selectedPersonMaxDepthLabel);
		infoPanel.add(selectedPersonMaxWidthLabel);
		infoPanel.add(new JLabel("  "));
		infoPanel.add(twoPersonSelectedLabel);
		infoPanel.add(twoPersonFluRouteLabel);
		infoPanel.add(twoPersonCommonAncestorLabel);
	}

	/** Lay out the nodes in the FluTree. Specifically, call recLayoutNodes(...) [below], with
	 * particular starting arguments, and set the location of all of the depth labels. */
	private void layoutNodes() {
		float yPercent= 0.05f;
		float yInc= 1.0f / (root.maxDepth() + 1);

		recLayoutNodes(root, 0.025f, 0.975f, yPercent, yInc);
		if (depthLabels != null) {
			for (int i= 0; i < depthLabels.length; i++ ) {
				depthLabels[i].setLocation(20, (int) (yPercent * graphPanel.getHeight()) - 30);
				yPercent+= yInc;
			}
		}
	}

	/** Lay out the nodes of dt on the GraphPanel.<br>
	 * xMin is the minimum percentage of the width this layout is allowed to take.<br>
	 * xMax is the maximum percentage of the width this layout is allowed to take.<br>
	 * yPercent is the current height percentage for this recursive call.<br>
	 * yInc is the amount to increase yPercent in each recursive call.<br>
	 * Precondition: 0 <= xMin <= yMax <= 1. */
	private void recLayoutNodes(FluTree dt, float xMin, float xMax, float yPercent,
		float yInc) {
		float xCenter= (xMin + xMax) / 2.0f;
		GraphPanel<Person, PersonConnection>.Circle c= graphPanel.getCircle(dt.rootPerson());

		c.setX1((int) (graphPanel.getWidth() * xCenter));
		c.setY1((int) (graphPanel.getHeight() * yPercent));

		float maxWidthTotals= 0.0f;
		for (FluTree child : dt.copyOfChildren()) {
			maxWidthTotals+= child.maxWidth();
		}

		float xInc= (xMax - xMin) / maxWidthTotals;
		yPercent+= yInc;
		for (FluTree child : dt.copyOfChildren()) {
			float width= child.maxWidth();
			recLayoutNodes(child, xMin, xMin + xInc * width, yPercent, yInc);
			xMin+= xInc * width;
		}
	}

	/** Return a stroke (for drawing) for line l. If the line is part of the fluRoute, <br>
	 * return a bolded stroke; otherwise, a simple stroke. */
	private Stroke getStrokeFor(GraphPanel<Person, PersonConnection>.Line l) {
		if (fluRoutePrevToSelected != null) {
			Person p1= l.getC1().getRepresents();
			Person p2= l.getC2().getRepresents();
			if (fluRoutePrevToSelected.contains(p1) &&
				fluRoutePrevToSelected.contains(p2)) {
				return new BasicStroke(3.0f);
			}
		}
		return new BasicStroke();
	}

	/** Color the nodes of dt. <br>
	 * Nodes for dead people are colored DEAD_COLOR. Nodes for immune people are colored
	 * IMMUNE_COLOR */
	private void colorNodes(FluTree dt) {
		GraphPanel<Person, PersonConnection>.Circle c= graphPanel.getCircle(dt.rootPerson());
		c.setColor(dt.rootPerson().isDead() ? DEAD_COLOR : IMMUNE_COLOR);
		for (FluTree child : dt.copyOfChildren()) {
			colorNodes(child);
		}
	}

	/** Repopulate the information labels that have been added to the infoPanel. Uses lambdas - not
	 * covered in CS2110, you'll learn about them in CS3110 */
	private void populateLabels() {
		framesLabel.setText("No. steps in simulation: " + steps);

		Person p= selectedPerson;
		populateLabel(() -> root.depth(p), selectedDepthLabel, "Selected Person Depth");
		populateLabel(() -> root.widthAtDepth(root.depth(p)), selectedWidthLabel,
			"Selected Level Width");
		populateLabel(() -> p.name(), selectedPersonLabel, "Selected Person");

		populateLabel(() -> {
			Person parent= root.getParent(p);
			if (parent != null) return parent.name();
			else return "null";
		}, selectedPersonParentLabel, "Parent");

		populateLabel(() -> root.node(p).childrenSize(), selectedPersonChildrenCountLabel,
			"Children");
		populateLabel(() -> root.node(p).size(), selectedPersonSubtreeCountLabel,
			"Subtree Size");
		populateLabel(() -> root.node(p).maxDepth(), selectedPersonMaxDepthLabel,
			"Subtree height");
		populateLabel(() -> root.node(p).maxWidth(), selectedPersonMaxWidthLabel,
			"Subtree width");

		Person pp= previouslySelectedPerson;
		populateLabel(() -> (pp != null ? pp.name() : "null") +
			" and " + p.name(), twoPersonSelectedLabel, "Selected People");
		if (pp != null) {
			populateLabel(() -> {
				List<Person> route= root.node(pp).fluRouteTo(p);
				if (route == null) return null;
				else return Arrays.deepToString(route.stream().map(a -> a.name()).toArray());
			}, twoPersonFluRouteLabel, "Flu Route");
			populateLabel(() -> {
				Person parent= root.commonAncestor(pp, p);
				return parent == null ? "null" : parent.name();
			}, twoPersonCommonAncestorLabel, "Common Ancestor");
		}
	}

	/** Call f and put its output into l.<br>
	 * If f returns, set l to text catenated with the output of the call.<br>
	 * If the call results in an exception, set the text of l to text and the type of exception
	 * instead. Uses lambdas - not covered in CS2110, you'll learn about them in CS3110 */
	private static void populateLabel(Supplier<?> f, JLabel l, String text) {
		final String INDENT= "   ";
		try {
			l.setText(INDENT + text + ": " + f.apply() + INDENT);
		} catch (Throwable t) {
			l.setText(INDENT + text + ": " + "Exception occurred - " + t.toString() + INDENT);
		}
	}

	/** An instance is a simple colored rectangle that is used to highlight a particular depth level
	 * in the tree.
	 *
	 * @author Patashnik */
	@SuppressWarnings("serial")
	private static class ColoredPanel extends JPanel {
		private Color color;

		/** Constructor: a new ColoredPanel with the specified color. */
		private ColoredPanel(Color c) {
			color= c;
		}

		/** Paint this ColoredPanel by drawing a rectangle of its color, filling the whole area of
		 * the Panel. */

		public @Override void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(color);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}
}
