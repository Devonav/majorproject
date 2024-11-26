/*COP 3503C Major Project
Author: Devon Villalona
Date: 11/26/2024 
*/
import java.util.*;

public class SkipListSet<T extends Comparable<T>> implements SortedSet<T> {

    // SkipListNode to represent nodes in the skip list with a value and a list of next nodes at different levels 
    private class SkipListNode {
        T value;
        ArrayList<SkipListNode> nextNodes;

        SkipListNode(T value, int level) {
            this.value = value;
            this.nextNodes = new ArrayList<>(Collections.nCopies(level, null));
        }

        T getValue() {
            return value;
        }
    }

    // Fields for the SkipList class to maintain the skip list structure and properties 
    private SkipListNode head;
    private int maxLevel;
    private int size;
    private Random random;

    // Constructor for the SkipListSet
    public SkipListSet() {
        head = new SkipListNode(null, 1); // Initial head node with value null and level 1  
        maxLevel = 1;
        size = 0;
        random = new Random();
    }

    // Utility method to generate a random level for node insertion 
    private int generateRandomLevel() {
        int level = 1;
        while (random.nextDouble() < 0.5 && level < maxLevel + 1) {
            level++;
        }
        return level;
    }

    // Add a value to the skip list set     
    @Override
public boolean add(T value) {
    ArrayList<SkipListNode> update = new ArrayList<>(Collections.nCopies(maxLevel, null));
    SkipListNode current = head;

    // Traverse from the highest level to find the insertion point
    for (int i = maxLevel - 1; i >= 0; i--) {
        while (current.nextNodes.get(i) != null &&
               current.nextNodes.get(i).getValue().compareTo(value) < 0) {
            current = current.nextNodes.get(i);
        }
        update.set(i, current);
    }

    current = current.nextNodes.get(0);

    // If the value already exists, return false without adding it again 
    if (current != null && current.getValue().equals(value)) {
        return false;
    }

    // Generate level for the new node and update the head node if necessary    
    int newNodeLevel = generateRandomLevel();
    if (newNodeLevel > maxLevel) {
        for (int i = maxLevel; i < newNodeLevel; i++) {
            update.add(head);
            head.nextNodes.add(null); // Expand the head node to support the new level
        }
        maxLevel = newNodeLevel;
    }

    // Create the new node and insert it into the skip list     
    SkipListNode newNode = new SkipListNode(value, newNodeLevel);
    for (int i = 0; i < newNodeLevel; i++) {
        newNode.nextNodes.set(i, update.get(i).nextNodes.get(i));
        update.get(i).nextNodes.set(i, newNode);
    }

    size++;
    return true;
}

    // Methods inherited from the SortedSet interface 
    @Override
    public Comparator<? super T> comparator() {
        return null; // Natural ordering
    }
    //  Returns the first element in the skip list set
    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return head.nextNodes.get(0).getValue();
    }
    //  Returns the last element in the skip list set
    @Override
    public T last() {
        SkipListNode current = head;
        for (int i = maxLevel - 1; i >= 0; i--) {
            while (current.nextNodes.get(i) != null) {
                current = current.nextNodes.get(i);
            }
        }
        if (current == head) {
            throw new NoSuchElementException();
        }
        return current.getValue();
    }
    //  Returns a view of the portion of the skip list set whose elements are strictly less than toElement
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    //  Returns the number of elements in the skip list set
    @Override
    public int size() {
        return size;
    }
    //  Returns the subset of the skip list set whose elements are greater than or equal to fromElement and less than toElement
    @Override
    public boolean contains(Object o) {
        T value = (T) o;
        SkipListNode current = head;
    
        for (int i = maxLevel - 1; i >= 0; i--) {
            while (i < current.nextNodes.size() &&
                   current.nextNodes.get(i) != null &&
                   current.nextNodes.get(i).getValue().compareTo(value) < 0) {
                current = current.nextNodes.get(i);
            }
        }
    
        current = current.nextNodes.get(0);
        return current != null && current.getValue().equals(value);
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll operation is not supported.");
    }
    
    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        throw new UnsupportedOperationException("subSet operation is not supported.");
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        throw new UnsupportedOperationException("headSet operation is not supported.");
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException("tailSet operation is not supported.");
    }

    // Internal Iterator class to iterate over the elements in the skip list set 
    private class SkipListSetIterator implements Iterator<T> {
        private SkipListNode current;

        SkipListSetIterator() {
            current = head.nextNodes.get(0);
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T value = current.getValue();
            current = current.nextNodes.get(0);
            return value;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new SkipListSetIterator();
    }

    @Override
    // Remove a value from the skip list set    
public boolean remove(Object o) {
    T value = (T) o;
    ArrayList<SkipListNode> update = new ArrayList<>(Collections.nCopies(maxLevel, null));
    SkipListNode current = head;

    // Find the nodes that need updating before the node to be deleted
    for (int i = maxLevel - 1; i >= 0; i--) {
        while (i < current.nextNodes.size() &&
               current.nextNodes.get(i) != null &&
               current.nextNodes.get(i).getValue().compareTo(value) < 0) {
            current = current.nextNodes.get(i);
        }
        update.set(i, current);
    }

    current = current.nextNodes.get(0);

    // If the node to be removed is found in the skip list 
    if (current != null && current.getValue().equals(value)) {
        for (int i = 0; i < current.nextNodes.size(); i++) {
            if (update.get(i).nextNodes.get(i) != current) {
                break;
            }
            update.get(i).nextNodes.set(i, current.nextNodes.get(i));
        }

        // Decrease levels if necessary
        while (maxLevel > 1 && head.nextNodes.get(maxLevel - 1) == null) {
            maxLevel--;
        }

        size--;
        return true;
    }
    return false; // Node not found
}
// Rebalance the skip list set by rebuilding it from scratch    
public void reBalance() {
    if (maxLevel > Math.log(size) / Math.log(4)) { // Adjusted threshold to trigger balancing more frequently
        List<T> values = new ArrayList<>();
        SkipListNode current = head.nextNodes.get(0);

        while (current != null) {
            values.add(current.getValue());
            current = current.nextNodes.get(0);
        }

        clear();
        for (T value : values) {
            add(value);
        }
    }
}

   
    @Override
public boolean addAll(Collection<? extends T> c) {
    boolean isModified = false;
    for (T value : c) {
        if (add(value)) {
            isModified = true;
        }
    }
    if (isModified) {
        reBalance();  // Call reBalance after bulk addition 
    }
    return isModified;
}

@Override
public boolean removeAll(Collection<?> c) {
    boolean isModified = false;
    for (Object value : c) {
        if (remove(value)) {
            isModified = true;
        }
    }
    if (isModified) {
        reBalance();  // Call reBalance after bulk removal 
    }
    return isModified;
}

    //  Returns the subset of the skip list set whose elements are greater than or equal to fromElement and less than toElement 
    @Override
    public void clear() {
        head = new SkipListNode(null, 1);
        maxLevel = 1;
        size = 0;
    }
    //  Returns an array containing all the elements in the skip list set 
    @Override
    public Object[] toArray() {
        List<T> result = new ArrayList<>();
        SkipListNode current = head.nextNodes.get(0);
        
        while (current != null) {
            result.add(current.getValue());
            current = current.nextNodes.get(0);
        }
        
        return result.toArray();
    }
    //  Returns an array containing all the elements in the skip list set 
    @Override
    public <E> E[] toArray(E[] a) {
        List<T> result = new ArrayList<>();
        SkipListNode current = head.nextNodes.get(0);
        
        while (current != null) {
            result.add(current.getValue());
            current = current.nextNodes.get(0);
        }
        
        return result.toArray(a);
    }
    //  Returns true if the skip list set contains the specified element 
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }
    //  Returns true if the skip list set contains all the elements in the specified collection
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Set)) return false;

        Collection<?> c = (Collection<?>) o;
        if (c.size() != size) return false;

        try {
            return containsAll(c);
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }
//  Returns a string representation of the skip list set 
    @Override
    public int hashCode() {
        int hashCode = 0;
        for (T element : this) {
            hashCode += (element == null ? 0 : element.hashCode());
        }
        return hashCode;
    }
}