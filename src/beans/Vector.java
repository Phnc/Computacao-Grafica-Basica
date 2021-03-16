package beans;

public class Vector {
	public double x;
	public double y;
	public double z;

	public Vector() {
		super();
	}

	public Vector(double x, double y, double z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getNorm() {
		double norm = Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);
		norm = Math.sqrt(norm);

		return norm;
	}

	public Vector multiplyByScalar(double scalar) {
		Vector v = new Vector(x, y, z);
		v.x = v.x * scalar;
		v.y = v.y * scalar;
		v.z = v.z * scalar;

		return v;
	}

}
