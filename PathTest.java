package labyrinth;

import java.io.*;
import java.awt.*;
import javax.swing.*;

/*==========================================================================*/
public class PathTest {

/*============================================================================
MAIN : Built-in main for testing out Labyrinth and Graph
----------------------------------------------------------------------------*/
    public static void main (String[] args) {
        // args[0] = size of Labyrinth to make
        if (args.length < 1) {
            System.out.println("PathTest : size [labnum]");
            System.exit(-1);
        }

        // Parse out the size.
        int size = 0;
        try {
            size = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("First argument must be an INT.");
            System.out.println("PathTest : size [labnum]");
            System.exit(-1);
        }

		long labNum = 0;
        if (args.length > 1) {
			try {
				labNum = Long.parseLong(args[1]);
			} catch (NumberFormatException nfe) {
				System.out.println("Second argument must be a LONG.");
				System.out.println("PathTest : size [labnum]");
				System.exit(-1);
			}
		}

		System.out.println("Making a Labyrinth.");
		Labyrinth laby;
		if (args.length < 2) { // No Labyrinth number provided.
			laby = new Labyrinth(size);
		} else {               // Labyrinth number provided.
			laby = new Labyrinth(size, labNum);
		}

		System.out.println("Making a Graph.");
		Graph<Integer> grph = new Graph<Integer>();

		System.out.println("Finding solution.");
		laby.solution(grph);

		System.out.println("At the end.");
    }
}
