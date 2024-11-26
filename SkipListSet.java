import java.util.*;

public class SkipListSet<T extends Comparable<T>> implements SortedSet<T> {

    // SkipListNode to represent nodes in the skip list
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

    // Fields for the SkipList
    private SkipListNode head;
    private int maxLevel;
    private int size;
    private Random random;

    // Constructor for the SkipListSet
    public SkipListSet() {
        head = new SkipListNode(null, 1); // Initial head node
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

    // Add a value to the skip list
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

    // If the value already exists, return false
    if (current != null && current.getValue().equals(value)) {
        return false;
    }

    // Generate level for the new node
    int newNodeLevel = generateRandomLevel();
    if (newNodeLevel > maxLevel) {
        for (int i = maxLevel; i < newNodeLevel; i++) {
            update.add(head);
            head.nextNodes.add(null); // Expand the head node to support the new level
        }
        maxLevel = newNodeLevel;
    }

    // Create the new node
    SkipListNode newNode = new SkipListNode(value, newNodeLevel);
    for (int i = 0; i < newNodeLevel; i++) {
        newNode.nextNodes.set(i, update.get(i).nextNodes.get(i));
        update.get(i).nextNodes.set(i, newNode);
    }

    size++;
    return true;
}


    @Override
    public Comparator<? super T> comparator() {
        return null; // Natural ordering
    }

    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return head.nextNodes.get(0).getValue();
    }

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

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

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

    // Internal Iterator class
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

    // If the node to be removed is found
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

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean isModified = false;
        for (T value : c) {
            if (add(value)) {
                isModified = true;
            }
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
        return isModified;
    }
    public void reBalance() {
        List<T> values = new ArrayList<>();
        SkipListNode current = head.nextNodes.get(0);
    
        // Collect all values
        while (current != null) {
            values.add(current.getValue());
            current = current.nextNodes.get(0);
        }
    
        // Reset and add all values back with new random levels
        clear();
        for (T value : values) {
            add(value);
        }
    }
    
    @Override
    public void clear() {
        head = new SkipListNode(null, 1);
        maxLevel = 1;
        size = 0;
    }

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

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

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

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (T element : this) {
            hashCode += (element == null ? 0 : element.hashCode());
        }
        return hashCode;
    }
}
