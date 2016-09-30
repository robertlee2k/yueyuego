package yueyueGo.misc;

import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.supportVector.CachedKernel;
import weka.classifiers.functions.supportVector.Kernel;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Capabilities.Capability;
import weka.core.matrix.Matrix;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.Standardize;

public class MyGaussianClassifier extends GaussianProcesses {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3401715795914368112L;
	 /**
	   * Method for building the classifier.
	   * 
	   * @param insts the set of training instances
	   * @throws Exception if the classifier can't be built successfully
	   */
	  @Override
	  public void buildClassifier(Instances insts) throws Exception {

	    /* check the set of training instances */
	    if (!m_checksTurnedOff) {
	      // can classifier handle the data?
	      getCapabilities().testWithFail(insts);

	      // remove instances with missing class
	      insts = new Instances(insts);
	      insts.deleteWithMissingClass();
	      m_Missing = new ReplaceMissingValues();
	      m_Missing.setInputFormat(insts);
	      insts = Filter.useFilter(insts, m_Missing);
	    } else {
	      m_Missing = null;
	    }

	    if (getCapabilities().handles(Capability.NUMERIC_ATTRIBUTES)) {
	      boolean onlyNumeric = true;
	      if (!m_checksTurnedOff) {
	        for (int i = 0; i < insts.numAttributes(); i++) {
	          if (i != insts.classIndex()) {
	            if (!insts.attribute(i).isNumeric()) {
	              onlyNumeric = false;
	              break;
	            }
	          }
	        }
	      }

	      if (!onlyNumeric) {
	        m_NominalToBinary = new NominalToBinary();
	        m_NominalToBinary.setInputFormat(insts);
	        insts = Filter.useFilter(insts, m_NominalToBinary);
	      } else {
	        m_NominalToBinary = null;
	      }
	    } else {
	      m_NominalToBinary = null;
	    }

	    if (m_filterType == FILTER_STANDARDIZE) {
	      m_Filter = new Standardize();
	      ((Standardize) m_Filter).setIgnoreClass(true);
	      m_Filter.setInputFormat(insts);
	      insts = Filter.useFilter(insts, m_Filter);
	    } else if (m_filterType == FILTER_NORMALIZE) {
	      m_Filter = new Normalize();
	      ((Normalize) m_Filter).setIgnoreClass(true);
	      m_Filter.setInputFormat(insts);
	      insts = Filter.useFilter(insts, m_Filter);
	    } else {
	      m_Filter = null;
	    }

	    m_NumTrain = insts.numInstances();

	    // determine which linear transformation has been
	    // applied to the class by the filter
	    if (m_Filter != null) {
	      Instance witness = (Instance) insts.instance(0).copy();
	      witness.setValue(insts.classIndex(), 0);
	      m_Filter.input(witness);
	      m_Filter.batchFinished();
	      Instance res = m_Filter.output();
	      m_Blin = res.value(insts.classIndex());
	      witness.setValue(insts.classIndex(), 1);
	      m_Filter.input(witness);
	      m_Filter.batchFinished();
	      res = m_Filter.output();
	      m_Alin = res.value(insts.classIndex()) - m_Blin;
	    } else {
	      m_Alin = 1.0;
	      m_Blin = 0.0;
	    }

	    // Initialize kernel
//	    try {
//	      CachedKernel cachedKernel = (CachedKernel) m_kernel;
//	      cachedKernel.setCacheSize(0);
//	    } catch (Exception e) {
//	      // ignore
//	    	
//	    }
	    m_kernel.buildKernel(insts);

	    // Compute average target value
	    double sum = 0.0;
	    for (int i = 0; i < insts.numInstances(); i++) {
	      sum += insts.instance(i).classValue();
	    }
	    m_avg_target = sum / insts.numInstances();

	    // Store squared noise level
	    m_deltaSquared = m_delta * m_delta;

	    // initialize kernel matrix/covariance matrix
	    int n = insts.numInstances();
	    m_L = new double[n][];
	    double kv = 0;
	    for (int i = 0; i < n; i++) {
	      m_L[i] = new double[i + 1];
	      for (int j = 0; j < i; j++) {
	        kv = m_kernel.eval(i, j, insts.instance(i));
	        m_L[i][j] = kv;
	      }
	      kv = m_kernel.eval(i, i, insts.instance(i));
	      m_L[i][i] = kv + m_deltaSquared;
	    }

	    // Save memory (can't use Kernel.clean() because of polynominal kernel with
	    // exponent 1)
	    if (m_kernel instanceof CachedKernel) {
	      m_kernel = Kernel.makeCopy(m_kernel);
	      ((CachedKernel) m_kernel).setCacheSize(-1);
	      m_kernel.buildKernel(insts);
	    }

	    // Calculate inverse matrix exploiting symmetry of covariance matrix
	    // NB this replaces the kernel matrix with (the negative of) its inverse and
	    // does
	    // not require any extra memory for a solution matrix
	    double[] tmprow = new double[n];
	    double tmp2 = 0, tmp = 0;
	    for (int i = 0; i < n; i++) {
	      tmp = -m_L[i][i];
	      m_L[i][i] = 1.0 / tmp;
	      for (int j = 0; j < n; j++) {
	        if (j != i) {
	          if (j < i) {
	            tmprow[j] = m_L[i][j];
	            m_L[i][j] /= tmp;
	            tmp2 = m_L[i][j];
	            m_L[j][j] += tmp2 * tmp2 * tmp;
	          } else if (j > i) {
	            tmprow[j] = m_L[j][i];
	            m_L[j][i] /= tmp;
	            tmp2 = m_L[j][i];
	            m_L[j][j] += tmp2 * tmp2 * tmp;
	          }
	        }
	      }

	      for (int j = 0; j < n; j++) {
	        if (j != i) {
	          if (i < j) {
	            for (int k = 0; k < i; k++) {
	              m_L[j][k] += tmprow[j] * m_L[i][k];
	            }
	          } else {
	            for (int k = 0; k < j; k++) {
	              m_L[j][k] += tmprow[j] * m_L[i][k];
	            }

	          }
	          for (int k = i + 1; k < j; k++) {
	            m_L[j][k] += tmprow[j] * m_L[k][i];
	          }
	        }
	      }
	    }

	    m_t = new Matrix(insts.numInstances(), 1);
	    double[] tt = new double[n];
	    for (int i = 0; i < n; i++) {
	      tt[i] = insts.instance(i).classValue() - m_avg_target;
	    }

	    // calculate m_t = tt . m_L
	    for (int i = 0; i < n; i++) {
	      double s = 0;
	      for (int k = 0; k < i; k++) {
	        s -= m_L[i][k] * tt[k];
	      }
	      for (int k = i; k < n; k++) {
	        s -= m_L[k][i] * tt[k];
	      }
	      m_t.set(i, 0, s);
	    }

	  } // buildClassifier

}
