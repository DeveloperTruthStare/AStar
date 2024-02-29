package com.smith.astar;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Ship {
    public static int numberOfShips = 0;
    public Node[][] grid;
    public int SHIP_WIDTH = -1, SHIP_HEIGHT = -1;
    public int shipId;
    public int posX, posY;
    public ArrayList<Point2D> validRooms = new ArrayList<>();
    public HashMap<String, ArrayList<Point2D>> paths = new HashMap<String, ArrayList<Point2D>>();;
    public String fileName;
    public Ship(String fileName, int roomWeight, int posX, int posY) throws FileNotFoundException {
        shipId = numberOfShips;
        numberOfShips++;
        this.posX = posX;
        this.posY = posY;
        this.fileName = fileName;
        File myObj = new File(fileName);
        Scanner myReader = new Scanner(myObj);
        int y = -1;
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String[] parts = data.split("=");
            if (parts.length != 2) {
                System.err.println("Error reading line: " + data);
                continue;
            }
            switch (parts[0]) {
                case "IMAGE":
                    break;
                case "RACE":
                    break;
                case "CLASS":
                    break;
                case "SHIELDTYPE":
                    break;
                case "HEALTH":
                    break;
                case "HSIZE":
                    SHIP_HEIGHT = Integer.parseInt(parts[1]);
                    y = SHIP_HEIGHT-1;
                    initializeGrid();
                    break;
                case "WSIZE":
                    SHIP_WIDTH = Integer.parseInt(parts[1]);
                    initializeGrid();
                    break;
                case "CELLS":
                    addCellRow(parts[1], y, roomWeight);
                    y--;
                    break;
                default:
                    break;
            }
        }
        for(int i = 0; i < validRooms.size(); ++i) {
            for (int j = 0; j < validRooms.size(); ++j) {
                if (i == j) continue;
                findPath(validRooms.get(i), validRooms.get(j));
            }
        }
    }
    public void initializeGrid() {
        if (SHIP_WIDTH != -1 && SHIP_HEIGHT != -1) {
            grid = new Node[SHIP_WIDTH][SHIP_HEIGHT];
            for(int i = 0; i < SHIP_WIDTH; ++i) {
                for(int j = 0; j < SHIP_HEIGHT; ++j) {
                    grid[i][j] = new Node();
                    grid[i][j].i = i;
                    grid[i][j].j = j;
                }
            }
        }
    }
    private void addCellRow(String data, int y, int roomWeight) {
        if (SHIP_WIDTH == -1 || SHIP_HEIGHT == -1) {
            System.err.println("WSIZE and HSIZE must be before CELLS data");
            return;
        }

        int x = 0;
        while (data.length() >= 5) {
            String cell = data.substring(0, 5);
            data = data.substring(5);
            switch(Character.getNumericValue(cell.charAt(0))) {
                case 0:
                    // Empty Space
                    break;
                case 1:
                    // Blocked
                    grid[x][y].blocked = true;
                    break;
                case 2:
                    // Room
                    grid[x][y].isSpace = false;
                    grid[x][y].weight = roomWeight;
                    int c = Character.getNumericValue(cell.charAt(1));
                    grid[x][y].walls[0] = c % 2 == 1;
                    grid[x][y].walls[2] = c > 1;
                    c = Character.getNumericValue(cell.charAt(2));
                    grid[x][y].walls[1] = c % 2 == 1;
                    grid[x][y].walls[3] = c > 1;
                    c = Character.getNumericValue(cell.charAt(3));
                    grid[x][y].doors[0] = c % 2 == 1;
                    grid[x][y].doors[2] = c > 1;
                    c = Character.getNumericValue(cell.charAt(4));
                    grid[x][y].doors[1] = c % 2 == 1;
                    grid[x][y].doors[3] = c > 1;
                    validRooms.add(new Point(x, y));
                    break;
            }
            ++x;
        }
    }
    private void resetPathfinding() {
        for(int y = 0; y < SHIP_HEIGHT; ++y) {
            for(int x = 0; x < SHIP_WIDTH; ++x) {
                grid[x][y].closed = false;
                grid[x][y].inQueue = false;
                grid[x][y].isOnPath = false;
                grid[x][y].parent = null;
                grid[x][y].start = false;
                grid[x][y].end = false;

            }
        }
    }
    CustomQueue heap = new CustomQueue();
    public ArrayList<Point2D> findPath(Point2D startPos, Point2D endPos) {
        String pathKey = String.valueOf(startPos.getX()) + String.valueOf(startPos.getY()) + ":" + String.valueOf(endPos.getX()) + String.valueOf(endPos.getY());
        if (paths.containsKey(pathKey)) {
            System.out.println("Pulling from cache");
            return paths.get(pathKey);
        }
        ArrayList<Point2D> optimalPath = new ArrayList<>();
        resetPathfinding();
        heap.clear();

        Node.StartNode = grid[(int)startPos.getX()][(int)startPos.getY()];
        Node.StartNode.start = true;
        Node.EndNode = grid[(int)endPos.getX()][(int)endPos.getY()];
        Node.EndNode.end = true;
        heap.addToQueue(grid[(int)startPos.getX()][(int)startPos.getY()]);
        while(!heap.isEmpty()) {
            Node currentNode = heap.getBestNode();
            if (currentNode.end)
                break;
            currentNode.calculateCosts();
            if (currentNode.i > 0 && (!currentNode.walls[0] || currentNode.doors[0])) {
                Node nextNode = grid[currentNode.i-1][currentNode.j];
                checkNode(nextNode, currentNode);
            }
            if (currentNode.i < SHIP_WIDTH-1 && (!currentNode.walls[2] || currentNode.doors[2])) {
                Node nextNode = grid[currentNode.i+1][currentNode.j];
                checkNode(nextNode, currentNode);
            }
            if (currentNode.j > 0 && (!currentNode.walls[3] || currentNode.doors[3])) {
                Node nextNode = grid[currentNode.i][currentNode.j-1];
                checkNode(nextNode, currentNode);
            }
            if (currentNode.j < SHIP_HEIGHT-1 && (!currentNode.walls[1] || currentNode.doors[1])) {
                Node nextNode = grid[currentNode.i][currentNode.j+1];
                checkNode(nextNode, currentNode);
            }
        }
        if (Node.EndNode.parent == null) return null;
        Node node = Node.EndNode;
        optimalPath.add(new Point(node.i, node.j));
        while (!node.start) {
            node.isOnPath = true;
            node = node.parent;
            node.isOnPath = true;
            optimalPath.add(new Point(node.i, node.j));
        }
        paths.put(pathKey, optimalPath);
        return optimalPath;
    }
    public void checkNode(Node next, Node cur) {
        if (next.closed) return;
        if (next.blocked) return;
        if (heap.contains(next)) {
            next.calculateCosts();
            if (next.gCost > cur.gCost + next.weight) {
                next.parent = cur;
                next.calculateCosts();
                heap.checkPosition(next.positionInHeap);
            }
        } else {
            next.parent = cur;
            next.calculateCosts();
            heap.addToQueue(next);
        }
    }

    int CELL_WIDTH = 50;
    int CELL_HEIGHT = 50;
    int CELL_X_SPACING = 1;
    int CELL_Y_SPACING = 1;

    public void draw(ShapeRenderer shapeRenderer) {
        for (int x = 0; x < SHIP_WIDTH; ++x) {
            for(int y = 0; y < SHIP_HEIGHT; ++y) {
                shapeRenderer.setColor(1, 1, 1, 0);
                if (grid[x][y].start) {
                    shapeRenderer.setColor(0, 1, 0, 1);
                } else if (grid[x][y].end) {
                    shapeRenderer.setColor(0, 0, 1, 1);
                } else if (grid[x][y].closed) {
                    shapeRenderer.setColor(1, 0, 1, 1);
                } else if (grid[x][y].blocked) {
                    shapeRenderer.setColor(0.75f, 0.5f, 0.5f, 1);
                } else if (!grid[x][y].isSpace) {
                    shapeRenderer.setColor(1, 1, 1, 1);
                }
                shapeRenderer.rect(x * (CELL_WIDTH + CELL_X_SPACING), y*(CELL_HEIGHT + CELL_Y_SPACING), CELL_WIDTH, CELL_HEIGHT);
                shapeRenderer.setColor(0, 0, 0, 1);
                if (grid[x][y].walls[0]) {
                    shapeRenderer.rect(x*(CELL_WIDTH + CELL_X_SPACING), y * (CELL_HEIGHT + CELL_Y_SPACING), 5, CELL_HEIGHT);
                }
                if (grid[x][y].walls[3]) {
                    shapeRenderer.rect(x*(CELL_WIDTH + CELL_X_SPACING), y * (CELL_HEIGHT + CELL_Y_SPACING), CELL_WIDTH, 5);
                }
                if (grid[x][y].walls[2]) {
                    shapeRenderer.rect((x+1)*(CELL_WIDTH + CELL_X_SPACING) - (CELL_X_SPACING+5), y * (CELL_HEIGHT + CELL_Y_SPACING), 5, CELL_HEIGHT);
                }
                if (grid[x][y].walls[1]) {
                    shapeRenderer.rect(x * (CELL_WIDTH + CELL_X_SPACING), (y + 1) * (CELL_HEIGHT + CELL_Y_SPACING) - (CELL_Y_SPACING + 5), CELL_WIDTH, 5);
                }
                shapeRenderer.setColor(1, 0.62f, 0, 1);
                if (grid[x][y].doors[0]) {
                    shapeRenderer.rect(x*(CELL_WIDTH + CELL_X_SPACING), y * (CELL_HEIGHT + CELL_Y_SPACING) + (CELL_HEIGHT/4), 5, CELL_HEIGHT/2);
                }
                if (grid[x][y].doors[3]) {
                    shapeRenderer.rect(x*(CELL_WIDTH + CELL_X_SPACING) + (CELL_WIDTH/4), y * (CELL_HEIGHT + CELL_Y_SPACING), CELL_WIDTH/2, 5);
                }
                if (grid[x][y].doors[2]) {
                    shapeRenderer.rect((x+1)*(CELL_WIDTH + CELL_X_SPACING) - (CELL_X_SPACING+5), y * (CELL_HEIGHT + CELL_Y_SPACING) + (CELL_HEIGHT/4), 5, CELL_HEIGHT/2);
                }
                if (grid[x][y].doors[1]) {
                    shapeRenderer.rect(x*(CELL_WIDTH + CELL_X_SPACING) + (CELL_WIDTH/4), (y+1) * (CELL_HEIGHT + CELL_Y_SPACING) - (CELL_Y_SPACING+5), CELL_WIDTH/2, 5);
                }
                if (grid[x][y].isOnPath) {
                    shapeRenderer.setColor(0, 0, 0, 1);
                    shapeRenderer.rect(x * (CELL_WIDTH + CELL_X_SPACING) + CELL_WIDTH/4, y * (CELL_HEIGHT + CELL_Y_SPACING) + CELL_HEIGHT/4, CELL_WIDTH/2, CELL_HEIGHT/2);
                }
            }
        }
    }
}
