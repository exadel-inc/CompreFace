package com.exadel.frs.core.trainservice.component;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smile.classification.LogisticRegression;
import smile.math.DifferentiableMultivariateFunction;
import smile.math.Math;
import java.lang.reflect.InvocationTargetException;

public class LogisticRegressionExt {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(LogisticRegression.class);

    /**
     * The dimension of input space.
     */
    private int p;

    /**
     * The number of classes.
     */
    private int k;

    /**
     * The log-likelihood of learned model.
     */
    private double L;

    /**
     * The linear weights for binary logistic regression.
     */
    private double[] w;

    /**
     * The linear weights for multi-class logistic regression.
     */
    private double[][] W;

    /**
     * Regularization factor.
     */
    private double lambda;

    /**
     * learning rate for stochastic gradient descent.
     */
    private double eta = 5e-5;

    /**
     * Trainer for logistic regression.
     */

    public LogisticRegressionExt(double[][] x, int[] y, double lambda, double tol, int maxIter) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(
                    String.format("The sizes of X and Y don't match: %d != %d", x.length, y.length));
        }

        if (lambda < 0.0) {
            throw new IllegalArgumentException("Invalid regularization factor: " + lambda);
        }
        this.lambda = lambda;

        if (tol <= 0.0) {
            throw new IllegalArgumentException("Invalid tolerance: " + tol);
        }

        if (maxIter <= 0) {
            throw new IllegalArgumentException("Invalid maximum number of iterations: " + maxIter);
        }

        // class label set.
        int[] labels = Math.unique(y);
        Arrays.sort(labels);

        for (int i = 0; i < labels.length; i++) {
            if (labels[i] < 0) {
                throw new IllegalArgumentException("Negative class label: " + labels[i]);
            }

            if (i > 0 && labels[i] - labels[i - 1] > 1) {
                throw new IllegalArgumentException("Missing class: " + (labels[i - 1] + 1));
            }
        }

        k = labels.length;
        if (k < 2) {
            throw new IllegalArgumentException("Only one class.");
        }

      p = x[0].length;
      if (k == 2) {
            var func = getFunction("BinaryObjectiveFunction", new Object[]{x, y, lambda});
//            BinaryObjectiveFunction func = new BinaryObjectiveFunction(x, y, lambda);

            w = new double[p + 1];

            L = 0.0;
            try {
                L = -Math.min(func, 5, w, tol, maxIter);
            } catch (Exception ex) {
                // If L-BFGS doesn't work, let's try BFGS.
                L = -Math.min(func, w, tol, maxIter);
            }
        } else {
          var func = getFunction("MultiClassObjectiveFunction", new Object[]{x, y, k, lambda});
//      MultiClassObjectiveFunction func = new MultiClassObjectiveFunction(x, y, k, lambda);

            w = new double[k * (p + 1)];

            L = 0.0;
            try {
                L = -Math.min(func, 5, w, tol, maxIter);
            } catch (Exception ex) {
                // If L-BFGS doesn't work, let's try BFGS.
                L = 0.0;
            }

            W = new double[k][p + 1];
            for (int i = 0, m = 0; i < k; i++) {
                for (int j = 0; j <= p; j++, m++) {
                    W[i][j] = w[m];
                }
            }
            w = null;
        }
    }

  private DifferentiableMultivariateFunction getFunction(String funcName, Object[] args) {
    DifferentiableMultivariateFunction func = null;
    try {
      //add choice of constructor
      var constructor = Arrays.stream(LogisticRegression.class.getDeclaredClasses())
              .filter(innerClass -> innerClass.getSimpleName().equals(funcName))
              .findFirst()
              .map(funcClass -> Arrays.stream(funcClass.getDeclaredConstructors())
                      .findFirst().orElseThrow()
              )
              .get();
      constructor.setAccessible(true);
      func = (DifferentiableMultivariateFunction) constructor.newInstance(args);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return func;
  }

  public int predict(double[] x) {
    return predict(x, null);
  }

    public int predict(double[] x, double[] posteriori) {
        if (x.length != p) {
            throw new IllegalArgumentException(
                    String.format("Invalid input vector size: %d, expected: %d", x.length, p));
        }

        if (posteriori != null && posteriori.length != k) {
            throw new IllegalArgumentException(
                    String.format("Invalid posteriori vector size: %d, expected: %d", posteriori.length, k));
        }

        if (k == 2) {
            double f = 1.0 / (1.0 + Math.exp(-dot(x, w)));

            if (posteriori != null) {
                posteriori[0] = 1.0 - f;
                posteriori[1] = f;
            }

            if (f < 0.5) {
                return 0;
            } else {
                return 1;
            }
        } else {
            int label = -1;
            double max = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < k; i++) {
                double prob = dot(x, W[i]);
                if (prob > max) {
                    max = prob;
                    label = i;
                }

                if (posteriori != null) {
                    posteriori[i] = prob;
                }
            }

            if (posteriori != null) {
                double Z = 0.0;
                for (int i = 0; i < k; i++) {
                    posteriori[i] = Math.exp(posteriori[i] - max);
                    Z += posteriori[i];
                }

                for (int i = 0; i < k; i++) {
                    posteriori[i] /= Z;
                }
            }

            return label;
        }
    }

    public static class Trainer {

        /**
         * Regularization factor. &lambda; > 0 gives a "regularized" estimate of linear weights which
         * often has superior generalization performance, especially when the dimensionality is high.
         */
        private double lambda = 0.0;
        /**
         * The tolerance for BFGS stopping iterations.
         */
        private double tol = 1E-5;
        /**
         * The maximum number of BFGS iterations.
         */
        private int maxIter = 500;

        /**
         * Sets the regularization factor. &lambda; &gt; 0 gives a "regularized" estimate of linear
         * weights which often has superior generalization performance, especially when the
         * dimensionality is high.
         *
         * @param lambda regularization factor.
         */
        public Trainer setRegularizationFactor(double lambda) {
            this.lambda = lambda;
            return this;
        }

        /**
         * Sets the tolerance for BFGS stopping iterations.
         *
         * @param tol the tolerance for stopping iterations.
         */
        public Trainer setTolerance(double tol) {
            if (tol <= 0.0) {
                throw new IllegalArgumentException("Invalid tolerance: " + tol);
            }

            this.tol = tol;
            return this;
        }

        /**
         * Sets the maximum number of iterations.
         *
         * @param maxIter the maximum number of iterations.
         */
        public Trainer setMaxNumIteration(int maxIter) {
            if (maxIter <= 0) {
                throw new IllegalArgumentException("Invalid maximum number of iterations: " + maxIter);
            }

            this.maxIter = maxIter;
            return this;
        }

        public LogisticRegressionExt train(final double[][] x, final int[] y) {
            return new LogisticRegressionExt(x, y, lambda, tol, maxIter);
        }
    }

    private static double dot(final double[] x, final double[] w) {
        int i = 0;
        double dot = 0.0;

        for (; i < x.length; i++) {
            dot += x[i] * w[i];
        }

        return dot + w[i];
    }
}