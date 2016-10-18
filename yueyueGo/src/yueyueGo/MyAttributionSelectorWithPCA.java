package yueyueGo;

import weka.attributeSelection.Ranker;
import weka.classifiers.meta.AttributeSelectedClassifier;
import yueyueGo.databeans.BaseInstances;
import yueyueGo.databeans.WekaInstances;

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
	
//	public Instances preprocessData(Instances data) throws Exception {
//		MyPrincipalComponents pca =(MyPrincipalComponents)getEvaluator();
//		return pca.prepareTrainingData(data);
//	}
	 public void buildClassifier(BaseInstances data) throws Exception {
		 super.buildClassifier(WekaInstances.convertToWekaInstances(data));
		 releasePCAData();
	 }
	 
	 
	
	private void releasePCAData() {
		MyPrincipalComponents myPCA=null;
		myPCA=(MyPrincipalComponents)this.getEvaluator();
		myPCA.cleanUpInstanceAfterBuilt();
	}

}
