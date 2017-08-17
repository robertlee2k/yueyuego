package yueyueGo.utility;

import java.util.ArrayList;

import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.TwoClassStats;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class NumericThresholdCurve {
	  /** The name of the relation used in threshold curve datasets */
	  public static final String RELATION_NAME = "ThresholdCurve";

	  /** attribute name: True Positives */
	  public static final String TRUE_POS_NAME = "True Positives";
	  /** attribute name: False Negatives */
	  public static final String FALSE_NEG_NAME = "False Negatives";
	  /** attribute name: False Positives */
	  public static final String FALSE_POS_NAME = "False Positives";
	  /** attribute name: True Negatives */
	  public static final String TRUE_NEG_NAME = "True Negatives";
	  /** attribute name: False Positive Rate" */
	  public static final String FP_RATE_NAME = "False Positive Rate";
	  /** attribute name: True Positive Rate */
	  public static final String TP_RATE_NAME = "True Positive Rate";
	  /** attribute name: Precision */
	  public static final String PRECISION_NAME = "Precision";
	  /** attribute name: Recall */
	  public static final String RECALL_NAME = "Recall";
	  /** attribute name: Fallout */
	  public static final String FALLOUT_NAME = "Fallout";
	  /** attribute name: FMeasure */
	  public static final String FMEASURE_NAME = "FMeasure";
	  /** attribute name: Sample Size */
	  public static final String SAMPLE_SIZE_NAME = "Sample Size";
	  /** attribute name: Lift */
	  public static final String LIFT_NAME = "Lift";
	  /** attribute name: Threshold */
	  public static final String THRESHOLD_NAME = "Threshold";
	  
	  

	public Instances getCurve(ArrayList<Prediction> predictions) {
		return getCurve( predictions, 0);
	}

	/**
	 * Calculates the performance stats for the desired class and return results
	 * as a set of Instances.
	 * 
	 * @param predictions the predictions to base the curve on
	 * @param judgeLine. 大于这个值的认为是positive，否则认为是negative （一般为0）
	 * @return datapoints as a set of instances.
	 */
	public Instances getCurve(ArrayList<Prediction> predictions, double judgeLine) {

		if (predictions.size() == 0)
			return null;

		double totPos = 0, totNeg = 0;
		double[] predictedValues = getPredictedValues(predictions);

		// Get distribution of positive/negatives
		for (int i = 0; i < predictedValues.length; i++) {
			NumericPrediction pred = (NumericPrediction) predictions.get(i);
			if (pred.actual() == Prediction.MISSING_VALUE) {
				System.err.println(getClass().getName()
						+ " Skipping prediction with missing class value");
				continue;
			}
			if (pred.weight() < 0) {
				System.err.println(getClass().getName()
						+ " Skipping prediction with negative weight");
				continue;
			}
			if (pred.actual() >judgeLine) { //实际的分布
				totPos += pred.weight();
			} else {
				totNeg += pred.weight();
			}
		}

		Instances insts = makeHeader();
		int[] sorted = Utils.sort(predictedValues);
		TwoClassStats tc = new TwoClassStats(totPos, totNeg, 0, 0);
		double threshold = -999999; //从最大负数开始算
		double cumulativePos = 0;
		double cumulativeNeg = 0;

		for (int i = 0; i < sorted.length; i++) {

			if ((i == 0) || (predictedValues[sorted[i]] > threshold)) {
				tc.setTruePositive(tc.getTruePositive() - cumulativePos);
				tc.setFalseNegative(tc.getFalseNegative() + cumulativePos);
				tc.setFalsePositive(tc.getFalsePositive() - cumulativeNeg);
				tc.setTrueNegative(tc.getTrueNegative() + cumulativeNeg);
				threshold = predictedValues[sorted[i]];
				insts.add(makeInstance(tc, threshold));
				cumulativePos = 0;
				cumulativeNeg = 0;
				if (i == sorted.length - 1) {
					break;
				}
			}

			NumericPrediction pred = (NumericPrediction) predictions.get(sorted[i]);

			if (pred.actual() == Prediction.MISSING_VALUE) {
				System.err.println(getClass().getName()
						+ " Skipping prediction with missing class value");
				continue;
			}
			if (pred.weight() < 0) {
				System.err.println(getClass().getName()
						+ " Skipping prediction with negative weight");
				continue;
			}
			if (pred.actual() > judgeLine) {
				cumulativePos += pred.weight();
			} else {
				cumulativeNeg += pred.weight();
			}


		}

		// make sure a zero point gets into the curve
		if (tc.getFalseNegative() != totPos || tc.getTrueNegative() != totNeg) {
			tc = new TwoClassStats(0, 0, totNeg, totPos);
			threshold = predictedValues[sorted[sorted.length - 1]] + 10e-6;
			insts.add(makeInstance(tc, threshold));
		}

		return insts;
	}

	/**
	 * generates the header
	 * 
	 * @return the header
	 */
	private Instances makeHeader() {

		ArrayList<Attribute> fv = new ArrayList<Attribute>();
		fv.add(new Attribute(TRUE_POS_NAME));
		fv.add(new Attribute(FALSE_NEG_NAME));
		fv.add(new Attribute(FALSE_POS_NAME));
		fv.add(new Attribute(TRUE_NEG_NAME));
		fv.add(new Attribute(FP_RATE_NAME));
		fv.add(new Attribute(TP_RATE_NAME));
		fv.add(new Attribute(PRECISION_NAME));
		fv.add(new Attribute(RECALL_NAME));
		fv.add(new Attribute(FALLOUT_NAME));
		fv.add(new Attribute(FMEASURE_NAME));
		fv.add(new Attribute(SAMPLE_SIZE_NAME));
		fv.add(new Attribute(LIFT_NAME));
		fv.add(new Attribute(THRESHOLD_NAME));
		return new Instances(RELATION_NAME, fv, 100);
	}

	/**
	 * generates an instance out of the given data
	 * 
	 * @param tc the statistics
	 * @param prob the probability
	 * @return the generated instance
	 */
	private Instance makeInstance(TwoClassStats tc, double prob) {

		int count = 0;
		double[] vals = new double[13];
		vals[count++] = tc.getTruePositive();
		vals[count++] = tc.getFalseNegative();
		vals[count++] = tc.getFalsePositive();
		vals[count++] = tc.getTrueNegative();
		vals[count++] = tc.getFalsePositiveRate();
		vals[count++] = tc.getTruePositiveRate();
		vals[count++] = tc.getPrecision();
		vals[count++] = tc.getRecall();
		vals[count++] = tc.getFallout();
		vals[count++] = tc.getFMeasure();
		double ss = (tc.getTruePositive() + tc.getFalsePositive())
				/ (tc.getTruePositive() + tc.getFalsePositive() + tc.getTrueNegative() + tc
						.getFalseNegative());
		vals[count++] = ss;
		double expectedByChance = (ss * (tc.getTruePositive() + tc
				.getFalseNegative()));
		if (expectedByChance < 1) {
			vals[count++] = Utils.missingValue();
		} else {
			vals[count++] = tc.getTruePositive() / expectedByChance;

		}
		vals[count++] = prob;
		return new DenseInstance(1.0, vals);
	}

	/**
	 * 
	 * @param predictions the predictions to use
	 * @return the predicted values probabilities
	 */
	private double[] getPredictedValues(ArrayList<Prediction> predictions) {

		// sort by predicted probability of the desired class.
		double[] probs = new double[predictions.size()];
		for (int i = 0; i < probs.length; i++) {
			NumericPrediction pred = (NumericPrediction) predictions.get(i);
			probs[i] = pred.predicted();
		}
		return probs;
	}
}
