package labyrinth;

import java.io.*;
//import java.lang.Math.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.awt.*;
import javax.swing.*;


//Author: taswar

/*==========================================================================*/
public class Labyrinth {
 private int N;               // The size of our matrix for the Labyrinth.
	private long theSeed = 0;    // Says which labyrinth to make.
	                             // (The seed for random generator).
	private Random ranGen;       // Our random generator object.

 private int entry;           // Entrance cell to the Labyrinth.
 private int exit;            // Exit cell from the Labyrinth.
 private LabyCell[][] cells;  // Cells of the matrix.
	// Map cells by labels for easy access.
	private TreeMap<Integer, LabyCell> cellMap;
	private int counter = 0;     // Used to label LabyCell's

	private ArrayList<Integer> path; // A path in the Labyrinth.

	private JFrame frame;        // Holds frame for graphical display.

/*----------------------------------------------------------------------------
Labyrinth Constructor : Construct a labyrinth on a Size X Size grid.
----------------------------------------------------------------------------*/
 public Labyrinth (int size) {
		this(size, System.currentTimeMillis());
	}

 public Labyrinth (int size, long seed) {
		// Check that size is not preposterous.
		if (size < 0) {
			System.out.println("Do not know how to make negative sized "
				+ "labyrinths.  Sorry.  Bailing.");
			System.exit(-1);
		}
		if (size < 2) {
			System.out.println("A labyrinth of size one?  "
				+ "Get real.  What's the point?  Bailing.");
			System.exit(-1);
		}

     N       = size;
		theSeed = seed;
     cells   = new LabyCell[N+2][N+2];
		cellMap = new TreeMap<Integer, LabyCell>();
		path    = new ArrayList<Integer>();

     // Tell each cell its position.
     for (int i = 0; i <= N+1; i++) {
         for (int j = 0; j <= N+1; j++) {
             cells[i][j] = new LabyCell(i, j);
				cellMap.put(cells[i][j].label(), cells[i][j]);
         }
     }

		// Set up the random generator.
		ranGen = new Random(theSeed);

     // Generate the labyrinth.
     make();
		System.out.println("Labyrinth " + size + " " + theSeed + ".");

     // Display the labyrinth.
		display();
 }

/*----------------------------------------------------------------------------
LabyCell : one cell of the Labyrinth matrix.
----------------------------------------------------------------------------*/
 private class LabyCell {
     // Row position.
     private int row = 0;
     // Column position.
     private int col = 0;

     // Label.
		private int theLabel = counter++;

     // Wall in that direction?
     public boolean north = true;
     public boolean east  = true;
     public boolean south = true;
     public boolean west  = true;

     // Reachable from start cell?
     public int reachable = 0; // Zero is not reachable from anyone.
     // Whether forbidden to enter.  (For border cells.)
     public boolean forbidden = false;

     public void setPos(int R, int C) {
         row = R;
         col = C;
     }

     public LabyCell(int i, int j) {
         row = i;
         col = j;
     }

		public Integer label() {
			return new Integer(theLabel);
		}
 }

/*----------------------------------------------------------------------------
LabyPair : a pair of (adjacent) cells of the Labyrinth matrix.
----------------------------------------------------------------------------*/
 private class LabyPair {
		private LabyCell oneCell;
		private LabyCell twoCell;

		public LabyCell one() {
			return oneCell;
		}

		public LabyCell two() {
			return twoCell;
		}

     public LabyPair(LabyCell A, LabyCell B) {
			oneCell = A;
			twoCell = B;
		}
 }

/*----------------------------------------------------------------------------
make() : Make the labyrinth.
----------------------------------------------------------------------------*/
 private void make() {
     // Drop walls of the northern border cells.
     for (int j=0; j <= N+1; j++) {
         cells[0][j].north = false;
         cells[0][j].east  = false;
         cells[0][j].west  = false;
         cells[0][j].forbidden = true;
     }
     // Drop walls of the eastern border cells.
     for (int i=0; i <= N+1; i++) {
         cells[i][N+1].north = false;
         cells[i][N+1].east  = false;
         cells[i][N+1].south = false;
         cells[i][N+1].forbidden = true;
     }
     // Drop walls of the southern border cells.
     for (int j=0; j <= N+1; j++) {
         cells[N+1][j].east  = false;
         cells[N+1][j].south = false;
         cells[N+1][j].west  = false;
         cells[N+1][j].forbidden = true;
     }
     // Drop walls of the western border cells.
     for (int i=0; i <= N+1; i++) {
         cells[i][0].north = false;
         cells[i][0].south = false;
         cells[i][0].west  = false;
         cells[i][0].forbidden = true;
     }

     // Choose an entry cell (BEGIN) on the NORTH.
     entry = ranGen.nextInt(N) + 1;
		// Breakdown wall.  (Walls have two sides!)
     cells[1][entry].north = false;
     cells[0][entry].south = false;

     // Set the beginnings of a path.
     path.add(cells[0][entry].label());
     path.add(cells[1][entry].label());

     // Choose an exit cell (END) on the SOUTH.
     exit  = ranGen.nextInt(N) + 1;
		// Breakdown wall.  (Walls have two sides!)
     cells[N][exit].south = false;
     cells[N+1][exit].north = false;

     // Process cells.  Connect the graph.
		Map<Integer, ArrayList<LabyCell>> islands =
			new TreeMap<Integer, ArrayList<LabyCell>>();
		ArrayList<Integer> choices = new ArrayList<Integer>();

		// Every cell starts out disconnected.  It's its own island.
		int iNum = 0;
		Integer INum;
		ArrayList<LabyCell> group;
		for (int i = 1; i <= N; i++) {
			for (int j = 1; j <= N; j++) {
				group = new ArrayList<LabyCell>();
				group.add(cells[i][j]);
				cells[i][j].reachable = iNum;
				INum = new Integer(iNum);
				islands.put(INum, group);
				choices.add(INum);
				iNum++;
			}
		}

		// While more than one island:
		//   1. Pick an island.
		//   2. Find its boundardy cells.
		//   3. Pick a boundary cell.
		//   4. Merge it with the island on the other side of boundary cell.
		ArrayList<LabyPair> border;
		LabyPair  crossing;
		ArrayList<LabyCell> other;
		LabyCell  cell;
		while (choices.size() > 1) {
			INum  = choices.remove(ranGen.nextInt(choices.size()));
			group = islands.remove(INum);

			border   = findBorder(group);
			crossing = border.get(ranGen.nextInt(border.size()));

			// Knockdown wall.
			if (crossing.one().row == crossing.two().row - 1) {
				crossing.one().south = false;
				crossing.two().north = false;
			} else if (crossing.one().col == crossing.two().col - 1) {
				crossing.one().east = false;
				crossing.two().west = false;
			} else if (crossing.one().row == crossing.two().row + 1) {
				crossing.one().north = false;
				crossing.two().south = false;
			} else {
				crossing.one().west = false;
				crossing.two().east = false;
			}

			// Add island to the other island.
			iNum  = crossing.two().reachable;
			INum  = new Integer(iNum);
			other = islands.get(INum);
			for (int i = 0; i < group.size(); i++) {
				cell = (LabyCell)group.get(i);
				cell.reachable = iNum;
				other.add(cell);
			}
		}
 }

/*----------------------------------------------------------------------------
findBorder(...) : These are the island's border cells and neighbours.
----------------------------------------------------------------------------*/
private ArrayList<LabyPair> findBorder(ArrayList<LabyCell> island) {
	LabyCell cell;
	ArrayList<LabyPair> border = new ArrayList<LabyPair>();
	LabyPair crossing;

	for (int i = 0; i < island.size(); i++) {
		cell = (LabyCell)island.get(i);
		if (!cells[cell.row-1][cell.col].forbidden &&
				(cells[cell.row-1][cell.col].reachable != cell.reachable)) {
			crossing = new LabyPair(
								cell,
								cells[cell.row-1][cell.col]
							);
			border.add(crossing);
		}
		if (!cells[cell.row][cell.col-1].forbidden &&
				(cells[cell.row][cell.col-1].reachable != cell.reachable)) {
			crossing = new LabyPair(
								cell,
								cells[cell.row][cell.col-1]
							);
			border.add(crossing);
		}
		if (!cells[cell.row+1][cell.col].forbidden &&
				(cells[cell.row+1][cell.col].reachable != cell.reachable)) {
			crossing = new LabyPair(
								cell,
								cells[cell.row+1][cell.col]
							);
			border.add(crossing);
		}
		if (!cells[cell.row][cell.col+1].forbidden &&
				(cells[cell.row][cell.col+1].reachable != cell.reachable)) {
			crossing = new LabyPair(
								 cell,
								 cells[cell.row][cell.col+1]
							);
			border.add(crossing);
		}
	}

	return border;
}

/*============================================================================
PATH methods : Manipulating the path.
------------------------------------------------------------------------------
clearPath() : Clear the path.
----------------------------------------------------------------------------*/
	public void clearPath () {
		path.clear();

		// If a graphics window is displayed, update it.
		if (view != null) {
			view.update(view.getGraphics());
		}
	}

/*----------------------------------------------------------------------------
addToPath(int v) : Add vertex label to end of path.
----------------------------------------------------------------------------*/
	public void addToPath (int v) {
		Integer V = new Integer(v);

		if (cellMap.containsKey(V)) {
			path.add(V);
		}

		// If a graphics window is displayed, update it.
		if (view != null) {
			view.update(view.getGraphics());
		}
	}

/*----------------------------------------------------------------------------
asciiShow() : Prints an ASCII representation of the labyrinth to the window.
----------------------------------------------------------------------------*/
 public void asciiShow() {
     for (int i = 0; i <= N+1; i++) {
         // Print North walls.
         for (int j = 0; j <= N+1; j++) {
             if (cells[i][j].north) {
                 System.out.print(" -");
             } else {
                 System.out.print("  ");
             }
         }
         System.out.println();

         // Print West walls.
         for (int j = 0; j <= N+1; j++) {
             if (cells[i][j].west) {
                 System.out.print("|");
             } else {
                 System.out.print(" ");
             }
             if (cells[i][j].reachable == 1) {
                 System.out.print(" "); // Print 'A' instead for debugging.
             } else if (cells[i][j].reachable == 2) {
                 System.out.print(" "); // Print 'Z' instead for debugging.
             } else {
                 System.out.print(" ");
             }
         }
         System.out.println();
     }
 }

/*============================================================================
GRAPHICS : Methods to provide a graphical window pop-up showing the labyrinth.
----------------------------------------------------------------------------*/
	protected Component view;
	protected Dimension dim;

	private static final int ROOM_SIZE      = 10;
	private static final int WALL_THICKNESS =  2;
	private static final int MARGIN         = 10;

/*----------------------------------------------------------------------------
display : Show the maze in a graphical window.
----------------------------------------------------------------------------*/
public void display () {
	// Show in a window.
	frame = new JFrame("Labyrinth " + N + " " + theSeed);
	frame.setContentPane(new Labyrinth.LabyPanel(this));
	frame.pack();
	Dimension frameDim = frame.getSize();
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	frame.setLocation(screenSize.width / 2 - frameDim.width / 2,
		screenSize.height / 2 - frameDim.height / 2);
	frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	frame.setVisible(true);
}

/*----------------------------------------------------------------------------
LabyPanel : Build a graphical panel for displaying the labyrinth.
----------------------------------------------------------------------------*/
	public static class LabyPanel extends JPanel {
		private Labyrinth labyrinth;
		private Dimension gdim;

		public LabyPanel(Labyrinth L) {
			labyrinth = L;

			labyrinth.setView(this);
			Dimension d = labyrinth.getDimension();
			gdim = new Dimension(d.width * ROOM_SIZE + 2 * MARGIN,
							d.height * ROOM_SIZE + 2 * MARGIN);
		}

		public void paint(Graphics G) {
			Dimension D = getSize();
			G.setColor(Color.gray);
			G.fillRect(0, 0, D.width, D.height);
			if (labyrinth != null) {
				labyrinth.draw(G);
				labyrinth.drawPath(G);
			}

			requestFocus();
		}

		public boolean isFocusable() { // v1.4 and up!
			return true;
		}

		public Dimension getPreferredSize() {
			return gdim;
		}

		public Dimension getMinimumSize() {
			return gdim;
		}
	}

/*----------------------------------------------------------------------------
setView(Component) : Link the "component" being viewed.
----------------------------------------------------------------------------*/
	protected void setView(Component V) {
		view = V;
	}

/*----------------------------------------------------------------------------
getDimension() : Report the graphic dimension.
----------------------------------------------------------------------------*/
	public Dimension getDimension() {
		if (dim == null) {
			dim = new Dimension(N + 2, N + 2);
		}
		return dim;
	}

/*----------------------------------------------------------------------------
draw(Graphics G) : Instructions to draw the labyrinth.
----------------------------------------------------------------------------*/
	public void draw(Graphics G) {
		if (dim == null) {
			getDimension();
		}

		int dx = MARGIN;
		int dy = MARGIN;

		G.setColor(Color.black);

		int ddx;
		int ddy = dy;
		for (int i = 0; i <= N + 1; i++) {
			ddx = dx;
			for (int j = 0; j <= N + 1; j++) {
				// Draw North wall, if there.
				if (cells[i][j].north == true) {
					G.fillRect(ddx, ddy, ROOM_SIZE, WALL_THICKNESS);
				}

				// Draw West wall, if there.
				if (cells[i][j].west == true) {
					G.fillRect(ddx, ddy, WALL_THICKNESS,
						ROOM_SIZE + WALL_THICKNESS);
				}

				ddx += ROOM_SIZE;
			}
			ddy += ROOM_SIZE;
		}
	}

/*----------------------------------------------------------------------------
drawPath(Graphics G) : Instructions to draw a path in the labyrinth.
----------------------------------------------------------------------------*/
	public void drawPath(Graphics G) {
		if (dim == null) {
			getDimension();
		}

		int dx = MARGIN + 1;
		int dy = MARGIN + 1;

		G.setColor(Color.red);

		Iterator<Integer> walk = path.iterator();
		LabyCell prev;
		LabyCell cell;
		Integer L;

		if (!walk.hasNext()) return;

		L = walk.next();
		prev = (LabyCell)cellMap.get(L);
		while (walk.hasNext()) {
			L = (Integer)walk.next();
			cell = (LabyCell)cellMap.get(L);

			G.drawLine(dx + (prev.col * ROOM_SIZE) + (ROOM_SIZE / 2),
					   dy + (prev.row * ROOM_SIZE) + (ROOM_SIZE / 2),
					   dx + (cell.col * ROOM_SIZE) + (ROOM_SIZE / 2),
					   dy + (cell.row * ROOM_SIZE) + (ROOM_SIZE / 2));
			prev = cell;
		}
	}

/*============================================================================
GRAPH : Handshake with Graph to get a solution to the Labyrinth.
------------------------------------------------------------------------------
solution : Call Graph, load the Labyrinth's vertices and edges,
	and retrieve the shortest-path solution.
----------------------------------------------------------------------------*/
	public void solution (AbsGraph<Integer> G) {
		// Check that graph is empty.  If not, complain!
		if (G.numVertices() != 0) {
			System.out.println(
				"Labyrinth has been provided with a non-empty Graph object"
			);
			System.out.println(
				"to find a path through the maze."
			);
			System.out.println(
				"Unacceptable.  Bailing."
			);
			System.exit(-1);
		}

		// Add BEGIN vertex.
		G.addVertex(cells[0][entry].label());  // BEGIN

		// Add END vertex.
		G.addVertex(cells[N+1][exit].label()); // END

		// Add rest of vertices.
		for (int i = 1; i <= N; i++) {
			for (int j = 1; j <= N; j++) {
				G.addVertex(cells[i][j].label());
			}
		}

		// Add BEGIN's edge out.
		G.addEdge(cells[0][entry].label(), cells[1][entry].label());
		G.addEdge(cells[1][entry].label(), cells[0][entry].label());
		// Add END's edge in.
		G.addEdge(cells[N][exit].label(), cells[N+1][exit].label());
		G.addEdge(cells[N+1][exit].label(), cells[N][exit].label());

		// Add rest of edges.
		for (int i = 1; i <= N; i++) {
			for (int j = 1; j <= N; j++) {
				if (!cells[i][j].north) {
					G.addEdge(cells[i][j].label(), cells[i-1][j].label());
				}
				if (!cells[i][j].east) {
					G.addEdge(cells[i][j].label(), cells[i][j+1].label());
				}
				if (!cells[i][j].south) {
					G.addEdge(cells[i][j].label(), cells[i+1][j].label());
				}
				if (!cells[i][j].west) {
					G.addEdge(cells[i][j].label(), cells[i][j-1].label());
				}
			}
		}

		// show the labyrinth
		if (view != null)
			view.update(view.getGraphics());

		// Ask for the shortest path.
		Iterator<Integer> walk = G.findPath(cells[0][entry].label(),
										    cells[N+1][exit].label());
		path.clear();
		while (walk.hasNext())
			path.add(walk.next());

		// update the labyrinth with the path drawn
		if (view != null)
			view.repaint();
	}
}
