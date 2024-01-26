package com.smith.astar;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.Shape;
import java.util.ArrayList;

public class MainClass extends Game {
	public InputProcessor inputProcessor = new InputProcessor() {
		@Override
		public boolean keyDown(int keycode) {
			if (keycode == Input.Keys.SPACE) {
				currentSide++;
				if(currentSide > 3)
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
			int i = (screenX - offSetX) / 55;
			int j = (1080 - screenY - offSetY) / 55;
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
			int i = (screenX - offSetX) / 55;
			int j = (1080 - screenY - offSetY) / 55;
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
			return false;
		}

		@Override
		public boolean scrolled(float amountX, float amountY) {
			return false;
		}
	};
	public int currentSide = 0;

	SpriteBatch batch;
	int WIDTH = 32;
	int HEIGHT = 18;
	Node[][] grid;
	CustomQueue queue;
	ShapeRenderer shapeRenderer;
	int offSetX = 0, offSetY = 0;
	ArrayList<Node> closed;
	@Override
	public void create () {
		Gdx.input.setInputProcessor(inputProcessor);
		closed = new ArrayList<>();
		batch = new SpriteBatch();
		queue = new CustomQueue();
		shapeRenderer = new ShapeRenderer();

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
			}
		}
		// Randomly assign up to two of the walls to be blocked
	}
	public void checkNode(Node nextNode, Node currentNode) {
		if (closed.contains(nextNode)) return;
		if (queue.contains(nextNode)) {
			// Check if this is a better path
			nextNode.calculateCosts();
			if (currentNode.gCost + 1 <= nextNode.gCost) {
				nextNode.parent = currentNode;
			}
		} else {
			// Add it
			queue.addToQueue(nextNode);
			nextNode.parent = currentNode;
		}
	}
	int frames = 0;
	@Override
	public void render () {
		ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		// Find optimal path
		queue.clear();
		closed.clear();
		if (!(Node.StartNode == null || Node.EndNode == null))
		{
			int nodesChecked = 0;

			for(int count = 0; count < 3000; ++count) {
				queue.addToQueue(Node.StartNode);
				while (!queue.isEmpty()) {
					nodesChecked++;
					Node currentNode = queue.getBestNode();
					if (currentNode.end) {
						// We're done
						queue.clear();
					} else {
						currentNode.calculateCosts();
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
						closed.add(currentNode);
					}
				}
				for (int i = 0; i < WIDTH; ++i)
					for (int j = 0; j < HEIGHT; ++j)
						grid[i][j].isOnPath = false;
				Node node = Node.EndNode;
				while (node != Node.StartNode) {
					node.isOnPath = true;
					node = node.parent;
				}
			}
			System.out.println(nodesChecked);
		}



		for(int i = 0; i < WIDTH; ++i) {
			for(int j = 0; j < HEIGHT; ++j) {
				// Draw Node
				shapeRenderer.setColor(1, 1, 1, 1);
				if (grid[i][j].start)
					shapeRenderer.setColor(0, 1, 0, 1);
				else if (grid[i][j].end)
					shapeRenderer.setColor(0, 0, 1, 1);
				drawRect(i * 55, j * 55, 50, 50);

				shapeRenderer.setColor(1, 0, 0, 1);
				if (grid[i][j].left) {
					drawRect(i*55, j*55, 5, 50);
				}
				if (grid[i][j].right) {
					drawRect((i+1)*55 - 10, j*55, 5, 50);
				}
				if (grid[i][j].top) {
					drawRect(i*55, (j+1)*55-10, 50, 5);
				}
				if (grid[i][j].bottom) {
					drawRect(i*55, j*55, 50, 5);
				}
				if (grid[i][j].isOnPath) {
					shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1);
					drawRect(i*55 + 20, j*55 + 20, 10, 10);
				}
			}
		}
		shapeRenderer.end();
	}
	void drawRect(int x, int y, int width, int height) {
		shapeRenderer.rect(x + offSetX, y + offSetY, width, height);
	}
	@Override
	public void dispose () {
		batch.dispose();
	}
}
