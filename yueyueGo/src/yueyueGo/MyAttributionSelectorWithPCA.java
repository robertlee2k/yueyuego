package yueyueGo;

import weka.attributeSelection.Ranker;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Instances;

public class MyAttributionSelectorWithPCA extends AttributeSelectedClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2060568251010962192L;

	public MyAttributionSelectorWithPCA() {
		super();
		MyPrincipalComponents pca = new MyPrincipalComponents();
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
