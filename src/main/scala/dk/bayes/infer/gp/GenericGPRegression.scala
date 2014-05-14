package dk.bayes.infer.gp

import dk.bayes.math.linear._
import scala.math._

/**
 * Gaussian Process Regression. It uses Gaussian likelihood and zero mean functions.
 *
 * @param x Inputs. [NxD] matrix, where N - number of training examples, D - dimensionality of input space
 * @param y Targets. [Nx1] matrix, where N - number of training examples
 * @param covFunc Covariance function
 * @param noiseStdDev Log of noise standard deviation of Gaussian likelihood function
 *
 */
case class GenericGPRegression(x: Matrix, y: Matrix, covFunc: CovFunc, noiseStdDev: Double) extends GPRegression {

  private val kXX = cov(x)
  private val kXXInv = kXX.inv

  /**
   * @param z Inputs for making predictions. [NxD] matrix. N - number of test points, D - dimensionality of input space
   * @return Predicted targets.[mean variance]
   */
  def predict(z: Matrix): Matrix = {

    val kXZ = cov(x, z)
    val kZZ = cov(z)

    val predMean = kXZ.t * (kXXInv * y)
    val predVar = kZZ - kXZ.t * kXXInv * kXZ

    predMean.combine(0, 1, predVar.extractDiag)
  }

  /**
   * @param v [N x D] vector
   * @return [N x N] covariance matrix
   */
  private def cov(v: Matrix): Matrix = covFunc.cov(v) + exp(2*noiseStdDev) * Matrix.identity(v.numRows)

  /**
   * @param x [N x D] vector
   * @param z [M x D] vector
   * @return [N x M] covariance matrix
   */
  private def cov(x: Matrix, z: Matrix): Matrix = {
    Matrix(x.numRows, z.numRows, (rowIndex, colIndex) => covFunc.cov(x.row(rowIndex).t, z.row(colIndex).t))
  }
}