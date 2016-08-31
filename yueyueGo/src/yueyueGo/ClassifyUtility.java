package yueyueGo;

import weka.classifiers.trees.J48;
import weka.classifiers.trees.M5P;

// 设置一些基础分类器属性的方法组
public class ClassifyUtility {

	//设置M5P的相关参数
	public static M5P prepareM5P(int trainDataCount,int minNumObj,int divide){
		M5P model = new M5P();
		int count=trainDataCount/divide;
		if (count<minNumObj){
			count=minNumObj; //防止树过大
		}
	
		String batchSize=Integer.toString(count);
		model.setBatchSize(batchSize);
		model.setMinNumInstances(count);
		model.setNumDecimalPlaces(6);
		System.out.println(" preparing m5p model.actual minNumObj value:"+model.getMinNumInstances());
		return model;
	}

	public static J48 prepareJ48(int trainDataCount,int minNumObj,int divide){
		//设置基础的J48 classifier参数
		J48 model = new J48();
		int count=trainDataCount/divide;
		if (count<minNumObj){
			count=minNumObj; //防止树过大
		}
		String batchSize=Integer.toString(count);
		model.setBatchSize(batchSize);
		model.setMinNumObj(count);
		model.setNumDecimalPlaces(6);
		model.setDebug(true);
		System.out.println(" preparing j48 model.actual minNumObj value:"+model.getMinNumObj());
		return model;
	}

}
