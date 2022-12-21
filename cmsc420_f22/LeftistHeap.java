package cmsc420_f22;

import java.util.ArrayList;

/*
 * Author: Adrian Paras
 * For: CMSC420
 * Date: Fall 2022
 * 
 * Description: This is a leftist heap structure that contains the normal operations a
 * standard min heap would have, except that it maintains a leftist property, in which
 * every node has a null-path length(npl) value, and the left child's npl value must always
 * be greater than or equal to that of that right child.
 * 
 */

public class LeftistHeap<Key extends Comparable<Key>, Value> {

	/*
	 * Node class that holds the key, value, npl value, and left/right child nodes
	 */
	private class Node {
		private Node left, right;
		private Key key;
		private Value value;
		private int npl;

		private Node(Key key, Value value) {
			this.left = null;
			this.right = null;
			this.key = key;
			this.value = value;
			this.npl = 0;
		}
	}

	private Node root;

	/*
	 * Simple constructor setting root to null
	 */
	public LeftistHeap() {
		root = null;
	}

	/*
	 * Checks if heap is empty
	 */
	public boolean isEmpty() {
		return root == null;
	}

	/*
	 * Resets all contents of the heap
	 */
	public void clear() {
		root = null;
	}

	/*
	 * Inserts the key-value pair into the heap using the merge function
	 */
	public void insert(Key x, Value v) {
		root = merge(root, new Node(x, v));
	}

	/*
	 * Merges another leftist heap h2 with the current one
	 */
	public void mergeWith(LeftistHeap<Key, Value> h2) {
		if (h2 == null || this == h2) {
			return;
		}
		root = merge(this.root, h2.root);
		h2.root = null;
	}

	/*
	 * Splits the heap at the given key x
	 */
	public LeftistHeap<Key, Value> split(Key x) {
		LeftistHeap<Key, Value> h2 = new LeftistHeap<>();
		ArrayList<Node> list = new ArrayList<>();
		root = pre_order_left_to_right(root, list, x);
		for (Node n : list) {
			LeftistHeap<Key, Value> temp = new LeftistHeap<>();
			temp.root = n;
			h2.mergeWith(temp);
		}
		fixNpl(root);
		fixTree(root);
		return h2;
	}

	/*
	 * Returns the smallest key in the heap, which is always the root
	 */
	public Key getMinKey() {
		if (root == null) {
			return null;
		}
		return root.key;
	}

	/*
	 * Returns and removes the smallest key in the heap
	 */
	public Value extractMin() throws Exception {
		if (root == null) {
			throw new Exception("Empty heap");
		}
		Value temp = root.value;
		root = merge(root.left, root.right);
		return temp;
	}

	/*
	 * Returns a list of all the data in the heap in a pre-order traversal
	 */
	public ArrayList<String> list() {
		ArrayList<String> result = new ArrayList<>();
		pre_order(root, result);
		return result;
	}

	/*
	 * Performs pre_order traversal on the LeftistHeap and adds to ArrayList
	 */
	private void pre_order(Node root, ArrayList<String> list) {
		if (root == null) {
			list.add("[]");
			return;
		}
		list.add("(" + root.key + ", " + root.value + ")" + " [" + root.npl + "]");
		pre_order(root.right, list);
		pre_order(root.left, list);
	}

	// Used for the split() function, which unlinks nodes that have a greater key
	// than x, and adds to an ArrayList
	private Node pre_order_left_to_right(Node root, ArrayList<Node> list, Key x) {
		if (root == null) {
			return null;
		}
		if (root.key.compareTo(x) <= 0) {
			root.left = pre_order_left_to_right(root.left, list, x);
			root.right = pre_order_left_to_right(root.right, list, x);
		} else if (root.key.compareTo(x) > 0) {
			list.add(root);
			return null;
		}
		return root;
	}

	// Does all the work to merge two leftist heaps given their roots
	private Node merge(Node n1, Node n2) {
		if (n1 == null) {
			return n2;
		}
		if (n2 == null) {
			return n1;
		}
		if (n1.key.compareTo(n2.key) > 0) {
			Node temp = n1;
			n1 = n2;
			n2 = temp;
		}
		if (n1.left == null) {
			n1.left = n2;
		} else {
			n1.right = merge(n1.right, n2);
			if (n1.left.npl < n1.right.npl) {
				Node temp = n1.left;
				n1.left = n1.right;
				n1.right = temp;
			}
			n1.npl = n1.right.npl + 1;
		}
		return n1;
	}

	// Used to re-computes the npl values for each node after split() is called
	private int fixNpl(Node root) {
		if (root == null) {
			return -1;
		}
		root.npl = Math.min(fixNpl(root.left), fixNpl(root.right)) + 1;
		return root.npl;
	}

	// Used to maintain the leftist heap property after split() is called by
	// swapping nodes,
	// if it's the case that the right npl value is greater than the left
	private void fixTree(Node root) {
		if (root == null) {
			return;
		}
		if (root.left != null && root.right != null) {
			if (root.right.npl > root.left.npl) {
				Node temp = root.left;
				root.left = root.right;
				root.right = temp;
			}
		} else if (root.left == null && root.right != null) {
			Node temp = root.left;
			root.left = root.right;
			root.right = temp;
		}
		fixTree(root.left);
		fixTree(root.right);
	}
}
