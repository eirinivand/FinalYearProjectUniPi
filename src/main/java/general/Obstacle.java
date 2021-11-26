package general;

public class Obstacle extends State {

	private int x;
	private int y;

	public Obstacle(int x, int y) {
		super(x, y);
		this.x = x;
		this.y = y;
	}

	// x
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	// y
	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

}
