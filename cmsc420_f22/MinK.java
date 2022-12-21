package cmsc420_f22;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/*
 * Author: Adrian Paras
 * For: CMSC420
 * Date: Fall 2022
 * 
 * Description: This class is used to assist in finding the k nearest neighbors
 * by constructing a max heap of the distances computed.
 * 
 */
public class MinK<Key extends Comparable<Key>, Value> {

	private Key maxKey;
	private int k, size;
	private HashMap<Key, Value>[] keysAndVals;

	/*
	 * Constructor that initializes the heap, k value, maxKey, and sets size to 0
	 */
	@SuppressWarnings("unchecked")
	public MinK(int k, Key maxKey) {
		this.k = k;
		this.maxKey = maxKey;
		size = 0;
		keysAndVals = new HashMap[k + 1];
	}

	/*
	 * returns the size
	 */
	public int size() {
		return size;
	}

	/*
	 * resets MinK to initial state
	 */
	@SuppressWarnings("unchecked")
	public void clear() {
		keysAndVals = new HashMap[k + 1];
		size = 0;
	}

	/*
	 * returns maxKey when size is less than k. Otherwise, return the maximum key
	 * among all elements
	 */
	public Key getKth() {
		if (size == k) {
			return keysAndVals[1].keySet().iterator().next();
		}
		return maxKey;
	}

	/*
	 * adds a key-value pair to the set
	 */
	public void add(Key x, Value v) {
		if (size < k) {
			HashMap<Key, Value> m = new HashMap<Key, Value>();
			m.put(x, v);
			keysAndVals[++size] = m;
			keysAndVals[sift_up(size, x)] = m;
		} else {
			int compare = x.compareTo(keysAndVals[1].keySet().iterator().next());
			if (compare < 0) {
				HashMap<Key, Value> m = new HashMap<Key, Value>();
				m.put(x, v);
				keysAndVals[1] = m;
				keysAndVals[sift_down(1, x)] = m;
			}
		}
	}

	/*
	 * returns a list of the values in the structure, sorted in increasing order by
	 * their key values
	 */
	public ArrayList<Value> list() {
		ArrayList<Value> result = new ArrayList<>();
		TreeMap<Key, Value> treeMap = new TreeMap<Key, Value>();
		for (int i = 1; i <= size; i++) {
			Key key = keysAndVals[i].keySet().iterator().next();
			Value val = keysAndVals[i].values().iterator().next();
			treeMap.put(key, val);
		}
		result.addAll(treeMap.values());
		return new ArrayList<Value>(result);
	}

	/*
	 * Helper function that will be used to sift the key x up to its correct
	 * position. Used whenever the current size is less than k
	 */
	private int sift_up(int i, Key x) {
		while (i > 1 && x.compareTo(keysAndVals[parent(i)].keySet().iterator().next()) > 0) {
			keysAndVals[i] = keysAndVals[parent(i)];
			i = parent(i);
		}
		return i;
	}

	/*
	 * Helper function that will be used to sift the key x down to its correct
	 * position. Used whenever the root is replaced upon adding an element
	 */
	private int sift_down(int i, Key x) {
		while (left(i) != -1) {
			int u = left(i);
			int v = right(i);
			if (v != -1) {
				int compare = keysAndVals[v].keySet().iterator().next()
						.compareTo(keysAndVals[u].keySet().iterator().next());
				if (compare > 0) {
					u = v;
				}
			}
			if (keysAndVals[u].keySet().iterator().next().compareTo(x) > 0) {
				keysAndVals[i] = keysAndVals[u];
				i = u;
			} else {
				break;
			}
		}
		return i;
	}

	// Computes parent of node at index i
	private int parent(int i) {
		if (i >= 2) {
			return (int) Math.floor(i / 2);
		} else {
			return -1;
		}
	}

	// Computes left child of node at index i
	private int left(int i) {
		if (2 * i <= size) {
			return 2 * i;
		} else {
			return -1;
		}
	}

	// Computes right child of node at index i
	private int right(int i) {
		if (2 * i + 1 <= size) {
			return 2 * i + 1;
		} else {
			return -1;
		}
	}
}
