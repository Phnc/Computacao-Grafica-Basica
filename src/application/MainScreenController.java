package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import beans.Light;
import beans.Matrix;
import beans.Point;
import beans.Vector;
import beans.VirtualCamera;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import operations.Operations;

public class MainScreenController extends Application implements Initializable {

	public static String loaded;

	public static int width;

	public static int height;

	public static Matrix applied;

	public static double angle;

	public static void main(String[] args) {
		launch(args);
	}

    @FXML
    private Canvas canvas;

    @FXML
    private TextField Cx;

    @FXML
    private TextField Cy;

    @FXML
    private TextField Cz;

    @FXML
    private TextField Vx;

    @FXML
    private TextField Vy;

    @FXML
    private TextField Vz;

    @FXML
    private TextField Nx;

    @FXML
    private TextField Ny;

    @FXML
    private TextField Nz;

    @FXML
    private TextField D;

    @FXML
    private TextField Hx;

    @FXML
    private TextField Hy;

    @FXML
    private ComboBox<String> pictures;

    @FXML
    private TextField IambR;

    @FXML
    private TextField IambG;

    @FXML
    private TextField IambB;

    @FXML
    private TextField Ka;

    @FXML
    private TextField IlR;

    @FXML
    private TextField IlG;

    @FXML
    private TextField IlB;

    @FXML
    private TextField PlX;

    @FXML
    private TextField PlY;

    @FXML
    private TextField PlZ;

    @FXML
    private TextField KdR;

    @FXML
    private TextField KdG;

    @FXML
    private TextField KdB;

    @FXML
    private TextField OdR;

    @FXML
    private TextField OdG;

    @FXML
    private TextField OdB;

    @FXML
    private TextField Ks;

    @FXML
    private TextField Eta;

	private VirtualCamera virtualCamera;
	
	private Light light;

	private void comboBoxFiles() {
		ArrayList<String> pics = new ArrayList<>();
		pics.add("");
		pics.add("calice2");
		pics.add("maca");
		pics.add("maca2");
		pics.add("piramide");
		pics.add("triangulo");
		pics.add("vaso");
		pictures.setItems(FXCollections.observableArrayList(pics));
		pictures.getSelectionModel().select("");
		pictures.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (arg2 != "") {
					loaded = arg2 + ".byu";
					paint();
				}
			}
		});
	}

	private void fillFields() {
		Cx.setText("" + VirtualCamera.C.x);
		Cy.setText("" + VirtualCamera.C.y);
		Cz.setText("" + VirtualCamera.C.z);
		D.setText("" + VirtualCamera.D);
		Hx.setText("" + VirtualCamera.Hx);
		Hy.setText("" + VirtualCamera.Hy);
		Vx.setText("" + VirtualCamera.V.x);
		Vy.setText("" + VirtualCamera.V.y);
		Vz.setText("" + VirtualCamera.V.z);
		Nx.setText("" + VirtualCamera.N.x);
		Ny.setText("" + VirtualCamera.N.y);
		Nz.setText("" + VirtualCamera.N.z);
		virtualCamera = VirtualCamera.getCamera();
		
		IambR.setText("" + Light.Iamb.x);
		IambG.setText("" + Light.Iamb.y);
		IambB.setText("" + Light.Iamb.z);
		Ka.setText("" + Light.Ka);
		IlR.setText("" + Light.Il.x);
		IlG.setText("" + Light.Il.y);
		IlB.setText("" + Light.Il.z);
		PlX.setText("" + Light.Pl.x);
		PlY.setText("" + Light.Pl.y);
		PlZ.setText("" + Light.Pl.z);
		KdR.setText("" + Light.Kd.x);
		KdG.setText("" + Light.Kd.y);
		KdB.setText("" + Light.Kd.z);
		OdR.setText("" + Light.Od.x);
		OdG.setText("" + Light.Od.y);
		OdB.setText("" + Light.Od.z);
		Ks.setText("" + Light.Ks);
		Eta.setText("" + Light.Eta);

		light = Light.getLight();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		runPreparation();
		comboBoxFiles();
	}

	public void load() throws IOException {
		if (loaded != null) {
			File camera = new File("CameraParameters.txt");
			BufferedWriter w = new BufferedWriter(new FileWriter(camera));
			Point c = new Point();
			c.x = Double.parseDouble(Cx.getText());
			c.y = Double.parseDouble(Cy.getText());
			c.z = Double.parseDouble(Cz.getText());
			Vector n = new Vector();
			n.x = Double.parseDouble(Nx.getText());
			n.y = Double.parseDouble(Ny.getText());
			n.z = Double.parseDouble(Nz.getText());
			Vector v = new Vector();
			v.x = Double.parseDouble(Vx.getText());
			v.y = Double.parseDouble(Vy.getText());
			v.z = Double.parseDouble(Vz.getText());
			VirtualCamera x = new VirtualCamera(c, n, v, Double.parseDouble(D.getText()),
					Double.parseDouble(Hx.getText()), Double.parseDouble(Hy.getText()));
			if (!virtualCamera.equals(x)) {
				w.write("N = " + Nx.getText() + " " + Ny.getText() + " " + Nz.getText());
				w.newLine();
				w.write("V = " + Vx.getText() + " " + Vy.getText() + " " + Nz.getText());
				w.newLine();
				w.write("d = " + D.getText());
				w.newLine();
				w.write("hx = " + Hx.getText());
				w.newLine();
				w.write("hy = " + Hy.getText());
				w.newLine();
				w.write("C = " + Cx.getText() + " " + Cy.getText() + " " + Cz.getText());
				w.close();
				Operations.loadCameraParameters();
			}

			File light = new File("Light.txt");
			BufferedWriter wr = new BufferedWriter(new FileWriter(light));
			
			Vector Iamb = new Vector();
			Iamb.x = Double.parseDouble(IambR.getText());
			Iamb.y = Double.parseDouble(IambG.getText());
			Iamb.z = Double.parseDouble(IambB.getText());
			Vector Il = new Vector();
			Il.x = Double.parseDouble(IlR.getText());
			Il.y = Double.parseDouble(IlG.getText());
			Il.z = Double.parseDouble(IlB.getText());
			Point Pl = new Point();
			Pl.x = Double.parseDouble(PlX.getText());
			Pl.y = Double.parseDouble(PlY.getText());
			Pl.z = Double.parseDouble(PlZ.getText());
			Vector Kd = new Vector();
			Kd.x = Double.parseDouble(KdR.getText());
			Kd.y = Double.parseDouble(KdG.getText());
			Kd.z = Double.parseDouble(KdB.getText());
			Vector Od = new Vector();
			Od.x = Double.parseDouble(OdR.getText());
			Od.y = Double.parseDouble(OdG.getText());
			Od.z = Double.parseDouble(OdB.getText());
			
			Light y = new Light(Iamb, Double.parseDouble(Ka.getText()), Il, Pl, Kd, Od,
					Double.parseDouble(Ks.getText()), Double.parseDouble(Eta.getText()));
			
			if (!this.light.equals(y)) {
				wr.write("Iamb = " + IambR.getText() + " " + IambG.getText() + " " + IambB.getText());
				wr.newLine();
				wr.write("Ka = " + Ka.getText());
				wr.newLine();
				wr.write("Il = " + IlR.getText() + " " + IlG.getText() + " " + IlB.getText());
				wr.newLine();
				wr.write("Pl = " + PlX.getText() + " " + PlY.getText() + " " + PlZ.getText());
				wr.newLine();
				wr.write("Kd = " + KdR.getText() + " " + KdG.getText() + " " + KdB.getText());
				wr.newLine();
				wr.write("Od = " + OdR.getText() + " " + OdG.getText() + " " + OdB.getText());
				wr.newLine();
				wr.write("Ks = " + Ks.getText());
				wr.newLine();
				wr.write("Eta = " + Eta.getText());
				wr.close();
				Operations.loadLightParameters();
			}
			
			
			try {
				Operations.lightPaint(width, height, loaded, applied);
				//paint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void paint() {
		//canvas.getGraphicsContext2D().setFill(Color.BLACK);
		//canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		//canvas.setVisible(true);
		try {
			//Operations.scanLinePaint(canvas.getGraphicsContext2D(), width, height, loaded);
			Operations.lightPaint(width, height, loaded, applied);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void runPreparation() {
		angle = 0;
		applied = Operations.identityMatrix();
		Operations.graphicsContext = canvas.getGraphicsContext2D();
		width = (int) canvas.getWidth();
		height = (int) canvas.getHeight();
		try {
			Operations.loadCameraParameters();
			Operations.loadLightParameters();
			fillFields();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
		try {
			stage.setScene(new Scene(FXMLLoader.load(this.getClass().getResource("../MainScreen.fxml"))));
			stage.setResizable(false);
			stage.setTitle("Projeto de Computação Gráfica - VA 2");
			stage.show();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

}
