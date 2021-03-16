package beans;

public class Light {
	public static Vector Iamb;
	public static double Ka;
	public static Vector Il;
	public static Point Pl;
	public static Vector Kd;
	public static Vector Od;
	public static double Ks;
	public static double Eta;
	
	public Light() {
		Iamb = new Vector();
		Il = new Vector();
		Pl = new Point();
		Kd = new Vector();
		Od = new Vector();
	}
	
	public Light(Vector iamb, double ka, Vector il, Point pl, Vector kd, Vector od, double ks, double eta) {
		Iamb = iamb;
		Ka = ka;
		Il = il;
		Pl = pl;
		Kd = kd;
		Od = od;
		Ks = ks;
		Eta = eta;
	}
	
	public static Light getLight() {
		return new Light(Iamb, Ka, Il, Pl, Kd, Od, Ks, Eta);
	}
}
