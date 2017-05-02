package yueyueGo.classifier.deprecated;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import yueyueGo.ModelStore;
import yueyueGo.MyAttributionSelectorWithPCA;
import yueyueGo.NominalClassifier;
import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.utility.ClassifyUtility;
@Deprecated
public class J48ABFullModel extends NominalClassifier {
 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3650296432932510448L;
	protected int leafMinObjNum; 	//j48树最小节点叶子数
	protected int divided; //将trainingData分成多少份
	
	@Override
	protected void initializeParams() {

		m_policySubGroup = new String[]{"" };
		modelArffFormat=FullModelDataFormat.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		classifierName= "J48ABFullModel";

		m_modelFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式

		leafMinObjNum=1000; 	//j48树最小节点叶子数
		divided=800; //将trainingData分成多少份

	}
	
	
	@Override
	protected Classifier buildModel(GeneralInstances train) throws Exception {
 
		//设置基础的J48 classifier参数
		J48 model=ClassifyUtility.prepareJ48(train.numInstances(),leafMinObjNum,divided);
	
	    
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();	    
	    classifier.setDebug(true);
	    classifier.setClassifier(model);
	    classifier.buildClassifier(WekaInstances.convertToWekaInstances(train));
	    return classifier;
	}

}
