package beans;

import java.util.ArrayList;

public class Point {
	public double x;
	public double y;
	public double z;
	public ArrayList<Integer> triangles = new ArrayList<>();
	public Vector normal;

	public Point() {

	}

	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		triangles = new ArrayList<>();
	}

	public Point copy() {
		Point v = new Point();
		v.x = x;
		v.y = y;
		v.z = z;
		v.normal = normal;
		v.triangles = triangles;

		return v;
	}

}
