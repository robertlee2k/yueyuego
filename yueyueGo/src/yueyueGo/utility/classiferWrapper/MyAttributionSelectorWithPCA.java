package yueyueGo.utility.classiferWrapper;

import weka.attributeSelection.Ranker;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Instances;

public class MyAttributionSelectorWithPCA extends AttributeSelectedClassifier {

	public final static boolean NEED_NORMALIZATION=true;
	
	public final static int STANDARDIZE_DATA=0;
	public final static int CENTER_DATA=1;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2060568251010962192L;

	public MyAttributionSelectorWithPCA(boolean normalization,int centerOrStandardize) {
		super();
		MyPrincipalComponents pca = new MyPrincipalComponents();
		//added libo to center & normailize data rather than standardize data in PCA @20170912
		switch (centerOrStandardize) {
		case STANDARDIZE_DATA:
			pca.setCenterData(false);	
			break;
		case CENTER_DATA:
			pca.setCenterData(true);	
			break;
		default:
			//default in PCA is standardize
			break;
		}
		pca.setNormalize(normalization);
		
		Ranker rank = new Ranker();
		setEvaluator(pca);
		setSearch(rank);	
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		super.buildClassifier(data);
		releasePCAData();
	}
	
	public void releasePCAData() {
		MyPrincipalComponents myPCA=null;
		myPCA=(MyPrincipalComponents)this.getEvaluator();
		myPCA.cleanUpInstanceAfterBuilt();
	}

}
