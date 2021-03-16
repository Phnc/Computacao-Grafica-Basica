package beans;

import operations.Operations;

public class Triangle {
	public Point v1;
	public Point v2;
	public Point v3;
	public Vector normal;
	public Point p1Original;
	public Point p2Original;
	public Point p3Original;
	public Point c1;
	public Point c2;
	public Point c3;
	public Point view1;
	public Point view2;
	public Point view3;
	public Point barycenter;

	public Triangle() {
		normal = new Vector();
	}

	public Triangle(Point p1, Point p2, Point p3) {
		v1 = p1;
		v2 = p2;
		v3 = p3;
	}

	public void calculateBarycenter() {
		barycenter = new Point();
		barycenter.x = (view1.x + view2.x + view3.x) / 3;
		barycenter.y = (view1.y + view2.y + view3.y) / 3;
		barycenter.z = (view1.z + view2.z + view3.z) / 3;
	}

	public Triangle copy() {
		Triangle t = new Triangle();
		t.v1 = v1.copy();
		t.v2 = v2.copy();
		t.v3 = v3.copy();
		if (normal != null)
			t.normal = normal;
		if (p1Original != null && p2Original != null && p3Original != null) {
			t.p1Original = p1Original.copy();
			t.p2Original = p2Original.copy();
			t.p3Original = p3Original.copy();
		}

		return t;
	}

	public void normalTriangle() {
		Vector temp = Operations.subtractPoints(view2, view1);
		Vector temp1 = Operations.subtractPoints(view3, view1);
		normal = Operations.vectorialProduct(temp, temp1);
		normal.x = -normal.x;
		normal.y = -normal.y;
		normal.z = -normal.z;
		normal = Operations.normalize(normal);
	}

}
