package com.smith.astar;

import java.util.ArrayList;
import java.util.Arrays;

public class CustomQueue {
    public ArrayList<Node> queue;
    public Node[] heap;
    public int maxSize = 16;
    public int items = 0;
    boolean useHeap = true;
    public CustomQueue() {
        queue = new ArrayList<>();
        heap = new Node[maxSize];
    }
    public void addToQueue(Node node) { // O(log(n))
        node.inQueue = true;
        if (useHeap) {
            node.calculateCosts();
            if (items == maxSize) {
                // Resize Array
                heap = Arrays.copyOf(heap, maxSize * 2);
                maxSize *= 2;
            }
            heap[items] = node;
            node.positionInHeap = items;
            items++;
            heapifyUp(items-1);
        } else {
            queue.add(node);
        }
    }
    private int getLeftChildIndex(int parentIndex) { return 2 * parentIndex + 1; }
    private int getRightChildIndex(int parentIndex) { return 2 * parentIndex + 2; }
    private int getParentIndex(int childIndex) { return (childIndex - 1) / 2; }
    public boolean hasLeftChild(int index) { return getLeftChildIndex(index) < items; }
    public boolean hasRightChild(int index) { return getRightChildIndex(index) < items; }
    public boolean hasParent(int index) { return getParentIndex(index) >= 0; }

    public Node leftChild(int index) { return heap[getLeftChildIndex(index)]; }
    public Node rightChild(int index) { return heap[getRightChildIndex(index)]; }
    public Node parent(int index) { return heap[getParentIndex(index)]; }

    public void swap(int index1, int index2) {
        Node temp = heap[index1];
        heap[index1] = heap[index2];
        heap[index2] = temp;
        heap[index1].positionInHeap = index1;
        heap[index2].positionInHeap = index2;
    }
    public void clear() {       // O(1)
        if (useHeap) {
            items = 0;
        } else {
            queue.clear();
        }
    }
    public boolean isEmpty() {  // O(1)
        if (useHeap) {
            return items == 0;
        } else {
            return queue.size() == 0;
        }
    }
    public boolean contains(Node node) {    // O(N) => O(1)
        return node.inQueue;
//        return queue.contains(node);
    }
    public Node getBestNode() { // O(N) => O(log(n))
        if (useHeap) {
            Node item = heap[0];
            heap[0] = heap[items-1];
            items--;
            heapifyDown(0);
            item.inQueue = false;
            item.closed = true;
            return item;
        } else {

            Node lowestNode = queue.get(0);
            lowestNode.calculateCosts();
            for(Node node : queue) {
                node.calculateCosts();
                if (node.fCost <= lowestNode.fCost)
                    lowestNode = node;
            }
            queue.remove(lowestNode);
            lowestNode.inQueue = false;
            lowestNode.closed = true;
            return lowestNode;
        }
    }
    public void checkPosition(int index) {
        //heapifyDown(index);
        heapifyUp(index);
    }
    private void heapifyUp(int index) {
        while (hasParent(index) && parent(index).fCost > heap[index].fCost) {
            swap(getParentIndex(index), index);
            index = getParentIndex(index);
        }
    }
    private void heapifyDown(int index) {
        while(hasLeftChild(index)) {
            int smallerChildIndex = getLeftChildIndex(index);
            if (hasRightChild(index) && rightChild(index).fCost < leftChild(index).fCost) {
                smallerChildIndex = getRightChildIndex(index);
            }
            if (heap[index].fCost < heap[smallerChildIndex].fCost) {
                break;
            }
            swap(index, smallerChildIndex);
            index = smallerChildIndex;
        }
    }
}
