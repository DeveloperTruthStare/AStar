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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainScreen implements Screen{
    Thread mainThread;
    String displayText = "";
    public InputProcessor inputProcessor = new InputProcessor() {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.SPACE) {
                currentSide++;
                if(currentSide >= ships.size())
                    currentSide = 0;
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
                Ship ship = ships.get(currentSide);
                if (i > ship.posX && i < ship.posX + ship.SHIP_WIDTH && j > ship.posY && j < ship.posY + ship.SHIP_HEIGHT) {
                    if (Node.StartNode != null) {
                        Node.StartNode.start = false;
                    }
                    ship.grid[i][j].start = true;
                    Node.StartNode = ship.grid[i][j];
                }
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            int i = (screenX - offSetX) / (CELL_WIDTH+CELL_X_SPACING);
            int j = (1080 - screenY - offSetY) / (CELL_HEIGHT+CELL_Y_SPACING);
            if (i < 0 || i > WIDTH-1 || j < 0 || j > HEIGHT-1) return false;

            if (button == 0) {
                Ship ship = ships.get(currentSide);
                if (i > ship.posX && i < ship.posX + ship.SHIP_WIDTH && j > ship.posY && j < ship.posY + ship.SHIP_HEIGHT) {
                    if (Node.EndNode != null) {
                        Node.EndNode.end = false;
                    }
                    ship.grid[i][j].end = true;
                    Node.EndNode = ship.grid[i][j];
                    optimalPath = ship.findPath(new Point(Node.StartNode.i, Node.StartNode.j), new Point(i, j));
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
    int CELL_WIDTH = 50;
    int CELL_HEIGHT = 50;
    int CELL_X_SPACING = 1;
    int CELL_Y_SPACING = 1;
    int PATH_WIDTH = 10;
    int PATH_HEIGHT = 10;
    Node[][] grid;
    CustomQueue queue;
    ShapeRenderer shapeRenderer;
    int offSetX = 0, offSetY = 0;
    BitmapFont font;
    int hoveredI = -1, hoveredJ = -1;
    Random rand;
    ArrayList<Point2D> optimalPath;
    ArrayList<Ship> ships = new ArrayList<>();
    public MainScreen() {
        font = new BitmapFont();
        batch = new SpriteBatch();
        Gdx.input.setInputProcessor(inputProcessor);
        batch = new SpriteBatch();
        queue = new CustomQueue();
        shapeRenderer = new ShapeRenderer();
        rand = new Random();

        grid = new Node[WIDTH][HEIGHT];
        for(int i = 0; i < WIDTH; ++i) {
            for(int j = 0; j < HEIGHT; ++j) {
                grid[i][j] = new Node();
                grid[i][j].i = i;
                grid[i][j].j = j;
                //initializeNode(i, j);
            }
        }
        try {
            long startTime = System.nanoTime();
            int paths = 0;
            for(String fileName : listFilesUsingJavaIO("ships/")) {
                ships.add(new Ship("ships/" + fileName, 1, 0, 0));
            }
            for(Ship ship : ships)
                paths += ship.paths.size();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            System.out.println("Found all " + paths + " ships in " + duration);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public Set<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
    }
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < Math.min(64, WIDTH); ++i) {
            for (int j = 0; j < Math.min(36, HEIGHT); ++j) {
                // Draw Node
                shapeRenderer.setColor(0, 0, 0, 0.25f);
                drawRect(i * (CELL_WIDTH + CELL_X_SPACING), j * (CELL_HEIGHT + CELL_Y_SPACING), CELL_WIDTH, CELL_HEIGHT);
            }
        }

        ships.get(currentSide).draw(shapeRenderer);
        shapeRenderer.setColor(0, 0, 0, 1);
        if (null != optimalPath){
            for (Point2D point : optimalPath) {
                shapeRenderer.rect((float) point.getX() * (CELL_WIDTH + CELL_X_SPACING) + (CELL_WIDTH/2 - CELL_WIDTH / 8), (float) point.getY() * (CELL_HEIGHT + CELL_Y_SPACING) + (CELL_HEIGHT/2 - CELL_HEIGHT / 8), CELL_WIDTH / 4, CELL_HEIGHT / 4);
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
