package cmsc420_f22;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
 * Author: Adrian Paras
 * For: CMSC420
 * Date: Fall 2022
 * 
 * Description: This implementation is a variant of the kd-tree data
 * structure. It stores points in 2-dimensional space and distinguishes
 * between internal and external nodes, where internal nodes store cut
 * dimensions/cut values and have pointers to other internal or external 
 * nodes. External nodes are used to store the actual points, and cannot
 * exceed the specified bucket size.
 * 
 */

public class XkdTree<LPoint extends LabeledPoint2D> {

	/*
	 * Abstract node class defining the helper functions internal/external nodes
	 * will contain.
	 */
	private abstract class Node {
		abstract LPoint find(Point2D pt, Node root);

		abstract Node bulkInsert(ArrayList<LPoint> pts, Node root);

		abstract Point2D nearestNeighbor(Point2D q, Node root, Rectangle2D cell, Point2D best);

		abstract void list(ArrayList<String> list, Node root);

		abstract Node delete(Point2D pt, Node root) throws Exception;

		abstract void kNNHelper(Point2D q, Node root, Rectangle2D cell, @SuppressWarnings("rawtypes") MinK minK);
	}

	/*
	 * Internal node class specifying a cut dimension and cut value, along with left
	 * and right pointers to other internal/external nodes.
	 */
	private class InternalNode extends Node {
		private Node left, right;
		private int cutDim;
		private double cutVal;

		/*
		 * Constructor that initializes the internal node with the specified cut
		 * dimension and cut value.
		 */
		private InternalNode(int cutDim, double cutVal) {
			this.cutDim = cutDim;
			this.cutVal = cutVal;
		}

		/*
		 * Helper find function for an internal node that will traverse the left or
		 * right subtree based on the cut dimensions and cut values.
		 */
		LPoint find(Point2D q, Node root) {
			InternalNode n = (InternalNode) root;
			if (q.get(n.cutDim) == n.cutVal) {
				LPoint l = null, r = null;
				l = n.left.find(q, n.left);
				r = n.right.find(q, n.right);
				if (l != null) {
					return l;
				} else {
					return r;
				}
			} else {
				if (q.get(n.cutDim) < n.cutVal) {
					return n.left.find(q, n.left);
				} else {
					return n.right.find(q, n.right);
				}
			}
		}

		/*
		 * Helper function for bulkInsert that will recursively split the pts list to be
		 * placed in the left and right subtrees based on the cut dimension and cut
		 * value.
		 */
		Node bulkInsert(ArrayList<LPoint> pts, Node root) {
			InternalNode n = (InternalNode) root;
			int cutDim = n.cutDim;
			double cutVal = n.cutVal;
			int index = 0;
			if (cutDim == 0) {
				Collections.sort(pts, new sortByXThenY());
				for (LPoint pt : pts) {
					if (pt.getX() >= cutVal) {
						break;
					}
					index++;
				}
			} else {
				Collections.sort(pts, new sortByYThenX());
				for (LPoint pt : pts) {
					if (pt.getY() >= cutVal) {
						break;
					}
					index++;
				}
			}
			ArrayList<LPoint> left = new ArrayList<>(pts.subList(0, index));
			ArrayList<LPoint> right = new ArrayList<>(pts.subList(index, pts.size()));
			n.left = n.left.bulkInsert(left, n.left);
			n.right = n.right.bulkInsert(right, n.right);
			return n;
		}

		/*
		 * Helper function to find the nearest neighbor given that the node is internal.
		 */
		Point2D nearestNeighbor(Point2D q, Node root, Rectangle2D cell, Point2D best) {
			InternalNode n = (InternalNode) root;
			int cutDim = n.cutDim;
			double cutVal = n.cutVal;
			Rectangle2D leftCell = cell.leftPart(cutDim, cutVal);
			Rectangle2D rightCell = cell.rightPart(cutDim, cutVal);
			if (q.get(cutDim) >= cutVal) {
				best = n.right.nearestNeighbor(q, n.right, rightCell, best);
				if (leftCell.distanceSq(q) < q.distanceSq(best)) {
					best = n.left.nearestNeighbor(q, n.left, leftCell, best);
				}
			} else {
				best = n.left.nearestNeighbor(q, n.left, leftCell, best);
				if (rightCell.distanceSq(q) < q.distanceSq(best)) {
					best = n.right.nearestNeighbor(q, n.right, rightCell, best);
				}
			}
			return best;
		}

		/*
		 * Internal node helper function for list. Stores values x or y based on cut
		 * dimension along with their cut value.
		 */
		void list(ArrayList<String> list, Node root) {
			InternalNode n = (InternalNode) root;
			if (n.cutDim == 0) {
				list.add("(x=" + n.cutVal + ")");
			} else {
				list.add("(y=" + n.cutVal + ")");
			}
			n.right.list(list, n.right);
			n.left.list(list, n.left);
		}

		Node delete(Point2D pt, Node root) throws Exception {
			InternalNode n = (InternalNode) root;
			if (pt.get(n.cutDim) == n.cutVal) {
				try {
					return keepOrReplace(n.left.delete(pt, n.left), n, true);
				} catch (Exception e) {
					return keepOrReplace(n.right.delete(pt, n.right), n, false);
				}
			} else {
				if (pt.get(n.cutDim) < n.cutVal) {
					return keepOrReplace(n.left.delete(pt, n.left), n, true);
				} else {
					return keepOrReplace(n.right.delete(pt, n.right), n, false);
				}
			}
		}

		void kNNHelper(Point2D q, Node root, Rectangle2D cell, @SuppressWarnings("rawtypes") MinK minK) {
			InternalNode n = (InternalNode) root;
			if (cell.distanceSq(q) > (double) minK.getKth()) {
				return;
			}
			int cd = n.cutDim;
			double cutVal = n.cutVal;
			Rectangle2D leftCell = cell.leftPart(cd, cutVal);
			Rectangle2D rightCell = cell.rightPart(cd, cutVal);
			if (q.get(cd) >= cutVal) {
				n.right.kNNHelper(q, n.right, rightCell, minK);
				n.left.kNNHelper(q, n.left, leftCell, minK);
			} else {
				n.left.kNNHelper(q, n.left, leftCell, minK);
				n.right.kNNHelper(q, n.right, rightCell, minK);
			}
		}
	}

	/*
	 * External node class that contains the data points in an array list. Cannot
	 * exceed bucket size
	 */
	private class ExternalNode extends Node {
		private ArrayList<LPoint> pts;
		private int bucketSize;

		/*
		 * External node constructor initializing an empty array list and specifying a
		 * bucket size
		 */
		private ExternalNode(int bucketSize) {
			this.bucketSize = bucketSize;
			pts = new ArrayList<LPoint>();
		}

		/*
		 * Helper find function for external node. Traverses the list of points to
		 * determine if the point to be found exists or not.
		 */
		LPoint find(Point2D q, Node root) {
			ExternalNode n = (ExternalNode) root;
			ArrayList<LPoint> lst = new ArrayList<>(n.pts);
			for (LPoint pt : lst) {
				if (pt.getPoint2D().equals(q)) {
					return pt;
				}
			}
			return null;
		}

		/*
		 * Helper function for bulkInsert that splits the pts to insert in the external
		 * node if upon adding them, it exceeds the bucket size.
		 */
		Node bulkInsert(ArrayList<LPoint> pts, Node root) {
			ExternalNode n = (ExternalNode) root;
			n.pts.addAll(pts);
			if (n.currSize() <= bucketSize) {
				return n;
			} else {
				Rectangle2D rect = new Rectangle2D();
				for (LPoint pt : n.pts) {
					rect.expand(pt.getPoint2D());
				}
				Point2D low = rect.getLow();
				Point2D high = rect.getHigh();
				double width = high.getX() - low.getX();
				double height = high.getY() - low.getY();
				double median = 0;
				int cutDim = -1;
				int m = (int) Math.floor(n.pts.size() / 2);
				int num = n.pts.size();
				if (width >= height) {
					cutDim = 0;
					Collections.sort(n.pts, new sortByXThenY());
					if (num % 2 != 0) {
						median = n.pts.get(m).getX();
					} else {
						median = (n.pts.get(m - 1).getX() + n.pts.get(m).getX()) / 2;
					}
				} else {
					cutDim = 1;
					Collections.sort(n.pts, new sortByYThenX());
					if (num % 2 != 0) {
						median = n.pts.get(m).getY();
					} else {
						median = (n.pts.get(m - 1).getY() + n.pts.get(m).getY()) / 2;
					}
				}
				InternalNode node = new InternalNode(cutDim, median);
				ArrayList<LPoint> left = new ArrayList<>(n.pts.subList(0, m));
				ArrayList<LPoint> right = new ArrayList<>(n.pts.subList(m, num));
				node.left = new ExternalNode(bucketSize);
				node.right = new ExternalNode(bucketSize);
				ExternalNode n1 = (ExternalNode) node.left;
				ExternalNode n2 = (ExternalNode) node.right;
				if (left.size() <= bucketSize) {
					n1.pts.addAll(left);
					node.left = n1;
				} else {
					node.left = bulkInsert(left, n1);
				}
				if (right.size() <= bucketSize) {
					n2.pts.addAll(right);
					node.right = n2;
				} else {
					node.right = bulkInsert(right, n2);
				}
				return node;
			}
		}

		/*
		 * Helper function that will be used to determine if the number of points within
		 * this node exceed the bucket size.
		 */
		private int currSize() {
			return pts.size();
		}

		/*
		 * Helper function to find closest neighbor. Traverses the points in the
		 * external node and determines if any of them beat the current best.
		 */
		Point2D nearestNeighbor(Point2D q, Node root, Rectangle2D cell, Point2D best) {
			ExternalNode n = (ExternalNode) root;
			double min = Double.MAX_VALUE;
			Point2D result = null;
			for (LPoint pt : n.pts) {
				double dist = pt.getPoint2D().distanceSq(q);
				if (dist < min) {
					min = dist;
					result = pt.getPoint2D();
				}
			}
			if (min < q.distanceSq(best)) {
				return result;
			}
			return best;
		}

		/*
		 * External node helper function for list.
		 */
		void list(ArrayList<String> list, Node root) {
			String s = "[ ";
			ExternalNode n = (ExternalNode) root;
			ArrayList<LPoint> l = new ArrayList<>(n.pts);
			Collections.sort(l, new sortByLabel());
			for (LPoint pt : l) {
				s += "{" + pt.getLabel() + ": " + pt.getPoint2D() + "} ";
			}
			s += "]";
			list.add(s);
		}

		Node delete(Point2D pt, Node root) throws Exception {
			ExternalNode n = (ExternalNode) root;
			boolean found = false;
			for (LPoint p : n.pts) {
				if (p.getPoint2D().equals(pt)) {
					n.pts.remove(p);
					found = true;
					break;
				}
			}
			if (found) {
				if (n.pts.isEmpty()) {
					return null;
				}
				return n;
			}
			throw new Exception("Deletion of nonexistent point");
		}

		@SuppressWarnings("unchecked")
		void kNNHelper(Point2D q, Node root, Rectangle2D cell, @SuppressWarnings("rawtypes") MinK minK) {
			ExternalNode n = (ExternalNode) root;
			for (LPoint pt : n.pts) {
				minK.add(pt.getPoint2D().distanceSq(q), pt);
			}
		}
	}

	private Node root;
	private int bucketSize;
	private Rectangle2D bbox;
	private int numPoints;

	/*
	 * Basic constructor for the kd-tree setting the bucket size, bounding box, and
	 * initializing the root to be an external node with 0 points.
	 */
	public XkdTree(int bucketSize, Rectangle2D bbox) {
		root = new ExternalNode(bucketSize);
		this.bucketSize = bucketSize;
		this.bbox = bbox;
		numPoints = 0;
	}

	/*
	 * Clear the kd-tree structure.
	 */
	public void clear() {
		root = new ExternalNode(bucketSize);
		numPoints = 0;
	}

	/*
	 * return the number of points in the kd-tree.
	 */
	public int size() {
		return numPoints;
	}

	/*
	 * Find the 2d point q in the kd-tree and return it's labeled point.
	 */
	public LPoint find(Point2D q) {
		return root.find(q, root);
	}

	/*
	 * Insert single labeled point into the kd-tree.
	 */
	public void insert(LPoint pt) throws Exception {
		ArrayList<LPoint> pts = new ArrayList<LPoint>();
		pts.add(pt);
		bulkInsert(pts);
	}

	/*
	 * Given a list of points, inserts one or more points into the kd-tree.
	 */
	public void bulkInsert(ArrayList<LPoint> pts) throws Exception {
		Point2D low = bbox.getLow();
		Point2D high = bbox.getHigh();
		for (LPoint pt : pts) {
			Point2D p = pt.getPoint2D();
			if (p.getX() < low.getX() || p.getX() > high.getX() || p.getY() < low.getY() || p.getY() > high.getY()) {
				throw new Exception("Attempt to insert a point outside bounding box");
			}
		}
		root = root.bulkInsert(pts, root);
		numPoints += pts.size();
	}

	/*
	 * pre-order right-left traversal of the kd-tree structure, stored as a list or
	 * strings
	 */
	public ArrayList<String> list() {
		ArrayList<String> result = new ArrayList<>();
		root.list(result, root);
		return new ArrayList<>(result);
	}

	/*
	 * Computes the closest point to the given query point "center"
	 */
	public LPoint nearestNeighbor(Point2D center) {
		Rectangle2D rect = new Rectangle2D(bbox);
		Point2D best = new Point2D(Double.MAX_VALUE, Double.MAX_VALUE);
		Point2D result = root.nearestNeighbor(center, root, rect, best);
		return root.find(result, root);
	}

	/*
	 * Deletes a point from the kd-tree.
	 */
	public void delete(Point2D pt) throws Exception {
		if (root.find(pt, root) == null) {
			throw new Exception("Deletion of nonexistent point");
		}
		root = root.delete(pt, root);
		if (root == null) {
			root = new ExternalNode(bucketSize);
		}
		numPoints--;
	}

	/*
	 * Finds the k nearest neighbors centered around a point
	 */
	public ArrayList<LPoint> kNearestNeighbor(Point2D center, int k) {
		Rectangle2D rect = new Rectangle2D(bbox);
		MinK<Double, LPoint> minK = new MinK<Double, LPoint>(k, Double.MAX_VALUE);
		root.kNNHelper(center, root, rect, minK);
		return new ArrayList<LPoint>(minK.list());
	}

	/*
	 * Comparator class that will be used to assist in partitioning a set of
	 * coordinates when x is the cut dimension. It will sort the points based on
	 * their x values. If x values are equal, then sort based on y values
	 */
	private class sortByXThenY implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			int comp = (int) Math.ceil(pt1.getX() - pt2.getX());
			if (comp == 0) {
				return (int) Math.ceil(pt1.getY() - pt2.getY());
			} else {
				return (int) comp;
			}
		}
	}

	/*
	 * Comparator class that will be used to assist in partitioning a set of
	 * coordinates when y is the cut dimension. It will sort the points based on
	 * their y values. If y values are equal, then sort based on x values
	 */
	private class sortByYThenX implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			int comp = (int) Math.ceil(pt1.getY() - pt2.getY());
			if (comp == 0) {
				return (int) Math.ceil(pt1.getX() - pt2.getX());
			} else {
				return (int) comp;
			}
		}
	}

	/*
	 * Comparator class that will sort a set of points in ascending order based on
	 * their label. Used as a helper class for the list function
	 */
	private class sortByLabel implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			return pt1.getLabel().compareTo(pt2.getLabel());
		}
	}

	/*
	 * Helper function for the internal node delete function. Used to determine if
	 * the data from the external node resulted in an empty list. If it has, return
	 * the right child, otherwise, just set the the left child to the updated left
	 * subtree
	 */
	private Node keepOrReplace(Node temp, InternalNode n, boolean left) {
		if (left) {
			if (temp == null) {
				return n.right;
			}
			n.left = temp;
			return n;
		}
		if (temp == null) {
			return n.left;
		}
		n.right = temp;
		return n;
	}
}