package com.smith.astar;

import java.util.ArrayList;

public class CustomQueue {
    public ArrayList<Node> queue;
    public CustomQueue() {
        queue = new ArrayList<>();
    }
    public void addToQueue(Node node) {
        queue.add(node);
    }
    public void clear() {
        queue.clear();
    }
    public boolean isEmpty() {
        return queue.size() == 0;
    }
    public boolean contains(Node node) {
        return queue.contains(node);
    }
    public Node getBestNode() {
        // return the node with the lowest G cost
        Node lowestNode = Node.InfiniteNode();
        lowestNode.calculateCosts();
        for(Node node : queue) {
            node.calculateCosts();
            if (node.fCost < lowestNode.fCost)
                lowestNode = node;
        }
        queue.remove(lowestNode);
        return lowestNode;
    }
}
