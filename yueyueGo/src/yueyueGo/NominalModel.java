package yueyueGo;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaAttribute;
import yueyueGo.databeans.WekaInstance;
import yueyueGo.utility.FormatUtility;

public abstract class NominalModel extends AbstractModel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5570283670170193026L;

//	private GeneralInstances m_cachedOldClassInstances=null;
	public static final int CLASS_POSITIVE_INDEX=1;
	public static final int CLASS_NEGATIVE_INDEX=0;




	//对于二分类变量，返回分类1的预测可能性
	@Override
	protected  double classify(Classifier model,GeneralInstance curr) throws Exception {
		double[] problity =  model.distributionForInstance(WekaInstance.convertToWekaInstance(curr));
		return problity[CLASS_POSITIVE_INDEX];
	}
	
	//将原始数据变换为nominal Classifier需要的形式（更换class 变量等等）
	public GeneralInstances processDataForNominalClassifier(GeneralInstances inData) throws Exception{

		int oldClassIndex=inData.classIndex();
		if (oldClassIndex!=(inData.numAttributes()-1)){
			throw new Exception("fatal error! class index should be at the last column");
		}
		ArrayList<String> values=new ArrayList<String>();
		//CLASS_POSITIVE_INDEX=1时这里必须按照这个顺序添加！ 
		values.add(ArffFormat.STRING_VALUE_NO);
		values.add(ArffFormat.STRING_VALUE_YES);
		WekaAttribute newClassAtt=new WekaAttribute(ArffFormat.IS_POSITIVE,values);
		//在classValue之前插入positive,然后记录下它的新位置index
		inData.insertAttributeAt(newClassAtt,inData.numAttributes()-1);
		int newClassIndex=inData.numAttributes()-2;
		double shouyilv=0;
		
//		if (cacheOldClassValue==true){
//			m_cachedOldClassInstances=CreateCachedOldClassInstances();
//		}else{
//			m_cachedOldClassInstances=null;
//		}
		
		for (int i=0;i<inData.numInstances();i++){
			shouyilv=inData.instance(i).classValue();
			if (shouyilv>m_positiveLine){
				inData.instance(i).setValue(newClassIndex, ArffFormat.STRING_VALUE_YES);
			}else {
				inData.instance(i).setValue(newClassIndex, ArffFormat.STRING_VALUE_NO);
			}
			
//			//暂存收益率
//			if (cacheOldClassValue==true){
//				double id=inData.instance(i).value(0);
//				DataInstance cacheRow=new DataInstance(m_cachedOldClassInstances.numAttributes());
//				cacheRow.setDataset(m_cachedOldClassInstances);
//				cacheRow.setValue(0, id);
//				cacheRow.setValue(1, shouyilv);
//				m_cachedOldClassInstances.add(cacheRow);
//			}
		}
		//删除shouyilv
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(inData);
		inData=instanceProcessor.removeAttribs(inData, ""+inData.numAttributes());
		//设置新属性的位置
		inData.setClassIndex(inData.numAttributes()-1);
		System.out.println("class value replaced for nominal classifier. where m_positiveLine= "+m_positiveLine);
		return inData;
	}
	
	

	
//	@Override
//	 //这里覆盖父类方法，为二分类器classify后统计正负收益个数时提供分界值
//	protected double getPositiveLine(){
//	 return m_positiveLine;	
//	}
	

	@Override
	public String getIdentifyName(){
		if (m_positiveLine==0){ //如果是正常的正负分类时就不用特别标记
			return classifierName;
		}else { //如果用自定义的标尺线区分Class的正负，则特别标记
			return (classifierName+"("+FormatUtility.formatDouble(m_positiveLine)+")");
		}
	}
	
//	//用于清除分类器的内部缓存（如nominal 分类器的cache）
//	// 注意这里仅限于清除内部的cache，外部设置的比如ClassifySummary不在此列
//	@Override
//	public void cleanUp(){
//		m_cachedOldClassInstances=null;
//	}
}
