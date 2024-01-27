package com.smith.astar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Random;

public class MainScreen implements Screen{
    public InputProcessor inputProcessor = new InputProcessor() {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.SPACE) {
                currentSide++;
                if(currentSide > 3)
                    currentSide = 0;
            } else if (keycode == Input.Keys.A) {
                grid[250][250].end = true;
                Node.EndNode.end = false;
                Node.EndNode = grid[250][250];
            }
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            int i = (screenX - offSetX) / (CELL_WIDTH+CELL_X_SPACING);
            int j = (1080 - screenY - offSetY) / (CELL_HEIGHT+CELL_Y_SPACING);
            if (i < 0 || i > WIDTH-1 || j < 0 || j > HEIGHT-1) return false;
            if (button == 0)
            {
                if (Node.StartNode != null) {
                    Node.StartNode.start = false;
                }
                grid[i][j].start = true;
                Node.StartNode = grid[i][j];

            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            int i = (screenX - offSetX) / (CELL_WIDTH+CELL_X_SPACING);
            int j = (1080 - screenY - offSetY) / (CELL_HEIGHT+CELL_Y_SPACING);
            if (i < 0 || i > WIDTH-1 || j < 0 || j > HEIGHT-1) return false;

            if (button == 0) {
                if (null != Node.EndNode) {
                    Node.EndNode.end = false;
                }
                grid[i][j].end = true;
                Node.EndNode = grid[i][j];
            } else if (button == 1) {
                switch(currentSide) {
                    case 0:
                        grid[i][j].top = !grid[i][j].top;
                        break;
                    case 1:
                        grid[i][j].right = !grid[i][j].right;
                        break;
                    case 2:
                        grid[i][j].bottom = !grid[i][j].bottom;
                        break;
                    case 3:
                        grid[i][j].left = !grid[i][j].left;
                        break;
                }
            }
            return false;
        }

        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            hoveredI = (screenX - offSetX) / (CELL_WIDTH+CELL_X_SPACING);
            hoveredJ = (1080 - screenY - offSetY) / (CELL_HEIGHT+CELL_Y_SPACING);
            if (hoveredI < 0 || hoveredI > WIDTH-1 || hoveredJ < 0 || hoveredJ > HEIGHT-1) hoveredI = -1;

            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            return false;
        }
    };
    public int currentSide = 0;

    SpriteBatch batch;
    int WIDTH = 300;
    int HEIGHT = 300;
    int CELL_WIDTH = 25;
    int CELL_HEIGHT = 25;
    int CELL_X_SPACING = 5;
    int CELL_Y_SPACING = 5;
    int PATH_WIDTH = 10;
    int PATH_HEIGHT = 10;
    Node[][] grid;
    CustomQueue queue;
    ShapeRenderer shapeRenderer;
    int offSetX = 0, offSetY = 0;
    BitmapFont font;
    int hoveredI = -1, hoveredJ = -1;
    public MainScreen() {
        font = new BitmapFont();
        batch = new SpriteBatch();
        Gdx.input.setInputProcessor(inputProcessor);
        batch = new SpriteBatch();
        queue = new CustomQueue();
        shapeRenderer = new ShapeRenderer();
        Random rand = new Random();

        grid = new Node[WIDTH][HEIGHT];
        for(int i = 0; i < WIDTH; ++i) {
            for(int j = 0; j < HEIGHT; ++j) {
                grid[i][j] = new Node();
                grid[i][j].i = i;
                grid[i][j].j = j;
                if (i == 0) {
                    grid[i][j].left = true;
                }else if (i == WIDTH-1) {
                    grid[i][j].right = true;
                }
                if (j == 0) {
                    grid[i][j].bottom = true;
                } else if (j == HEIGHT-1) {
                    grid[i][j].top = true;
                }
                int r = rand.nextInt(0, 120);
                if (i < 20 && j < 20)
                    r = rand.nextInt(0, 10);
                switch(r) {
                    case 0:
                        grid[i][j].bottom = true;
                        break;
                    case 1:
                        grid[i][j].top = true;
                        break;
                    case 2:
                        grid[i][j].right = true;
                        break;
                    case 3:
                        grid[i][j].left = true;
                        break;
                }
            }
        }
        // Randomly assign up to two of the walls to be blocked
    }
    public void checkNode(Node nextNode, Node currentNode) {                                        // O(log(n))
        if (nextNode.closed) return;                                                                // O(1)
        if (queue.contains(nextNode)) {                                                             // O(1)
            nextNode.calculateCosts();                                                              // O(1)
            if (currentNode.gCost + 1 < nextNode.gCost) {                                           // O(1)
                nextNode.parent = currentNode;                                                      // O(1)
                nextNode.calculateCosts();
                queue.checkPosition(nextNode.positionInHeap);
            }
        } else {
            nextNode.parent = currentNode;                                                          // O(1)
            nextNode.calculateCosts();
            queue.addToQueue(nextNode);                                                             // O(log(n))

        }
    }
    boolean noPossiblePath = true;
    public void resetGrid() {
        for(int i = 0; i < WIDTH; ++i) {
            for (int j = 0; j < HEIGHT; ++j) {
                grid[i][j].closed = false;
                grid[i][j].inQueue = false;
                grid[i][j].isOnPath = false;
                grid[i][j].parent = null;
            }
        }
    }
    public void aStarPathFinding() /* O(N^2) * O(log(n)) = O(log(n)N^2) */ {

        resetGrid();    // O(N^2)

        queue.clear();  // O(1)
        queue.addToQueue(Node.StartNode);                                                           // O(log(n)) if done properly - because this is the starting node it's really O(1)
        while (!queue.isEmpty()) {                                                                  // O(NxM) - WIDTH X HEIGHT, basically O(N^2)
            Node currentNode = queue.getBestNode();                                                 // O(1) if done properly O(N) now
            if (currentNode.end)                                                                    // O(1)
                break;
            currentNode.calculateCosts();                                                           // O(1)
            if (Math.abs(currentNode.i - Node.EndNode.i) < Math.abs(currentNode.j - Node.EndNode.j)) { //O(1)
                if (currentNode.i > 0 && !currentNode.left) {                                       // O(1)
                    Node nextNode = grid[currentNode.i - 1][currentNode.j];                         // O(1)
                    if (!nextNode.right)                                                            // O(1)
                        checkNode(nextNode, currentNode);                                           // O(log(n))
                }
                if (currentNode.i < WIDTH - 1 && !currentNode.right) {
                    Node nextNode = grid[currentNode.i + 1][currentNode.j];
                    if (!nextNode.left)
                        checkNode(nextNode, currentNode);
                }
                if (currentNode.j > 0 && !currentNode.bottom) {
                    Node nextNode = grid[currentNode.i][currentNode.j - 1];
                    if (!nextNode.top)
                        checkNode(nextNode, currentNode);
                }
                if (currentNode.j < HEIGHT - 1 && !currentNode.top) {
                    Node nextNode = grid[currentNode.i][currentNode.j + 1];
                    if (!nextNode.bottom)
                        checkNode(nextNode, currentNode);
                }
            } else {
                if (currentNode.j > 0 && !currentNode.bottom) {
                    Node nextNode = grid[currentNode.i][currentNode.j - 1];
                    if (!nextNode.top)
                        checkNode(nextNode, currentNode);
                }
                if (currentNode.j < HEIGHT - 1 && !currentNode.top) {
                    Node nextNode = grid[currentNode.i][currentNode.j + 1];
                    if (!nextNode.bottom)
                        checkNode(nextNode, currentNode);
                }
                if (currentNode.i > 0 && !currentNode.left) {
                    Node nextNode = grid[currentNode.i - 1][currentNode.j];
                    if (!nextNode.right)
                        checkNode(nextNode, currentNode);
                }
                if (currentNode.i < WIDTH - 1 && !currentNode.right) {
                    Node nextNode = grid[currentNode.i + 1][currentNode.j];
                    if (!nextNode.left)
                        checkNode(nextNode, currentNode);
                }
            }
        }
    }
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Find optimal path
        if (!(Node.StartNode == null || Node.EndNode == null))
        {
            for(int count = 0; count < 16; ++count) {
                aStarPathFinding();

                noPossiblePath = Node.EndNode.parent == null;
                if (!noPossiblePath){
                    Node node = Node.EndNode;
                    while (node != Node.StartNode && node != null) {
                        node.isOnPath = true;
                        node = node.parent;
                    }
                }
            }
        }

        for(int i = 0; i < Math.min(64, WIDTH); ++i) {
            for(int j = 0; j < Math.min(36, HEIGHT); ++j) {
                // Draw Node
                shapeRenderer.setColor(1, 1, 1, 1);
                if (grid[i][j].start)
                    shapeRenderer.setColor(0, 1, 0, 1);
                else if (grid[i][j].end)
                    shapeRenderer.setColor(0, 0, 1, 1);
                else if (grid[i][j].closed) {
                    shapeRenderer.setColor(1, 0, 1, 1);
                }
                drawRect(i * (CELL_WIDTH+CELL_X_SPACING), j * (CELL_HEIGHT+CELL_Y_SPACING), CELL_WIDTH, CELL_HEIGHT);

                shapeRenderer.setColor(1, 0, 0, 1);
                if (grid[i][j].left) {
                    drawRect(i*(CELL_WIDTH+CELL_X_SPACING), j*(CELL_HEIGHT+CELL_Y_SPACING), 5, CELL_HEIGHT);
                }
                if (grid[i][j].right) {
                    drawRect((i+1)*(CELL_WIDTH+CELL_X_SPACING) - 10, j*(CELL_HEIGHT+CELL_Y_SPACING), 5, CELL_HEIGHT);
                }
                if (grid[i][j].top) {
                    drawRect(i*(CELL_WIDTH+CELL_X_SPACING), (j+1)*(CELL_HEIGHT+CELL_Y_SPACING)-10, CELL_WIDTH, 5);
                }
                if (grid[i][j].bottom) {
                    drawRect(i*(CELL_WIDTH+CELL_X_SPACING), j*(CELL_HEIGHT+CELL_Y_SPACING), CELL_WIDTH, 5);
                }
                if (grid[i][j].isOnPath) {
                    shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1);
                    drawRect(i*(CELL_WIDTH+CELL_X_SPACING) + (CELL_WIDTH/2-PATH_WIDTH/2), j*(CELL_HEIGHT+CELL_Y_SPACING) + (CELL_HEIGHT/2-PATH_HEIGHT/2), PATH_WIDTH, PATH_HEIGHT);
                }
            }
        }
        // Output FPS on Screen
        shapeRenderer.setColor(0, 0, 0, 0.75f);
        shapeRenderer.rect(1820, 980, 100, 100);
        shapeRenderer.end();
        if (Node.EndNode != null) {
            totalFrames++;
            totalTime += delta;
        }
        float fps = totalFrames/totalTime;
        batch.begin();
        String hoveredStats = "" + fps;
        if (hoveredI != -1){
            Node node = grid[hoveredI][hoveredJ];
            hoveredStats += "\nF: " + node.fCost + "\nG: " + node.gCost + "\nH: " + node.hCost;
        }
        font.draw(batch, hoveredStats,  1850, 1070);
        batch.end();
    }
    void drawRect(int x, int y, int width, int height) {
        shapeRenderer.rect(x + offSetX, y + offSetY, width, height);
    }
    float totalTime = 0;
    int totalFrames = 0;

    @Override
    public void show() {

    }
    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
