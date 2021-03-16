package operations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import application.MainScreenController;
import beans.Light;
import beans.Matrix;
import beans.Point;
import beans.Triangle;
import beans.Vector;
import beans.VirtualCamera;
import beans.Z;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Operations {

	public static Point[] points;
	public static Triangle[] triangles;
	public static GraphicsContext graphicsContext;
	public static Z[][] zBuffer;

	public static Vector ambientLight() {
		Vector Ia = new Vector();
		Ia.x = Light.Iamb.x;
		Ia.y = Light.Iamb.y;
		Ia.z = Light.Iamb.z;

		return Ia.multiplyByScalar(Light.Ka);

	}

	public static Point barycentricCartesianCoordinate(double[] barycentricCoordinate, Point a, Point b, Point c) {
		Point cartesian = new Point();

		cartesian.x = a.x * barycentricCoordinate[0] + b.x * barycentricCoordinate[1] + c.x * barycentricCoordinate[2];
		cartesian.y = a.y * barycentricCoordinate[0] + b.y * barycentricCoordinate[1] + c.y * barycentricCoordinate[2];
		cartesian.z = a.z * barycentricCoordinate[0] + b.z * barycentricCoordinate[1] + c.z * barycentricCoordinate[2];

		return cartesian;
	}

	public static double[] barycentricCoordinate(Point p, Point a, Point b, Point c) {
		Matrix m1 = new Matrix(2, 2);

		m1.setIJ(0, 0, a.x - c.x);
		m1.setIJ(0, 1, b.x - c.x);
		m1.setIJ(1, 0, a.y - c.y);
		m1.setIJ(1, 1, b.y - c.y);

		Matrix m2 = new Matrix(2, 1);

		m2.setIJ(0, 0, p.x - c.x);
		m2.setIJ(1, 0, p.y - c.y);

		Matrix alfaBeta = multiply(new Matrix(m1.getInverse()), m2);

		double alpha = alfaBeta.getIJ(0, 0);
		double beta = alfaBeta.getIJ(1, 0);
		double gama = 1 - alpha - beta;

		double[] res = new double[3];
		res[0] = alpha;
		res[1] = beta;
		res[2] = gama;

		return res;
	}

	public static void calculateAll(int width, int height, String s, Matrix m) throws Exception {
		Operations.loadTrianglePoints(s);
		Operations.loadCameraParameters();

		Operations.orthogonalizeV(VirtualCamera.V, VirtualCamera.N);
		Vector V = Operations.normalize(VirtualCamera.V);
		Vector N = Operations.normalize(VirtualCamera.N);
		Vector U = Operations.getU(N, V);

		for (int i = 0; i < triangles.length; i++) {
			triangles[i].view1 = Operations.getViewCoordinates(U, V, N, triangles[i].v1);
			triangles[i].view2 = Operations.getViewCoordinates(U, V, N, triangles[i].v2);
			triangles[i].view3 = Operations.getViewCoordinates(U, V, N, triangles[i].v3);
			triangles[i].normalTriangle();
			triangles[i].calculateBarycenter();

		}

		Point c = imageCenter();
		Matrix retornaTrans = translationMatrix(c.x, c.y, c.z);
		Matrix trans = translationMatrix(-c.x, -c.y, -c.z);
		Matrix T = multiply(retornaTrans, m);
		T = multiply(T, trans);

		for (int i = 0; i < triangles.length; i++) {
			triangles[i].view1 = Operations.rotate(triangles[i].view1, T);
			triangles[i].view2 = Operations.rotate(triangles[i].view2, T);
			triangles[i].view3 = Operations.rotate(triangles[i].view3, T);
			triangles[i].normalTriangle();
			triangles[i].calculateBarycenter();

		}

		calculateNormalVertices();

		Operations.quickSort(triangles, 0, triangles.length - 1, 1);

	}

	public synchronized static void calculateColor(int x, int y, Triangle t) {
		if (!(x > MainScreenController.width - 1 || x < 0 || y > MainScreenController.height - 1 || y < 0)) {
			double[] coord = Operations.barycentricCoordinate(new Point(x, y, 0), t.c1, t.c2, t.c3);

			Point p = pOriginal(coord, t);
			if (p.z < zBuffer[x][y].z) {
				Vector N = calculateNormalByBarycentric(t, coord);
				Vector V = Operations.findV(p);
				Vector L = Operations.findL(p);
				Vector R = Operations.findR(N, L);
				Vector color = Operations.phongLight(N, V, L, R);
				double r = Math.floor(color.x + 0.5);
				double g = Math.floor(color.y + 0.5);
				double b = Math.floor(color.z + 0.5);
				if (r > 255)
					r = 255;
				if (r < 0)
					r = 0;
				if (g > 255)
					g = 255;
				if (g < 0)
					g = 0;
				if (b > 255)
					b = 255;
				if (b < 0)
					b = 0;
				zBuffer[x][y].c = Color.rgb((int) r, (int) g, (int) b);
				zBuffer[x][y].z = p.z;
			}
		}
	}

	public static Vector calculateNormalByBarycentric(Triangle t, double[] coord) {
		Vector n1 = new Vector();
		n1 = t.p1Original.normal;
		n1 = n1.multiplyByScalar(coord[0]);

		Vector n2 = new Vector();
		n2 = t.p2Original.normal;
		n2 = n2.multiplyByScalar(coord[1]);

		Vector n3 = new Vector();
		n3 = t.p3Original.normal;
		n3 = n3.multiplyByScalar(coord[2]);

		Vector temp = Operations.sumVectors(n1, n2);
		temp = Operations.sumVectors(temp, n3);
		temp = Operations.normalize(temp);

		return temp;

	}

	public static Vector calculateNormalVertice(Point v) {
		Vector normal = new Vector();
		ArrayList<Triangle> l = returnTriangles(v, triangles);
		for (int i = 0; i < l.size(); i++) {
			normal = Operations.sumVectors(normal, l.get(i).normal);
		}

		normal = Operations.normalize(normal);
		return normal;

	}

	public static void calculateNormalVertices() {
		for (int i = 0; i < points.length; i++) {
			points[i].normal = calculateNormalVertice(points[i]);
		}

	}

	public static Vector difuseReflection(Vector N, Vector V, Vector L) {
		double cosTeta = Operations.scalarProduct(N, L);
		Vector temp = new Vector();
		temp.x = Light.Kd.x;
		temp.y = Light.Kd.y;
		temp.z = Light.Kd.z;

		Vector Id = temp.multiplyByScalar(cosTeta);
		Id = multiplyComponentComponent(Id, Light.Od);
		Id = multiplyComponentComponent(Id, Light.Il);

		return Id;
	}

	private static void fillInferiorTriangle(Triangle t, Triangle maior) {
		double a1, a2;

		if (t.v1.x < t.v2.x) {
			a1 = (t.v3.x - t.v1.x) / (t.v3.y - t.v1.y);
			a2 = (t.v3.x - t.v2.x) / (t.v3.y - t.v2.y);
		} else {
			a1 = (t.v3.x - t.v2.x) / (t.v3.y - t.v2.y);
			a2 = (t.v3.x - t.v1.x) / (t.v3.y - t.v1.y);
		}

		double xMin = t.v3.x;
		double xMax = t.v3.x;

		for (int yScan = (int) t.v3.y; yScan >= t.v1.y; yScan--) {
			for (int x = (int) xMin; x <= (int) xMax; x++) {
				// paint(x, yScan, Color.WHITE);
				calculateColor(x, yScan, maior);

			}
			xMin -= a1;
			xMax -= a2;
		}

	}

	private static void fillSuperiorTriangle(Triangle t, Triangle maior) {
		double a1, a2;
		if (t.v2.x < t.v3.x) {
			a1 = (t.v2.x - t.v1.x) / (t.v2.y - t.v1.y);
			a2 = (t.v3.x - t.v1.x) / (t.v3.y - t.v1.y);
		} else {
			a1 = (t.v3.x - t.v1.x) / (t.v3.y - t.v1.y);
			a2 = (t.v2.x - t.v1.x) / (t.v2.y - t.v1.y);
		}

		double xMin = t.v1.x;
		double xMax = t.v1.x;

		for (int yScan = (int) t.v1.y; yScan <= t.v2.y; yScan++) {
			for (int x = (int) xMin; x <= (int) xMax; x++) {
				// paint(x, yScan, Color.WHITE);
				calculateColor(x, yScan, maior);
			}
			xMin += a1;
			xMax += a2;
		}
	}

	public static Vector findL(Point p) {
		return Operations.normalize(Operations.subtractPoints(Light.Pl, p));

	}

	public static Vector findR(Vector N, Vector L) {
		double k = 2 * Operations.scalarProduct(N, L);
		Vector temp = new Vector();
		temp.x = N.x;
		temp.y = N.y;
		temp.z = N.z;
		temp = temp.multiplyByScalar(k);

		Vector R = new Vector();
		R.x = temp.x - L.x;
		R.y = temp.y - L.y;
		R.z = temp.z - L.z;

		return R;
	}

	public static Vector findV(Point p) {
		return Operations.normalize(subtractPoints(new Point(0, 0, 0), p));

	}

	public static Point getNormalizedCoordinates(Point p) {

		p.x = p.x / VirtualCamera.Hx;
		p.y = p.y / VirtualCamera.Hy;

		return p;
	}

	public static Point getPerspectiveProjection(Point p) {
		Point f = new Point();

		f.x = VirtualCamera.D * (p.x / p.z);
		f.y = VirtualCamera.D * (p.y / p.z);

		return f;
	}

	public static Point getScreenCoordinates(double resX, double resY, Point p) {
		double temp1 = Math.floor(((p.x + 1) / 2) * resX + 0.5);

		double temp2 = Math.floor(resY - (((p.y + 1) / 2) * resY + 0.5));

		p.x = temp1;
		p.y = temp2;

		return p;
	}

	public static Vector getU(Vector N, Vector V) {
		Vector U = null;

		if (N != null && V != null) {
			U = Operations.vectorialProduct(N, V);
		}

		return U;
	}

	public static Point getViewCoordinates(Vector U, Vector V, Vector N, Point m) {

		Matrix A = new Matrix(3, 3);
		A.setIJ(0, 0, U.x);
		A.setIJ(0, 1, U.y);
		A.setIJ(0, 2, U.z);
		A.setIJ(1, 0, V.x);
		A.setIJ(1, 1, V.y);
		A.setIJ(1, 2, V.z);
		A.setIJ(2, 0, N.x);
		A.setIJ(2, 1, N.y);
		A.setIJ(2, 2, N.z);

		Vector temp = Operations.subtractPoints(m, VirtualCamera.C);

		Matrix B = new Matrix(3, 1);

		B.setIJ(0, 0, temp.x);
		B.setIJ(1, 0, temp.y);
		B.setIJ(2, 0, temp.z);

		Matrix v = Operations.multiply(A, B);

		Point f = new Point();

		f.x = v.getIJ(0, 0);
		f.y = v.getIJ(1, 0);
		f.z = v.getIJ(2, 0);

		return f;
	}

	public static Z[][] getZBuffer() {
		return zBuffer;
	}

	public static Matrix identityMatrix() {
		Matrix m = new Matrix(4, 4);
		m.setIJ(0, 0, 1);
		m.setIJ(0, 1, 0);
		m.setIJ(0, 2, 0);
		m.setIJ(0, 3, 0);

		m.setIJ(1, 0, 0);
		m.setIJ(1, 1, 1);
		m.setIJ(1, 2, 0);
		m.setIJ(1, 3, 0);

		m.setIJ(2, 0, 0);
		m.setIJ(2, 1, 0);
		m.setIJ(2, 2, 1);
		m.setIJ(2, 3, 0);

		m.setIJ(3, 0, 0);
		m.setIJ(3, 1, 0);
		m.setIJ(3, 2, 0);
		m.setIJ(3, 3, 1);

		return m;

	}

	public static Point imageCenter() {
		Point temp = new Point(0, 0, 0);
		for (int i = 0; i < triangles.length; i++) {
			temp = sumPoints(temp, triangles[i].barycenter);
		}

		temp.x = temp.x / triangles.length;
		temp.y = temp.y / triangles.length;
		temp.z = temp.z / triangles.length;

		return temp;

	}

	public static Z[][] initializeZBuffer(int width, int height) {
		if (zBuffer == null)
			zBuffer = new Z[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				zBuffer[i][j] = new Z();
				zBuffer[i][j].z = Double.POSITIVE_INFINITY;
				zBuffer[i][j].c = Color.BLACK;
			}
		}

		return zBuffer;

	}

	public static void lightPaint(int width, int height, String s, Matrix m) throws Exception {
		Operations.loadTrianglePoints(s);
		Operations.loadCameraParameters();
		Operations.loadLightParameters();

		Operations.orthogonalizeV(VirtualCamera.V, VirtualCamera.N);
		Vector V = Operations.normalize(VirtualCamera.V);
		Vector N = Operations.normalize(VirtualCamera.N);
		Vector U = Operations.getU(N, V);

		for (int i = 0; i < triangles.length; i++) {
			triangles[i].view1 = Operations.getViewCoordinates(U, V, N, triangles[i].v1);
			triangles[i].view2 = Operations.getViewCoordinates(U, V, N, triangles[i].v2);
			triangles[i].view3 = Operations.getViewCoordinates(U, V, N, triangles[i].v3);
			triangles[i].normalTriangle();
			triangles[i].calculateBarycenter();

		}

		Point c = imageCenter();
		Matrix retornaTrans = translationMatrix(c.x, c.y, c.z);
		Matrix trans = translationMatrix(-c.x, -c.y, -c.z);
		Matrix T = multiply(retornaTrans, m);
		T = multiply(T, trans);

		for (int i = 0; i < triangles.length; i++) {
			triangles[i].view1 = Operations.rotate(triangles[i].view1, T);
			triangles[i].view2 = Operations.rotate(triangles[i].view2, T);
			triangles[i].view3 = Operations.rotate(triangles[i].view3, T);
			triangles[i].normalTriangle();
			triangles[i].calculateBarycenter();

		}

		calculateNormalVertices();

		Operations.quickSort(triangles, 0, triangles.length - 1, 1);

		initializeZBuffer(width, height);

		for (int i = 0; i < triangles.length; i++) {
			lightScreenCoordinates(triangles[i], width, height);
			Operations.scanLine(triangles[i], 1);
		}
		paintZBuffer();
	}

	public static void lightScreenCoordinates(Triangle t, int width, int height) {

		t.v1 = Operations.getPerspectiveProjection(t.view1);
		t.v2 = Operations.getPerspectiveProjection(t.view2);
		t.v3 = Operations.getPerspectiveProjection(t.view3);

		t.v1 = Operations.getNormalizedCoordinates(t.v1);
		t.v2 = Operations.getNormalizedCoordinates(t.v2);
		t.v3 = Operations.getNormalizedCoordinates(t.v3);

		t.v1 = Operations.getScreenCoordinates(width, height, t.v1);
		t.v2 = Operations.getScreenCoordinates(width, height, t.v2);
		t.v3 = Operations.getScreenCoordinates(width, height, t.v3);
	}

	public static void loadCameraParameters() throws IOException {
		String[] b = new String[6];
		b[0] = "N = ";
		b[1] = "V = ";
		b[2] = "d = ";
		b[3] = "hx = ";
		b[4] = "hy = ";
		b[5] = "C = ";

		BufferedReader r = new BufferedReader(new FileReader("CameraParameters.txt"));
		Double[] v = new Double[12];
		int k = 0;
		for (int i = 0; i < 6; i++) {
			String linha = r.readLine();
			String x[] = linha.split(b[i]);
			x = x[1].split(" ");
			for (int j = 0; j < 3; j++) {
				v[k] = Double.parseDouble(x[j]);
				k++;
				if (i == 2 || i == 3 || i == 4)
					break;
			}

		}
		VirtualCamera.N = new Vector(v[0], v[1], v[2]);
		VirtualCamera.V = new Vector(v[3], v[4], v[5]);
		VirtualCamera.D = v[6];
		VirtualCamera.Hx = v[7];
		VirtualCamera.Hy = v[8];

		VirtualCamera.C = new Point(v[9], v[10], v[11]);

		r.close();
	}

	public static void loadLightParameters() throws IOException {
		String[] b = new String[8];
		b[0] = "Iamb = ";
		b[1] = "Ka = ";
		b[2] = "Il = ";
		b[3] = "Pl = ";
		b[4] = "Kd = ";
		b[5] = "Od = ";
		b[6] = "Ks = ";
		b[7] = "Eta = ";

		BufferedReader r = new BufferedReader(new FileReader("Light.txt"));

		Double[] v = new Double[18];
		int k = 0;

		for (int i = 0; i < 8; i++) {
			String linha = r.readLine();
			String x[] = linha.split(b[i]);
			x = x[1].split(" ");
			for (int j = 0; j < 3; j++) {
				v[k] = Double.parseDouble(x[j]);
				k++;
				if (i == 1 || i == 6 || i == 7)
					break;
			}

		}

		Light.Iamb = new Vector(v[0], v[1], v[2]);
		Light.Ka = v[3];
		Light.Il = new Vector(v[4], v[5], v[6]);
		Light.Pl = new Point(v[7], v[8], v[9]);
		Light.Kd = new Vector(v[10], v[11], v[12]);
		Light.Od = new Vector(v[13], v[14], v[15]);
		Light.Ks = v[16];
		Light.Eta = v[17];

		r.close();
	}

	public static void loadTrianglePoints(String s) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(s));

		String line = reader.readLine();

		Point[] p;

		Triangle[] t;

		String str[] = line.split(" ");

		p = new Point[Integer.parseInt(str[0])];

		t = new Triangle[Integer.parseInt(str[1])];

		for (int i = 0; i < p.length; i++) {
			line = reader.readLine();
			str = line.split(" ");
			p[i] = new Point();
			p[i].x = Double.parseDouble(str[0]);
			p[i].y = Double.parseDouble(str[1]);
			p[i].z = Double.parseDouble(str[2]);

		}

		for (int i = 0; i < t.length; i++) {
			line = reader.readLine();
			str = line.split(" ");
			t[i] = new Triangle(p[Integer.parseInt(str[0]) - 1], p[Integer.parseInt(str[1]) - 1],
					p[Integer.parseInt(str[2]) - 1]);

			t[i].p1Original = p[Integer.parseInt(str[0]) - 1];
			t[i].p2Original = p[Integer.parseInt(str[1]) - 1];
			t[i].p3Original = p[Integer.parseInt(str[2]) - 1];
		}

		reader.close();
		points = p;
		triangles = t;
	}

	private static double mul(double[] linha, double[] coluna) {
		double ret = 0;

		for (int i = 0; i < linha.length; i++) {
			ret += linha[i] * coluna[i];
		}

		return ret;
	}

	public static Matrix multiply(Matrix A, Matrix B) {
		Matrix c = new Matrix(A.getLineCount(), B.getColCount());

		int k = 0;
		int l = 0;

		for (int i = 0; i < c.getLineCount(); i++) {
			for (int j = 0; j < c.getColCount(); j++) {
				c.setIJ(i, j, mul(A.getMatrix()[k], B.getTransposed()[l]));
				l++;
			}
			k++;

			l = 0;
		}

		return c;
	}

	private static Vector multiplyComponentComponent(Vector A, Vector B) {
		Vector c = new Vector();
		c.x = A.x * B.x;
		c.y = A.y * B.y;
		c.z = A.z * B.z;

		return c;
	}

	public static Vector normalize(Vector a) {
		Vector n = new Vector();

		n.x = a.x / a.getNorm();
		n.y = a.y / a.getNorm();
		n.z = a.z / a.getNorm();

		return n;
	}

	public static void orthogonalizeV(Vector V, Vector N) {
		Vector N1 = new Vector();

		N1.x = N.x;
		N1.y = N.y;
		N1.z = N.z;

		double a = Operations.scalarProduct(V, N1) / Operations.scalarProduct(N1, N1);

		Vector temp = N1.multiplyByScalar(a);

		V.x = V.x - temp.x;
		V.y = V.y - temp.y;
		V.z = V.z - temp.z;

	}

	public static void paint(int x, int y, Color c) {
		graphicsContext.setFill(c);
		graphicsContext.fillRect(x - 1, y, 1, 1);
	}

	public static void paintZBuffer() {
		for (int i = 0; i < Operations.getZBuffer().length; i++) {
			for (int j = 0; j < Operations.getZBuffer().length; j++) {
				paint(i, j, Operations.getZBuffer()[i][j].c);
			}
		}

	}

	private static int partition1(Object[] a, int p, int r) {
		Point[] A;

		if (a instanceof Point[]) {
			A = (Point[]) a;
			int i = p - 1;
			double y = A[r].y;
			int j;
			for (j = p; j <= r - 1; j++) {
				if (A[j].y <= y) {
					i++;
					Point temp;
					temp = A[i];
					A[i] = A[j];
					A[j] = temp;
				}
			}
			Point temp;
			temp = A[i + 1];
			A[i + 1] = A[r];
			A[r] = temp;

			return i + 1;
		}

		return -1;
	}

	private static int partition2(Object[] a, int p, int r) {
		Triangle[] A;

		if (a instanceof Triangle[]) {
			A = (Triangle[]) a;
			int i = p - 1;
			double z = A[r].barycenter.z;
			int j;
			for (j = p; j <= r - 1; j++) {
				if (A[j].barycenter.z >= z) {
					i++;
					Triangle temp;
					temp = A[i];
					A[i] = A[j];
					A[j] = temp;
				}
			}
			Triangle temp;
			temp = A[i + 1];
			A[i + 1] = A[r];
			A[r] = temp;

			return i + 1;
		}

		return -1;
	}

	public static Vector phongLight(Vector N, Vector V, Vector L, Vector R) {
		Vector Ia = ambientLight();
		Vector Id = null;
		Vector Is = null;

		if (Operations.scalarProduct(N, L) < 0) {
			if (Operations.scalarProduct(V, N) < 0) {
				N.x = -N.x;
				N.y = -N.y;
				N.z = -N.z;
			} else {
				Id = new Vector(0, 0, 0);
				Is = new Vector(0, 0, 0);
			}
		}

		if (Operations.scalarProduct(V, R) < 0) {
			Is = new Vector(0, 0, 0);
		}

		if (Id == null) {
			Id = difuseReflection(N, V, L);
		}
		if (Is == null) {
			Is = specularReflection(R, V);
		}

		Vector I = Operations.sumVectors(Ia, Id);
		I = Operations.sumVectors(I, Is);

		return I;
	}

	public static Point pOriginal(double[] coord, Triangle t) {

		Point r = Operations.barycentricCartesianCoordinate(coord, t.view1, t.view2, t.view3);
		return r;
	}

	public static void quickSort(Object[] A, int p, int r, int part) {
		int q = -1;
		if (p < r) {
			if (part == 0)
				q = partition1(A, p, r);
			if (part == 1)
				q = partition2(A, p, r);
			quickSort(A, p, q - 1, part);
			quickSort(A, q + 1, r, part);
		}

	}

	public static ArrayList<Triangle> returnTriangles(Point p, Triangle[] t) {
		ArrayList<Triangle> lista = new ArrayList<>();
		for (int i = 0; i < t.length; i++) {
			if (t[i].p1Original == p || t[i].p2Original == p || t[i].p3Original == p)
				lista.add(t[i]);
		}

		return lista;
	}

	public static Point rotate(Point p, Matrix T) {
		Matrix k = new Matrix(4, 1);
		k.setIJ(0, 0, p.x);
		k.setIJ(1, 0, p.y);
		k.setIJ(2, 0, p.z);
		k.setIJ(3, 0, 1);

		Matrix t = multiply(T, k);
		Point v = new Point();
		v.x = t.getIJ(0, 0);
		v.y = t.getIJ(1, 0);
		v.z = t.getIJ(2, 0);

		return v;
	}

	public static double scalarProduct(Vector a, Vector b) {
		double ret = a.x * b.x + a.y * b.y + a.z * b.z;

		return ret;
	}

	public static void scanLine(Triangle t, int k) {
		t.c1 = t.v1;
		t.c2 = t.v2;
		t.c3 = t.v3;

		Point[] vs = new Point[] { t.v1, t.v2, t.v3 };

		quickSort(vs, 0, 2, 0);

		t.v1 = vs[0];
		t.v2 = vs[1];
		t.v3 = vs[2];

		if (t.v2.y == t.v3.y) {
			fillSuperiorTriangle(t, t);
		} else if (t.v1.y == t.v2.y) {
			fillInferiorTriangle(t, t);
		} else {
			Point p = new Point();
			p.x = Math.floor((t.v1.x + ((t.v2.y - t.v1.y) / (t.v3.y - t.v1.y)) * (t.v3.x - t.v1.x) + 0.5));
			p.y = t.v2.y;

			Triangle first, second;
			first = t.copy();
			first.v3 = p;

			second = t.copy();
			second.v1 = t.v2;
			second.v2 = p;
			second.v3 = t.v3;

			fillSuperiorTriangle(first, t);
			fillInferiorTriangle(second, t);

		}
	}

	public static Canvas scanLinePaint(GraphicsContext gc, int width, int height, String s) throws Exception {
		Operations.loadTrianglePoints(s);
		Operations.loadCameraParameters();

		Operations.orthogonalizeV(VirtualCamera.V, VirtualCamera.N);
		Vector V = Operations.normalize(VirtualCamera.V);
		Vector N = Operations.normalize(VirtualCamera.N);
		Vector U = Operations.getU(N, V);

		Triangle[] pTela = new Triangle[triangles.length];

		for (int i = 0; i < pTela.length; i++) {
			pTela[i] = new Triangle();
			pTela[i].v1 = getViewCoordinates(U, V, N, triangles[i].v1);
			pTela[i].v2 = getViewCoordinates(U, V, N, triangles[i].v2);
			pTela[i].v3 = getViewCoordinates(U, V, N, triangles[i].v3);

			whiteScreenCoordinates(pTela[i], width, height);

			scanLine(pTela[i], 0);

		}
		return gc.getCanvas();
	}

	public static Vector specularReflection(Vector R, Vector V) {
		double RV = Math.pow(Operations.scalarProduct(R, V), Light.Eta) * Light.Ks;
		Vector temp = new Vector();
		temp.x = Light.Il.x;
		temp.y = Light.Il.y;
		temp.z = Light.Il.z;

		Vector Is = temp.multiplyByScalar(RV);

		return Is;
	}

	public static Vector subtractPoints(Point a, Point b) {
		Vector c = new Vector();

		c.x = a.x - b.x;
		c.y = a.y - b.y;
		c.z = a.z - b.z;

		return c;
	}

	public static Point sumPoints(Point A, Point B) {
		Point temp = new Point();
		temp.x = A.x + B.x;
		temp.y = A.y + B.y;
		temp.z = A.z + B.z;

		return temp;
	}

	public static Vector sumVectors(Vector A, Vector B) {
		Vector temp = new Vector();
		temp.x = A.x + B.x;
		temp.y = A.y + B.y;
		temp.z = A.z + B.z;

		return temp;
	}

	public static Matrix translationMatrix(double x, double y, double z) {
		Matrix translation = new Matrix(4, 4);

		translation.setIJ(0, 0, 1);
		translation.setIJ(0, 1, 0);
		translation.setIJ(0, 2, 0);
		translation.setIJ(0, 3, x);
		translation.setIJ(1, 0, 0);
		translation.setIJ(1, 1, 1);
		translation.setIJ(1, 2, 0);
		translation.setIJ(1, 3, y);
		translation.setIJ(2, 0, 0);
		translation.setIJ(2, 1, 0);
		translation.setIJ(2, 2, 1);
		translation.setIJ(2, 3, z);
		translation.setIJ(3, 0, 0);
		translation.setIJ(3, 1, 0);
		translation.setIJ(3, 2, 0);
		translation.setIJ(3, 3, 1);

		return translation;
	}

	public static Vector vectorialProduct(Vector a, Vector b) {
		Vector v = new Vector();

		v.x = a.y * b.z - a.z * b.y;
		v.y = a.z * b.x - a.x * b.z;
		v.z = a.x * b.y - a.y * b.x;

		return v;
	}

	public static void whiteScreenCoordinates(Triangle t, int width, int height) {

		t.v1 = Operations.getPerspectiveProjection(t.v1);
		t.v2 = Operations.getPerspectiveProjection(t.v2);
		t.v3 = Operations.getPerspectiveProjection(t.v3);

		t.v1 = Operations.getNormalizedCoordinates(t.v1);
		t.v2 = Operations.getNormalizedCoordinates(t.v2);
		t.v3 = Operations.getNormalizedCoordinates(t.v3);

		t.v1 = Operations.getScreenCoordinates(width, height, t.v1);
		t.v2 = Operations.getScreenCoordinates(width, height, t.v2);
		t.v3 = Operations.getScreenCoordinates(width, height, t.v3);
	}
}
