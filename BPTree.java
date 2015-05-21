
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.lang.IllegalArgumentException;
import java.util.NoSuchElementException;
import java.io.PrintStream;
import xml.XMLElement;


/**
 * This is a rough template for your B+ tree class. You may modify it
 * as you see fit. Just be sure that you implement all of the required
 * functions.
 * 
 * The B+ tree does not support null key or null value
 * The key Object must support equal, if it is necessary, override it
 * 
 * @author Daozheng Chen
 * 
 */
public class BPTree<K, V> extends AbstractMap<K, V> implements SortedMap<K, V>
{
	/**  
	 * how many links per guide node
	 */
	protected int order;
	
	/** 
	 * the maximum number of keys in a leaf node
	 */
	protected int leafOrder;
	
	/**
	 * minimum number of children for an internal node
	 */
	protected int minChild; 
	
	/**
	 * minimum number of leaves for a leaf
	 */
	protected int minLeaf; 
	
	/**
	 * the number of recoder in the B+ tree
	 */
	protected int size; 
	
	/**
	 * the root of the B+ tree
	 */
	protected BPNode root;
	
	/**
	 * the comparator used to sorted a B+ tree
	 */
	protected Comparator<? super K> comp; 
	
	/**
	 * 
	 */
	protected final GenericComparator<K> genericComparatorIntance = new GenericComparator<K>(); 
	
	
	/**
	 * 
	 */
	protected int modCount = 0;
	
	/**
	 * The class name and field variable is copied from the 
	 * Source code example for "A Practical Introduction
	 * to Data Structures and Algorithm Analysis"
	 * by Clifford A. Shaffer, Prentice Hall, 1998.
	 * Copyright 1998 by Clifford A. Shaffer
	 * Please see the following web
	 * http://people.cs.vt.edu/~shaffer/Book/JAVA/progs/BPtree/Pair.java
	 * Each BPNode stores an array of Pair
	 * 
	 * @author Daozheng Chen
	 */
	class Pair<KEY, VALUE> implements Map.Entry<KEY, VALUE>{
		/**
		 * the value field of the Pair.
		 * BPNode leaves point to Elem, BPNode internal nodes point to BPNode
		 */
		VALUE pointer;
		
		/**
		 * The key value for this pair
		 */
		KEY key;

		/**
		 * initialize both pointer and key to be null
		 *
		 */
		public Pair(){
			pointer = null;
			key = null;
		}

		/**
		 * this constructor let the key and pointer inside the class
		 * pointing address of the key and pointer passed in. (Not a copy of them)
		 * @param key the key of the pair
		 * @param pointer the value of the pair
		 */
		public Pair(KEY key, VALUE pointer){
			this.key = key;
			this.pointer = pointer;
		}

		/**
		 * @return the actual reference of the key data field
		 */
		public KEY getKey() {
			return key;
		}

		/**
		 * @return the actual reference of the value contained in the pair.
		 * Currently, the data field is visible out of class but within package 
		 */
		public VALUE getValue() {
			return pointer;
		}

		/**
		 * 
		 * @return
		 */
		public org.w3c.dom.Node toXML(){
			XMLElement ele = new XMLElement(CommandAttributes.ENTRY, null);
			ele.setAttribute(CommandAttributes.KEY, this.key.toString());
			ele.setAttribute(CommandAttributes.VALUE, this.pointer.toString());
			return ele;
		}
		
		/**
		 * this method is not supported yet
		 * @param arg0
		 * @return
		 */
		public VALUE setValue(VALUE arg0) {
			throw new UnsupportedOperationException("in BPTree - Pair - setValue - not supported");
		}
		
		@SuppressWarnings("unchecked")
		public boolean equals(Object o){
			if (!(o instanceof Entry))
				return false;
			
			Entry<KEY, VALUE> entry = (Entry<KEY, VALUE>)o;
			return (key.equals(entry.getKey()) && pointer.equals(entry.getValue()));
		}
		

		/**
		 * This method is copied from TreeMap.java in the Java API
		 */
        public int hashCode() {
            int keyHash = (key==null ? 0 : key.hashCode());
            int valueHash = (pointer==null ? 0 : pointer.hashCode());
            return keyHash ^ valueHash;
        }
		
	}
	
	
	/**
	 * the design of the data fields and methods of this class is from
	 * Source code example for "A Practical Introduction
	 * to Data Structures and Algorithm Analysis"
	 * by Clifford A. Shaffer, Prentice Hall, 1998.
	 * Copyright 1998 by Clifford A. Shaffer
	 * Please see the following web
	 * http://people.cs.vt.edu/~shaffer/Book/JAVA/progs/BPtree/BPNode.java
	 * 
	 * @author Daozheng Chen
	 */
	protected class BPNode {
		
		/**
		 * True if this is a leaf
		 */
		boolean isLeaf;
		
		/**
		 * Array of key/pointer pairs 
		 */
		Pair<K, Object>[] recarray;
		
		/**
		 * Number of pairs currently in node
		 * notice for internal node, it is number of child of the node
		 * for leaf, it is the number of records in the node
		 */
		int numrec;
		
		/**
		 * Each level forms a doubly-linked list
		 */
		BPNode leftptr, rightptr;
		
		
		/**
		 * constructor of BPNode make the recarray to be size maxNumRec, and the node
		 * to be appropriate leaf node or internal node
		 * @param maxNumRec - the size of the record array in this node
		 * @param isLeaf - true if it is a leave node. Otherwise, internal node
		 */
		@SuppressWarnings("unchecked") BPNode(int maxNumRec, boolean isLeaf){
			this.isLeaf = isLeaf;
			this.recarray = (Pair<K, Object>[])new Pair[maxNumRec];
			numrec = 0;
			leftptr = rightptr = null;
		}
			
		/**
		 * attention: currently this method just do linear search, but we want binary search
		 * @param key - the key to be searched for
		 * @return if curRoot is a leaf, return the position to be insert
		 * if curRoot is an internal node, return the index of the sub child to be followed
		 */
		int binaryle(K key){
			
			int result = 0;			
			if (isLeaf){ // is a leaf
				int i = 0;
				for (; i < numrec; i++){
					if (comp.compare(recarray[i].key, key) >= 0)
						return i;
				}		
				return i; // insert at the end of the array
			}
			else{ // is internal node return the index to sub child to be followed
				int i = 1;
				for(; i < numrec; i++){
					result = comp.compare(recarray[i].key, key);
					if (result > 0)
						return i-1;
					else if (result == 0)
						return i;
				}		
				return i-1; // following the child with the largest key
			}
		}
		
		/**
		 * the precondition of this method is that the recarray is not full, 
		 * so we can insert the new pair into the node
		 * @param index - the index of newPair to be inserted to
		 * @param newPair - the new pair to be inserted
		 */
		void putInArray(int index, Pair<K, Object> newPair){
			for (int i = numrec; i > index; i--)		
				recarray[i] = recarray[i-1]; // it will not cause array index
				// out of bound, because the recarray is not full
			// insert the new pair
			recarray[index] = newPair;
			// update the size
			numrec++;
		}
		
		
		/**
		 * the precondition is that the recarray is full, so we need split the node
		 * @param index - the index the newPair to be inserted to
		 * @param newPair - the new pair to be inserted
		 * @return the new created node
		 */
		BPNode splitNode(int index, Pair<K, Object> newPair){
			
			BPNode temp = null; // new node to be returned
			
			if (isLeaf){ // the node is a leaf
				temp = new BPNode(leafOrder, true); // create a leaf node
				splitNodeAux(index, newPair, leafOrder, minLeaf, temp);
			}
			else{ // the node is an internal node
				temp = new BPNode(order, false); // create an internal node
				splitNodeAux(index, newPair, order, minChild, temp);
			}		
			// update the linked list
			temp.rightptr = rightptr;
			temp.leftptr = (BPNode)this;
			if (temp.rightptr != null)
				temp.rightptr.leftptr = temp;
			rightptr = temp;		
			
			// return the new node
			return temp;
		}
		
		/**
		 * helper function for splitNode
		 * @param node
		 * @param index
		 * @param newPair
		 * @param nodeOrder
		 * @param minNodeOrder
		 * @param temp
		 * @return
		 */
		protected void splitNodeAux(int index, Pair<K, Object> newPair, int nodeOrder, int minNodeOrder, BPNode temp){		
			
			int j = 0;
			int i = 0;
			
			if (index <= minNodeOrder -1){ // newPair inserted into the old node
				// add records into the new node
				for (j = 0, i = minNodeOrder -1; i < numrec; j++, i++)
					temp.recarray[j] = recarray[i];	
				// update numrec of the new node
				temp.numrec = j;
				// update the old node
				for (i = minNodeOrder - 1; i > index; i--)
					recarray[i] = recarray[i-1];
				recarray[index] = newPair;
				// update numrec of the old node
				numrec = minNodeOrder;
			}
			else{ // new pair inserted into the new node

				// update records in the new node
				for (j = 0, i = minNodeOrder; i < numrec; j++, i++){
					if (i == index){
						temp.recarray[j] = newPair; // insert the new pair in
						temp.recarray[++j] = recarray[i];
					}
					else
						temp.recarray[j] = recarray[i];
				}								
				if (j == (numrec - minNodeOrder)){ // in case of the new node should be inserted at the end of array 
					temp.recarray[j] = newPair;
					// update numrec in the new node
					temp.numrec = j+1;
				}
				else // the newPair has been added into the array, update the size
					temp.numrec = j;
				
				// update records in the old node
				for (i = minNodeOrder; i < numrec; i++)
					recarray[i] = null;
				
				// update the numrec in the old node
				numrec = minNodeOrder;	
			}
		}
		
		
	} // class BPNode
	
	/**
	 * 
	 * @author Daozheng Chen
	 *
	 * @param <T>
	 */
	class GenericComparator<T> implements Comparator<T>{
		@SuppressWarnings("unchecked")
		public int compare(T o1, T o2) {
			return ((java.lang.Comparable)o1).compareTo(o2);
		}
	}
	
	/**************************************************************
	* You are responsible for implementing the following functions.
	* You may add any other private/protected/public functions that you want.
	* You are allowed to change the parameter variable names.
	***************************************************************/
	
	/**
	 * Defaults contructor set B+ tree order = 3, leafOrder = 3, 
	 * assumes added elements implement Comparable
	 */
	BPTree(){
		contorAux(this.genericComparatorIntance, 3, 3);		
	}

	/**
	 * contructor set leafOrder = order - 1
	 * the order must >= 3. Otherwise it will throw IllegalArgumentException
	 * @param order - the order of the B+ tree
	 * @throws IllegalArgumentException
	 */
	BPTree(int order){
		int leafOrder = order -1;
		
		if (order < 3)
			throw new IllegalArgumentException("In BPTree(int order) - order value < 3");
			
		// if (leafOrder < 1)
		//	throw new IllegalArgumentException("In BPTree(int order) - leafOrder value <= 0");
		
		contorAux(this.genericComparatorIntance, order, leafOrder); // attention here, I don't know how to fix this warning
	}
	
	/**
	 * Defaults to order 3, leafOrder 3, uses a Comparator and NEVER tries to cast 
	 * an added object to a Comparable.
	 * @param c - The Comparator to use to sort objects.
	 */
	BPTree(Comparator<? super K> c) {
		contorAux(c, 3, 3);
	}
	
	/**
	 * Initalizes the tree to the specified	order and assumes added elements 
	 * implement the Comparable interface. The order must be >= 3 and leafOrder
	 * must be > 0. Otherwise it will throw IllegalArgumentException
	 * @param order - The order to initialize the B+ tree with
	 * @param leafOrder - The leaf order to initialize the B+ tree with
	 * @throws IllegalArgumentException thrown if order is not >= 3 or if leafOrder is not >= 1
	 */
	BPTree(int order, int leafOrder) throws IllegalArgumentException{
		
		if (order < 3)
			throw new IllegalArgumentException("In BPTree(int order, int leafOrder) - order value < 3");
			
		if (leafOrder < 1)
			throw new IllegalArgumentException("In BPTree(int order, int leafOrder) - leafOrder value <= 0");
		
		contorAux(this.genericComparatorIntance, order, leafOrder); // attention here, I don't know how to fix this warning
				// if you are going to change it the method public Comparator<K> comparator(){ may also going to be changed.
	}

	
	/**
	 * Initalizes the tree to the specified	order and uses a Comparator 
	 * and NEVER tries to cast an added object to a Comparable.
	 * @param order - The order to initalize the B+ tree with
	 * @param leafOrder - The leaf order to initialize the B+ tree with
	 * @throws IllegalArgumentException thrown if order is not >= 3 or if leafOrder is not >= 1
	 */
	BPTree(Comparator<? super K> c, int order, int leafOrder) throws IllegalArgumentException{
		
		if (order < 3)
			throw new IllegalArgumentException("In BPTree(Comparator c, int order, int leafOrder) - order value < 3");
			
		if (leafOrder < 1)
			throw new IllegalArgumentException("In BPTree(Comparator c, int order, int leafOrder) - leafOrder value <= 0");
		
		contorAux(c, order, leafOrder);
	}

	/**
	 * helper function helping all the contructor above
	 * all the parameters in this method have valid value
	 * @param c
	 * @param order
	 * @param leafOrder
	 */
	protected void contorAux(Comparator<? super K> c, int order, int leafOrder){
		this.order = order;
		this.leafOrder = leafOrder;
		minChild = (int)Math.ceil((double)order/2);
		minLeaf = (int)Math.ceil((double)leafOrder/2);
		size = 0;
		root = null;
		comp = c;
	}

	
	/**
	 * the following two methods are copied from TreeMap class source code aviable on GRACE
	 *
	 */
    protected void incrementSize()   { modCount++; size++; }
    protected void decrementSize()   { modCount++; size--; }
	
	/**
	 * locate the head node (not pair in record array) of the leaves(linked List)
	 * @return the head of the leaves or null if root is null
	 */
	protected BPNode locateHead(){
		if (root == null)
			return null;
		
		BPNode tempRoot = root;
		while (!tempRoot.isLeaf)
			tempRoot = (BPNode)tempRoot.recarray[0].pointer;
		return tempRoot;
	}

	/**
	 * 
	 * @return the height of the tree. If there is no element in the tree, return 0.
	 */
	protected int height(){
		if (root == null)
			return 0;
		
		int height = 1;
		BPNode tempRoot = root;
		while (!tempRoot.isLeaf){
			height++;
			tempRoot = (BPNode)tempRoot.recarray[0].pointer;
		}
		return height;
	}
	
	/**
	 * locate the tail node (not pair in record array) of the leaves(linked List)
	 * @return the tail of the leaves or null if the root is null
	 */
	protected BPNode locateTail(){
		if (root == null)
			return null;
		
		BPNode tempRoot = root;
		while(!tempRoot.isLeaf)
			tempRoot = (BPNode)tempRoot.recarray[tempRoot.numrec-1].pointer;
		return tempRoot;
	}
	
	/**
	 * You are responsible for implementing the following SortedMap
	 * functions. See http://java.sun.com/j2se/1.5.0/docs/api/java/util/SortedMap.html
	 * for the specification of each function.
	 */
	
	/**
	 * @return the first key in the lowest order using the current comparator
	 */
	public K firstKey(){		
		if (size == 0)
			throw new NoSuchElementException("in BPTree - firstKey() - no element in the tree yet");
			
		BPNode tempRoot = root;
		while (!tempRoot.isLeaf)
			tempRoot = (BPNode)tempRoot.recarray[0].pointer;
		
		return tempRoot.recarray[0].key;
	}
	
	/**
	 * @return the first key in the lowest order using the current comparator
	 */
	public K lastKey(){
		if (size == 0)
			throw new NoSuchElementException("in BPTree - lastKey() - no element in the tree yet");
			
		BPNode tempRoot = root;
		while (!tempRoot.isLeaf)
			tempRoot = (BPNode)tempRoot.recarray[tempRoot.numrec-1].pointer;
		
		return tempRoot.recarray[tempRoot.numrec-1].key;		
	}
	
	
	/**
	 * the body of the code is copy from SkipList.java in the util package in the
	 * provided xml.jar file. author: Evan Machusak
	 * @return null if it is using the default comparator. Otherwise, return the acutal
	 * comparator
	 */
	public Comparator<? super K> comparator(){
		return (comp == this.genericComparatorIntance ? null : comp);
	}	
	
	/**
	 * @return the number of records in the tree
	 */
	public int size(){
		return size;
	}
	
	/**
	 * make the B+ tree to be an empty tree. 
	 * But the data for order, leafOrder and comparator preserved
	 */
	public void clear(){
		root = null;
		size = 0;
	}
	
	/**
	 * @return true if it empty. Otherwise return false
	 */
	public boolean isEmpty(){
		return (size == 0 ? true : false);
	}
	
	
	
	/**
	 * the method is modified from SkipList.java in xml.jar
	 */
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		
		if (this == o)
			return true;
				
		if (!(o instanceof Map)){
			return false;		
		}
		else {
			if (this.size != ((Map<K, V>)o).size())
				return false;
			
			Iterator<Entry<K, V>> i = ((Map<K, V>)o).entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry<K, V> me = i.next();
				V n = BPTree.this.locate(me.getKey());
				if (n == null || !n.equals(me.getValue()))
					return false;
			}
			return true;
		}
	}
	

	/**
	 * the tree does not support null key and value
	 * @return true if the tree contains the key. Otherwise false
	 */
	public boolean containsKey(Object key){
		if (key == null)
			throw new NullPointerException("in BPTree - containsKey(Object key) - key is null");
		
		return (locate(key) == null ? false : true); // this statement may throw runtime class cast exception
	}
	
	/**
	 * the precondition is assuming that the key is a valid class type and 
	 * it is not null
	 * @param key - the key of the value to be located
	 * @return the value corresponding to this key. if found. Otherwise return null
	 * @throws classCastException if the key cannot be compared with the keys currently in the map.
	 */
	@SuppressWarnings("unchecked")
	protected V locate(Object key_in){
	
		if (size == 0)
			return null;
		
		// if may throw classCastException here
		K key = (K)key_in;
		
		BPNode curRoot = root;
		int currec = 0;
		while (!curRoot.isLeaf){
			currec = curRoot.binaryle(key);
			curRoot = (BPNode)(curRoot.recarray[currec].pointer);
		} // exit when curRoot is a leaf

		currec = curRoot.binaryle(key); 
		
		if (currec == curRoot.numrec) // the insert position is at the end of the array,
									  // means that no element in the array is equal to key
			return null;
		
		if ((curRoot.recarray[currec].key).equals(key))
			return (V)(curRoot.recarray[currec].pointer); // the target is found
		else
			return  null; // not target is found
	}

	/**
	 * traverse the linkedList of leave to find the specific value.
	 * @return true if found. False otherwise
	 */	
	@SuppressWarnings("unchecked")
	public boolean containsValue(Object value_in){
		
		if (size == 0)
			return false;
			
		if (value_in == null)
			throw new NullPointerException("in BPTree - containsValue(Object value) - value == null");
		

		// System.out.println("B");

		// System.out.println(value_in.toString());
		
		// it may throw classCastException here
		V value = (V)value_in;
		
		// System.out.println(value.toString());
		
		// System.out.println("A");
		
		
		BPNode tempRoot = root;
		while (!tempRoot.isLeaf)
			tempRoot = (BPNode)tempRoot.recarray[0].pointer;
		
		do{
			for (int i = 0; i < tempRoot.numrec; i++)
				if (tempRoot.recarray[i].pointer.equals(value))
					return true;
			// update
			tempRoot = tempRoot.rightptr;
		}
		while(tempRoot != null);
			
		return false;		
	}
	

	/**
	 * 
	 */
	public  void putAll(Map<? extends K,? extends V> map){
		super.putAll(map);
	}
	
	/**
	 * @return the value corresponding to the key, if found. Null if not found
	 */
	public V get(Object key){
		if (key == null)
			throw new NullPointerException("in BPTree - containsKey(Object key) - key is null");
		
		return locate(key); // this statement may throw runtime class cast exception	
	}
	
	
	/**
	 * remove the entry corresponding to the key from the tree
	 * @return the value corresponding the key, if found. null if not found
	 */
	public V remove(Object key_in) {
		
		size--;
		
		//System.out.println("size is now:  " + size());
		return null;
	}

	/**
	 * the design of this method is copied from the
	 * Source code example for "A Practical Introduction
	 * to Data Structures and Algorithm Analysis"
	 * by Clifford A. Shaffer, Prentice Hall, 1998.
	 * Copyright 1998 by Clifford A. Shaffer
	 * @param key - the key of the new entry
	 * @param value - value of the new entry
	 * @return valud previously associate with the key if it is found. Otherwise null
	 */
	@SuppressWarnings("unchecked")
	public V put(K key, V value){
		
		if (key == null)
			throw new NullPointerException("in BPTree - put(K key, V value) - the key passed in is null");
		
		if (value == null)
			throw new NullPointerException("in BPTree - put(K key, V value) - the value passed in is null");
		
		Pair<K, Object> temp = new Pair<K, Object>();
		
		if (root == null){ // no element in tree yet
			// create new root 
			root = new BPNode(leafOrder, true);
			// insert the new value into the array
			root.putInArray(0, new Pair<K, Object>(key, value));			
			// update size
			this.incrementSize();
			return null;
		}
		try{
			temp = putAux(root, key, value);
			// update size
			this.incrementSize();
			// check the child of root split or not
			if (temp != null){ // the root split
				// increase the height of the B+ tree by one, update root
				BPNode newRoot = new BPNode(order, false); // the new root must be an internal node
				// update new root record
				newRoot.putInArray(0, new Pair<K, Object>(null, root));
				newRoot.putInArray(1, temp);
				// update root
				root = newRoot;
				newRoot = null;
			}
		}	
		catch(DuplicateRecordException e){ // encounter duplicate record, return the old one
			return (V)e.getOldRecord();
		}

		return null;
	}
	

	/**
	 * the design of this method is copied from the
	 * Source code example for "A Practical Introduction
	 * to Data Structures and Algorithm Analysis"
	 * by Clifford A. Shaffer, Prentice Hall, 1998.
	 * Copyright 1998 by Clifford A. Shaffer
	 * @param curRoot
	 * @param key
	 * @param value
	 * @return
	 */
	public Pair<K, Object> putAux(BPNode curRoot, K key, V value)
		throws DuplicateRecordException {
	
		Pair<K, Object> temp = new Pair<K, Object>(); // new pair to be inserted
		// find the inserting position or index of the subChild to be followed
		int currec = curRoot.binaryle(key);	 
				
		if (curRoot.isLeaf){ // it is a leaf	
			if (currec < curRoot.numrec && curRoot.recarray[currec].key.equals(key)){ // duplicate record
				Object oldValue = curRoot.recarray[currec].pointer;
				curRoot.recarray[currec].pointer = value;
				throw new DuplicateRecordException(oldValue); // return the duplicate record
			}
			
			temp = new Pair<K, Object>(key, value); // new pair to be inserted
			if (curRoot.numrec < leafOrder){ // the leave node is not full
				curRoot.putInArray(currec, temp);
				return null; // no new pair to be returned
			}
			else{ // the leaf is full
				BPNode newNode = curRoot.splitNode(currec, temp);
				return new Pair<K, Object>(newNode.recarray[0].key, newNode);				
			}
		}
		else{ // internal node
			// go to the next level
			temp = putAux((BPNode)curRoot.recarray[currec].pointer, key, value); 			
			if (temp == null) return null; // the child did not splite
			// the child split, we need to add the pair temp into the node
			if (curRoot.numrec < order){ // the internal node is not full
				curRoot.putInArray(currec+1, temp); // currec+1 is the inserting position
				return null; // no new pair to be returned
			}
			else{ // the internal node is full, need spliting
				BPNode newNode = curRoot.splitNode(currec+1, temp);
				return new Pair<K, Object>(newNode.recarray[0].key, newNode);
			}
		}		
	}
	
	/**
	 * the code below is adopt from SkipList.java in the xml.jar, 
	 * make some modification. make sure BPTree.entrySet().iterator 
	 * works correctly
	 */
	public Set<Map.Entry<K,V>> entrySet(){

		return new AbstractSet<Map.Entry<K,V>>() {
			
			// Adds the specified element to this set if it is not already present. 
			/**
			public boolean add(Entry<K, V> o) {
				K key = o.getKey();
				V value = o.getValue();
				boolean add = BPTree.this.containsKey(key);
				BPTree.this.put(key, value);
				return !add; 
			}
			*/
			
			// Returns an iterator over the elements in this set.
			public Iterator<Entry<K, V>> iterator() {
				return BPTree.this.entryIterator();
			}
			
			// Returns the number of elements in this set (its cardinality).
			public int size() {
				return BPTree.this.size;
			}
			
		};
		
	}
	
	
	/***
	 * the following method was copy from SkipList.java in xml.jar, 
	 * make no modification
	 * @return a new iterator for entries of this tree(start from the lowest
	 * order using the current comparator)
	 */
	protected Iterator<Entry<K, V>> entryIterator() {
		return new EntryIterator();
	}
	
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	protected BPNode locateNode(K key, int[] index){
		if (size == 0)
			return null;
		
		BPNode curRoot = root;
		int currec = 0;
		while (!curRoot.isLeaf){
			currec = curRoot.binaryle(key);
			curRoot = (BPNode)(curRoot.recarray[currec].pointer);
		} // exit when curRoot is a leaf

		currec = curRoot.binaryle(key); 
		
		if (currec == curRoot.numrec) // the insert position is at the end of the array,
									  // means that no element in the array is equal to key
			return null;
		
		if ((curRoot.recarray[currec].key).equals(key)){
			index[0] = currec;
			return curRoot; 
		}
		else
			return  null; // not target is found
	}

	/**
	 * the following class was copied from SkipList.java in 
	 * xml.jar(Author: Evan Machusak) make some modification. 
	 * the usage of modCount from TreeMap.java
	 */
	protected class EntryIterator implements java.util.Iterator<Entry<K, V>> {
		
		protected int expectedModCount = BPTree.this.modCount;
		protected BPNode curNode; // current BPNode
		protected int curIndex; // current pair
		protected K lastReturnedKey;
		
		protected EntryIterator() {
			curNode = locateHead(); // go to the head of the leaves or null if root == null
			curIndex = 0;
			lastReturnedKey = null;
		}
		
		@SuppressWarnings("unchecked")
		public Entry<K, V> next() {
			if (curNode == null) 
				throw new NoSuchElementException();
			if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
			// get the data corresponding to the current index
			Pair<K, Object> value = curNode.recarray[curIndex];
			// update the current index and node(if necessay)
			if(curIndex + 1 == curNode.numrec){ // 
				curIndex = 0;
				curNode = curNode.rightptr;
			}
			else
				curIndex++;
			
			this.lastReturnedKey = value.key;
			// return the old value
			return (Pair<K, V>)value;
		}
		
		public boolean hasNext() {
			return curNode != null;
		}
		
		public void remove() {
			
			if (this.lastReturnedKey == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
			
			if (curNode != null){
				int[] index = new int[1];
				K currentKey = this.curNode.recarray[this.curIndex].key;
				// remove
				BPTree.this.remove(this.lastReturnedKey);
				// update
				curNode = BPTree.this.locateNode(currentKey, index);
				this.curIndex = index[0];
				this.lastReturnedKey = null;
			}
			else{
				BPTree.this.remove(this.lastReturnedKey);
				this.lastReturnedKey = null;
			}
			// update expect mod count
			expectedModCount++;
		}
	}
	
	/**
	 * the implementation of this method is modified from the source code of 
	 * SkipList.java in xml.jar(Author: Evan Machusak). make sure BPTree.keySet().iterator works 
	 * correctly
	 */
	public Set<K> keySet(){
		return new AbstractSet<K>() {
			
			// Returns an iterator over the elements in this set.
			public Iterator<K> iterator() {
				return BPTree.this.keyIterator();
			}

			//Returns the number of elements in this set (its cardinality).
			public int size() {
				return BPTree.this.size;
			}
		};
	}
	
	
	/**
	 * this method is copied from SkipList.java in xml.jar
	 * @return a new iterator for keys of this map(start from the lowest
	 * order using the current comparator)
	 */
	protected Iterator<K> keyIterator() {
		return new KeyIterator();
	}
	
	/**
	 * this method is modified from SkipList.java in xml.jar(Author: Evan Machusak)
	 * the usage of modCount from TreeMap.java
	 * 
	 */
	protected class KeyIterator implements java.util.Iterator<K> {
		
		protected int expectedModCount = BPTree.this.modCount;
		protected BPNode curNode; // current BPNode
		protected int curIndex; // current pair
		protected K lastReturnedKey;
		
		protected KeyIterator() {
			curNode = locateHead(); // go to the head of the leaves or null if root == null
			curIndex = 0;
			lastReturnedKey = null;
		}
		
		public K next() {
			if (curNode == null) throw new NoSuchElementException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

			// get the data corresponding to the current index
			Pair<K, Object> value = curNode.recarray[curIndex];
			// update the current index and node(if necessay)
			if(curIndex + 1 == curNode.numrec){ // 
				curIndex = 0;
				curNode = curNode.rightptr;
			}
			else
				curIndex++;
			
			this.lastReturnedKey = value.key;
			// return the old value
			return value.key;
		}
		
		public boolean hasNext() {
			return curNode != null;
		}
		
		public void remove() {
			
			if (this.lastReturnedKey == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
			
			if (curNode != null){
				int[] index = new int[1];
				K currentKey = this.curNode.recarray[this.curIndex].key;
				// remove
				BPTree.this.remove(this.lastReturnedKey);
				// update
				curNode = BPTree.this.locateNode(currentKey, index);
				this.curIndex = index[0];
				this.lastReturnedKey = null;
			}
			else{
				BPTree.this.remove(this.lastReturnedKey);
				this.lastReturnedKey = null;
			}
			// update expect mod count
			expectedModCount++;
		}
	}
	
	/**
	 * this class is modified from SkipList.java in xml.jar(Author: Evan Machusak)
	 * make sure BPTree.values().iterator works correctly
	 */
	public Collection<V> values(){
		return new AbstractCollection<V>() {
			
			// Returns an iterator over the elements in this set.
			public Iterator<V> iterator() {				
				return BPTree.this.valueIterator();
			}

			//Returns the number of elements in this set (its cardinality).
			public int size() {
				return BPTree.this.size;
			}
			
			// the class below is copied from abstractSet - equals method
		    public boolean equals(Object o) {
		    	if (o == this)
		    	    return true;

		    	if (!(o instanceof Collection))
		    	    return false;
		    	Collection c = (Collection) o;
		    	if (c.size() != size())
		    	    return false;
		    	try {
		    		return containsAll(c);
		    	} catch(ClassCastException unused)   {
		    		return false;
		    	} catch(NullPointerException unused) {
		    		return false;
		    	}
		    }
		};
	}

	/**
	 * the method is copied from the SkipList.java in xml.jar
	 * @return a new iterator for value mapped by keys in the tree
	 * (start from the value corresponding to the lowest key in tree)
	 */
	protected Iterator<V> valueIterator() {
		return new ValueIterator();
	}	
	
	/**
	 * this class is modified from SkipList.java in xml.jar(Author: Evan Machusak)
	 * the usage of modCount from TreeMap.java
	 * @author Daozheng Chen
	 *
	 */
	protected class ValueIterator implements java.util.Iterator<V> {
		
		protected int expectedModCount = BPTree.this.modCount;
		protected BPNode curNode; // current BPNode
		protected int curIndex; // current pair
		protected K lastReturnedKey;
		
		protected ValueIterator() {
			curNode = locateHead(); // go to the head of the leaves or null if root == null
			curIndex = 0;
			lastReturnedKey = null;
		}
		
		@SuppressWarnings("unchecked")
		public V next() {
			if (curNode == null) 
				throw new NoSuchElementException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

			// get the data corresponding to the current index
			Pair<K, Object> value = curNode.recarray[curIndex];
			// update the current index and node(if necessay)
			if(curIndex + 1 == curNode.numrec){ // 
				curIndex = 0;
				curNode = curNode.rightptr;
			}
			else
				curIndex++;
			
			this.lastReturnedKey = value.key;
			// return the old value
			return (V)value.pointer;
		}
		
		public boolean hasNext() {
			return curNode != null;
		}
		
		public void remove() {
			
			if (this.lastReturnedKey == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
			
			if (curNode != null){
				int[] index = new int[1];
				K currentKey = this.curNode.recarray[this.curIndex].key;
				// remove
				BPTree.this.remove(this.lastReturnedKey);
				// update
				curNode = BPTree.this.locateNode(currentKey, index);
				this.curIndex = index[0];
				this.lastReturnedKey = null;
			}
			else{
				BPTree.this.remove(this.lastReturnedKey);
				this.lastReturnedKey = null;
			}
			// update expect mod count
			expectedModCount++;
		}
	}
	
	

	/**
	 * helper method for printing the B+ tree. Printing the indent
	 * @param out
	 * @param indent
	 */
	protected void indent(PrintStream out, int indent){
		for (int i = 0; i < indent; out.print("\t"), i++);
	}
	
	
	
	
	public org.w3c.dom.Node toXML(){
		
		if (size == 0)
			return null;
		
		XMLElement bptree = new XMLElement(CommandAttributes.BPTREE, null);
		
		// add the four attributes to <bptree> node
		bptree.setAttribute(CommandAttributes.CARDINALITY, String.valueOf(this.size));
		bptree.setAttribute(CommandAttributes.HEIGHT, String.valueOf(this.height()));
		bptree.setAttribute(CommandAttributes.BPORDER, String.valueOf(this.order));
		bptree.setAttribute(CommandAttributes.LEAFORDER, String.valueOf(this.leafOrder));		
		bptree.appendChild(toXMLAux(root));

		return bptree;
	}
	
	/**
	 * 
	 * @param curNode
	 * @return
	 */
	protected org.w3c.dom.Node toXMLAux(BPNode curNode){
		
		if (curNode == null)
			throw new RuntimeException("in BPTree - toXMLAux - curNode passed in is null");

		if (curNode.isLeaf){
			XMLElement ele = new XMLElement(CommandAttributes.LEAF, null);
			for(int i = 0; i < curNode.numrec; i++)
				ele.appendChild(curNode.recarray[i].toXML());
			return ele;
		}
		else{
			XMLElement ele = new XMLElement(CommandAttributes.GUIDE, null);
			
			ele.appendChild(toXMLAux(((BPNode)(curNode.recarray[0].pointer))));
			for(int i = 1; i < curNode.numrec; i++){
				XMLElement child = new XMLElement(CommandAttributes.KEY, null);
				child.setAttribute(CommandAttributes.VALUE, curNode.recarray[i].key.toString());
				ele.appendChild(child);
				ele.appendChild(toXMLAux(((BPNode)(curNode.recarray[i].pointer))));
			}
			return ele;
		}
	}
	
	
	/**************************************************************
	* You are not responsible for the following SortedMap functions, 
	* although you are encouraged to implement these functions if you 
	* have time. Without implementing them, the BPTree cannot truely 
	* replace another SortedMap. If you decide to implement any of 
	* these functions, do NOT throw the UnsupportedOperationException.
	***************************************************************/
	public SortedMap<K,V> headMap(K toKey)
	{
		throw new UnsupportedOperationException("Not required");
	}

	public SortedMap<K,V> tailMap(K fromKey)
	{
		throw new UnsupportedOperationException("Not required");
	}

	public SortedMap<K,V> subMap(K fromKey, K toKey)
	{
		throw new UnsupportedOperationException("Not required");
	}
}



























