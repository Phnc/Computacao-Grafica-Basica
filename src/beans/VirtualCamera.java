package beans;

public class VirtualCamera {
	public static Point C;
	public static Vector N;
	public static Vector V;
	public static double D;
	public static double Hx;
	public static double Hy;

	public static VirtualCamera getCamera() {
		return new VirtualCamera(C, N, V, D, Hx, Hy);
	}

	public VirtualCamera() {
		C = new Point();
		N = new Vector();
		V = new Vector();
	}

	public VirtualCamera(Point c, Vector n, Vector v, double d, double hx, double hy) {
		C = c;
		N = n;
		V = v;
		D = d;
		Hx = hx;
		Hy = hy;
	}

}
