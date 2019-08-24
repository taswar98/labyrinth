package labyrinth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class Graph<L> implements AbsGraph<L> {
	
	private int eCount = 0;
	private int vCount = 0;
	
	HashMap<L,LinkedList<L>> adj;

	public Graph() {
		adj = new HashMap<>();
	}
	
	@Override
	public int numVertices() {
		return vCount;
	}

	@Override
	public int numEdges() {
		return eCount;
	}

	@Override
	public void addVertex(L key) {
		if(!adj.containsKey(key)) {
			adj.put(key, new LinkedList<>());
			vCount++;
		}
	}

	@Override
	public void addEdge(L from, L to) {
		if(vCount != 0 && adj.containsKey(from) && adj.containsKey(to)) {
			adj.get(from).add(to);
			adj.get(to).add(from);
			eCount++;
		}
	}

	@Override
	public Iterator<L> findPath(L from, L to) {
		ArrayList<L> visited = new ArrayList<>();
		ArrayList<L> path = new ArrayList<>();
		try {
			DFS(from, to, visited,path);
		} catch (Exception e) {
			
		}
		return path.iterator();
	}	
	
	private void DFS(L from , L to, ArrayList<L> visited, ArrayList<L> path) throws Exception {
		visited.add(from);
		path.add(from);
		if(from.equals(to)) {
			throw new Exception("Target Found");
		}
		Iterator<L> i = adj.get(from).iterator();
		while(i.hasNext()) {
			L l = i.next();
			if(!visited.contains(l)) {
				DFS(l,to,visited,path);
			}
		}
		path.remove(from);
	}	
}