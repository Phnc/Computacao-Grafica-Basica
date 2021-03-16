package beans;

public class Matrix {
	private double matrix[][];
	private double transposed[][];
	private double inverse[][];
	private double cofactors[][];
	private Double determinant;

	public Matrix(double[][] matrix) {
		this.matrix = matrix;
	}

	public Matrix(int lineCount, int colCount) {
		matrix = new double[lineCount][colCount];
	}

	public double[][] getCofactors() {
		if (cofactors == null) {

			cofactors = new double[matrix.length][matrix.length];

			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix.length; j++) {
					double[][] aux = new double[matrix.length - 1][matrix.length - 1];
					int i_aux = 0, j_aux = 0;
					boolean ok = false;

					for (int linha = 0; linha < matrix.length; linha++) {
						for (int coluna = 0; coluna < matrix.length; coluna++) {
							if (linha != i && coluna != j) {
								aux[i_aux][j_aux] = matrix[linha][coluna];
								j_aux++;
								ok = true;
							}
						}
						if (ok) {
							i_aux++;
							j_aux = 0;
							ok = false;
						}

					}
					Matrix a = new Matrix(aux);
					double cofator = Math.pow(-1, (i + 1) + (j + 1)) * a.getDeterminant();
					if (cofator == -0)
						cofactors[i][j] = 0;
					else
						cofactors[i][j] = cofator;
				}
			}

		}
		return cofactors;
	}

	public int getColCount() {
		return this.matrix[0].length;
	}

	public double getDeterminant() {
		Double det = null;
		if (det == null) {
			if (matrix.length == 1) {
				det = matrix[0][0];
			} else if (matrix.length == 2) {
				det = matrix[0][0] * matrix[1][1] - (matrix[0][1] * matrix[1][0]);
			} else if (matrix.length == 3) {
				det = ((matrix[0][0] * matrix[1][1] * matrix[2][2] + matrix[0][1] * matrix[1][2] * matrix[2][0]
						+ matrix[0][2] * matrix[1][0] * matrix[2][1])
						- (matrix[0][2] * matrix[1][1] * matrix[2][0] + matrix[0][0] * matrix[1][2] * matrix[2][1]
								+ matrix[0][1] * matrix[1][0] * matrix[2][2]));
			} else {
				det = 0.0;
				double[][] aux;
				int i_aux, j_aux, linha, coluna, i;

				for (i = 0; i < matrix.length; i++) {

					if (matrix[0][i] != 0) {
						aux = new double[matrix.length - 1][matrix.length - 1];
						i_aux = 0;
						j_aux = 0;

						for (linha = 1; linha < matrix.length; linha++) {
							for (coluna = 0; coluna < matrix.length; coluna++) {
								if (coluna != i) {
									aux[i_aux][j_aux] = matrix[linha][coluna];
									j_aux++;
								}
							}

							i_aux++;
							j_aux = 0;
						}
						Matrix a = new Matrix(aux);
						det += Math.pow(-1, i) * matrix[0][i] * a.getDeterminant();
					}

				}
			}
		}

		return det;
	}

	public double getIJ(int i, int j) {
		return matrix[i][j];
	}

	public double[][] getInverse() {
		if (inverse == null) {
			if (getLineCount() == getColCount()) {
				determinant = getDeterminant();
				inverse = new double[getLineCount()][getColCount()];
				if (matrix.length == 2) {
					double valor = matrix[1][1] / determinant;

					if (valor == -0)
						inverse[0][0] = 0;
					else
						inverse[0][0] = matrix[1][1] / determinant;

					valor = matrix[0][0] / determinant;
					if (valor == -0)
						inverse[1][1] = 0;
					else
						inverse[1][1] = matrix[0][0] / determinant;

					valor = -matrix[0][1] / determinant;
					if (valor == -0)
						inverse[0][1] = 0;
					else
						inverse[0][1] = -matrix[0][1] / determinant;

					valor = -matrix[1][0] / determinant;
					if (valor == -0)
						inverse[1][0] = 0;
					else
						inverse[1][0] = -matrix[1][0] / determinant;
				} else if (matrix.length == 3) {
					Matrix cofatores = new Matrix(getCofactors());
					Matrix transposta = new Matrix(cofatores.getTransposed());

					for (int i = 0; i < inverse.length; i++) {
						for (int j = 0; j < inverse.length; j++) {
							double valor = (1 / determinant) * transposta.getIJ(i, j);
							if (valor == -0)
								inverse[i][j] = 0;
							else
								inverse[i][j] = valor;
						}
					}
				}
			}

		}

		return inverse;
	}

	public int getLineCount() {
		return this.matrix.length;
	}

	public double[][] getMatrix() {
		return this.matrix;
	}

	public double[][] getTransposed() {
		if (transposed == null) {
			transposed = new double[getColCount()][getLineCount()];
			for (int i = 0; i < getLineCount(); i++) {
				for (int j = 0; j < getColCount(); j++) {
					transposed[j][i] = this.matrix[i][j];
				}
			}
		}

		return transposed;
	}

	public void setIJ(int i, int j, double num) {
		this.matrix[i][j] = num;
	}

	public void setMatrix(double[][] matrix) {
		this.matrix = matrix;
	}

}
