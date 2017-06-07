package org.gumtree.data.nexus.fitting.apache;

import java.util.Arrays;

import org.apache.commons.math3.analysis.DifferentiableUnivariateFunction;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

/**
 * <a href="http://en.wikipedia.org/wiki/Gaussian_function">
 *  Gaussian</a> function.
 *
 * @since 3.0
 */
public class GaussianWithBackgroundFunction implements UnivariateDifferentiableFunction, DifferentiableUnivariateFunction {
    /** Mean. */
    private final double mean;
    /** Inverse of the standard deviation. */
    private final double is;
    /** Inverse of twice the square of the standard deviation. */
    private final double i2s2;
    /** Normalization factor. */
    private final double norm;
    /** Backgound factor. */
    private final double bg;

    /**
     * Gaussian with given normalization factor, mean and standard deviation.
     *
     * @param norm Normalization factor.
     * @param mean Mean.
     * @param sigma Standard deviation.
     * @param bg background.
     * @throws NotStrictlyPositiveException if {@code sigma <= 0}.
     */
    public GaussianWithBackgroundFunction(double norm,
                    double mean,
                    double sigma, 
                    double bg)
        throws NotStrictlyPositiveException {
        if (sigma <= 0) {
            throw new NotStrictlyPositiveException(sigma);
        }

        this.norm = norm;
        this.mean = mean;
        this.is   = 1 / sigma;
        this.bg = bg;
        this.i2s2 = 0.5 * is * is;
    }

    /**
     * Normalized gaussian with given mean and standard deviation.
     *
     * @param mean Mean.
     * @param sigma Standard deviation.
     * @throws NotStrictlyPositiveException if {@code sigma <= 0}.
     */
    public GaussianWithBackgroundFunction(double mean,
                    double sigma)
        throws NotStrictlyPositiveException {
        this(1 / (sigma * FastMath.sqrt(2 * Math.PI)), mean, sigma, 0.);
    }

    /**
     * Normalized gaussian with zero mean and unit standard deviation.
     */
    public GaussianWithBackgroundFunction() {
        this(0, 1);
    }

    /** {@inheritDoc} */
    public double value(double x) {
        return value(x - mean, norm, i2s2, bg);
    }

    /** {@inheritDoc}
     * @deprecated as of 3.1, replaced by {@link #value(DerivativeStructure)}
     */
    @Deprecated
    public UnivariateFunction derivative() {
        return FunctionUtils.toDifferentiableUnivariateFunction(this).derivative();
    }

    /**
     * Parametric function where the input array contains the parameters of
     * the Gaussian, ordered as follows:
     * <ul>
     *  <li>Norm</li>
     *  <li>Mean</li>
     *  <li>Standard deviation</li>
     * </ul>
     */
    public static class Parametric implements ParametricUnivariateFunction {
        /**
         * Computes the value of the Gaussian at {@code x}.
         *
         * @param x Value for which the function must be computed.
         * @param param Values of norm, mean and standard deviation.
         * @return the value of the function.
         * @throws NullArgumentException if {@code param} is {@code null}.
         * @throws DimensionMismatchException if the size of {@code param} is
         * not 3.
         * @throws NotStrictlyPositiveException if {@code param[2]} is negative.
         */
        public double value(double x, double ... param)
            throws NullArgumentException,
                   DimensionMismatchException,
                   NotStrictlyPositiveException {
            validateParameters(param);

            final double diff = x - param[1];
            final double i2s2 = 1 / (2 * param[2] * param[2]);
            return GaussianWithBackgroundFunction.value(diff, param[0], i2s2, param[3]);
        }

        /**
         * Computes the value of the gradient at {@code x}.
         * The components of the gradient vector are the partial
         * derivatives of the function with respect to each of the
         * <em>parameters</em> (norm, mean and standard deviation).
         *
         * @param x Value at which the gradient must be computed.
         * @param param Values of norm, mean and standard deviation.
         * @return the gradient vector at {@code x}.
         * @throws NullArgumentException if {@code param} is {@code null}.
         * @throws DimensionMismatchException if the size of {@code param} is
         * not 3.
         * @throws NotStrictlyPositiveException if {@code param[2]} is negative.
         */
        public double[] gradient(double x, double ... param)
            throws NullArgumentException,
                   DimensionMismatchException,
                   NotStrictlyPositiveException {
            validateParameters(param);

            final double norm = param[0];
            final double diff = x - param[1];
            final double sigma = param[2];
            final double i2s2 = 1 / (2 * sigma * sigma);
            final double bg = param[3];

            final double n = GaussianWithBackgroundFunction.value(diff, 1, i2s2, bg) - bg;
            final double m = norm * n * 2 * i2s2 * diff;
            final double s = m * diff / sigma;
            final double b = 1;

            return new double[] { n, m, s, b };
        }

        /**
         * Validates parameters to ensure they are appropriate for the evaluation of
         * the {@link #value(double,double[])} and {@link #gradient(double,double[])}
         * methods.
         *
         * @param param Values of norm, mean and standard deviation.
         * @throws NullArgumentException if {@code param} is {@code null}.
         * @throws DimensionMismatchException if the size of {@code param} is
         * not 3.
         * @throws NotStrictlyPositiveException if {@code param[2]} is negative.
         */
        private void validateParameters(double[] param)
            throws NullArgumentException,
                   DimensionMismatchException,
                   NotStrictlyPositiveException {
            if (param == null) {
                throw new NullArgumentException();
            }
            if (param.length != 4) {
                throw new DimensionMismatchException(param.length, 4);
            }
            if (param[2] <= 0) {
                throw new NotStrictlyPositiveException(param[2]);
            }
        }
    }

    /**
     * @param xMinusMean {@code x - mean}.
     * @param norm Normalization factor.
     * @param i2s2 Inverse of twice the square of the standard deviation.
     * @return the value of the Gaussian at {@code x}.
     */
    private static double value(double xMinusMean,
                                double norm,
                                double i2s2, 
                                double bg) {
        return norm * FastMath.exp(-xMinusMean * xMinusMean * i2s2) + bg;
    }

    /** {@inheritDoc}
     * @since 3.1
     */
    public DerivativeStructure value(final DerivativeStructure t)
        throws DimensionMismatchException {

        final double u = is * (t.getValue() - mean);
        double[] f = new double[t.getOrder() + 1];

        // the nth order derivative of the Gaussian has the form:
        // dn(g(x)/dxn = (norm / s^n) P_n(u) exp(-u^2/2) with u=(x-m)/s
        // where P_n(u) is a degree n polynomial with same parity as n
        // P_0(u) = 1, P_1(u) = -u, P_2(u) = u^2 - 1, P_3(u) = -u^3 + 3 u...
        // the general recurrence relation for P_n is:
        // P_n(u) = P_(n-1)'(u) - u P_(n-1)(u)
        // as per polynomial parity, we can store coefficients of both P_(n-1) and P_n in the same array
        final double[] p = new double[f.length];
        p[0] = 1;
        final double u2 = u * u;
        double coeff = norm * FastMath.exp(-0.5 * u2);
        if (coeff <= Precision.SAFE_MIN) {
            Arrays.fill(f, 0.0);
        } else {
            f[0] = coeff;
            for (int n = 1; n < f.length; ++n) {

                // update and evaluate polynomial P_n(x)
                double v = 0;
                p[n] = -p[n - 1];
                for (int k = n; k >= 0; k -= 2) {
                    v = v * u2 + p[k];
                    if (k > 2) {
                        p[k - 2] = (k - 1) * p[k - 1] - p[k - 3];
                    } else if (k == 2) {
                        p[0] = p[1];
                    }
                }
                if ((n & 0x1) == 1) {
                    v *= u;
                }

                coeff *= is;
                f[n] = coeff * v;

            }
        }

        return t.compose(f);

    }

}