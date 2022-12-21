package cmsc420_f22;

import java.util.ArrayList;

/*
 * Author: Adrian Paras
 * For: CMSC420
 * Date: Fall 2022
 * 
 * Description: This class implements the greedy facility location algorithm
 * by utilizing the two data structures created thus far, the kd-tree and the 
 * leftist heap.
 * 
 */
public class KCapFL<LPoint extends LabeledPoint2D> {

	private int capacity;
	private XkdTree<LPoint> kdTree;
	private LeftistHeap<Double, ArrayList<LPoint>> heap;

	/*
	 * Constructor initializing the capacity, a kd-tree and a leftist heap
	 */
	public KCapFL(int capacity, int bucketSize, Rectangle2D bbox) {
		this.capacity = capacity;
		kdTree = new XkdTree<LPoint>(bucketSize, bbox);
		heap = new LeftistHeap<Double, ArrayList<LPoint>>();
	}

	/*
	 * Clears the kd-tree and heap
	 */
	public void clear() {
		kdTree.clear();
		heap.clear();
	}

	/*
	 * Builds the kd-tree by performing a bulk insert of the pts given, along with
	 * tge leftist heap by computing the k nearest neighbors of each point, and the
	 * squared distances to the farthest point given a center point.
	 */
	public void build(ArrayList<LPoint> pts) throws Exception {
		if (pts.size() == 0 || pts.size() % capacity != 0) {
			throw new Exception("Invalid point set size");
		}
		kdTree.bulkInsert(pts);
		for (LPoint pt : pts) {
			ArrayList<LPoint> neighbors = new ArrayList<>(kdTree.kNearestNeighbor(pt.getPoint2D(), capacity));
			LPoint centerPoint = neighbors.get(0);
			double sqDist = centerPoint.getPoint2D().distanceSq(neighbors.get(capacity - 1).getPoint2D());
			heap.insert(sqDist, neighbors);
		}
	}

	/*
	 * Repeatedly extracts clusters from the leftist heap until it is found that
	 * every point is still in the kd-tree, in which all of them will be deleted
	 */
	public ArrayList<LPoint> extractCluster() {
		ArrayList<LPoint> neighbors = null;
		try {
			neighbors = new ArrayList<>(heap.extractMin());
		} catch (Exception e) {
			return neighbors;
		}
		boolean stillInTree = true;
		for (LPoint pt : neighbors) {
			if (kdTree.find(pt.getPoint2D()) == null) {
				stillInTree = false;
				break;
			}
		}
		if (!stillInTree) {
			LPoint centerPoint = neighbors.get(0);
			if (kdTree.find(centerPoint.getPoint2D()) != null) {
				ArrayList<LPoint> newList = kdTree.kNearestNeighbor(centerPoint.getPoint2D(), capacity);
				double sqDist = centerPoint.getPoint2D().distanceSq(newList.get(capacity - 1).getPoint2D());
				heap.insert(sqDist, newList);
			}
			return extractCluster();
		} else {
			for (LPoint pt : neighbors) {
				try {
					kdTree.delete(pt.getPoint2D());
				} catch (Exception e) {
					return null;
				}
			}
			return new ArrayList<>(neighbors);
		}
	}

	/*
	 * pre-order right-left traversal of the kd-tree structure, stored as a list of
	 * strings
	 */
	public ArrayList<String> listKdTree() {
		return new ArrayList<>(kdTree.list());
	}

	/*
	 *  returns a pre_order traversal of the LeftistHeap
	 */
	public ArrayList<String> listHeap() {
		return new ArrayList<>(heap.list());
	}
}
