package labyrinth;

import java.util.Iterator;

public interface AbsGraph<L> {
	int  numVertices();
	int  numEdges();
	void addVertex(L key);
	void addEdge(L from, L to);
	Iterator<L> findPath(L from, L to);
}