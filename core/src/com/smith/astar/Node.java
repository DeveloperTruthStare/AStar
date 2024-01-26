package com.smith.astar;

public class Node {
    public static Node InfiniteNode() {
        Node inf = new Node();
        inf.hCost = 99999;
        inf.gCost = 99999;
        inf.fCost = 99999;
        return inf;
    }
    public boolean top = false, right = false, left = false, bottom = false;
    public boolean start = false, end = false;
    public Node parent;
    public int i, j;
    public static Node StartNode;
    public static Node EndNode;
    public int gCost;       // Distance form starting node
    public int fCost;       // Total Distance
    public int hCost;       // Distance from end
    public boolean isOnPath = false;
    public void calculateCosts() {
        // No diagonal movement so diff = diffI + diffJ
        if (StartNode == null || EndNode == null) {
            gCost = 99999;
            hCost = 99999;
            return;
        }
        gCost = Math.abs(i - StartNode.i) + Math.abs(j - StartNode.j);
        if (parent == null)
            gCost = 9999;
        else
            gCost = parent.gCost + 1;

        if (start)
            gCost = 0;

        hCost = Math.abs(i - EndNode.i) + Math.abs(j - EndNode.j);
        fCost = hCost + gCost;
    }
}
