package com.smith.astar;

public class Node {
    public boolean[] walls = new boolean[4];
    public boolean[] doors = new boolean[4];
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
    public boolean inQueue = false;
    public boolean closed = false;
    public int positionInHeap = -1;

    public int cellType;
    public boolean wallRight = false;
    public boolean wallTop = false;
    public boolean wallLeft = false;
    public boolean wallBottom = false;
    public boolean blocked = false;
    public boolean isSpace = true;
    public int weight = 2;
    public void calculateCosts() {
        // No diagonal movement so diff = diffI + diffJ
        if (StartNode == null || EndNode == null) {
            gCost = 99999;
            hCost = 99999;
            return;
        }
        if (parent == null) {
            gCost = 9999;

        }
        else {
            //parent.calculateCosts();
            if (isSpace)
                gCost = parent.gCost + (weight * 10);
            else
                gCost = parent.gCost + (weight * 10);
        }

        if (start)
            gCost = 0;
        int diffI = Math.abs(i - EndNode.i);
        int diffJ = Math.abs(j - EndNode.j);
        int diff = Math.abs(diffI - diffJ);
        int diag = Math.max(diffI, diffJ) - diff;
        hCost = ((diag * 14) + (10 * diff)) * weight;
        //hCost = (Math.abs(i - EndNode.i) + Math.abs(j - EndNode.j)) * 10;
        fCost = hCost + gCost;
    }
}
