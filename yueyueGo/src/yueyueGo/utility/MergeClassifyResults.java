package yueyueGo.utility;

import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.DataInstance;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.modelPredict.ModelPredictor;
import yueyueGo.utility.modelPredict.PredictResults;


public class MergeClassifyResults {
	private  String m_policy_group;

	public MergeClassifyResults(String a_policy_group) {
		this.m_policy_group=a_policy_group;
	}

		/**
		 * @param resultData
		 * @param referenceData
		 * @param dataToAdd
		 * @param left
		 * @return
		 * @throws IllegalStateException
		 * @throws Exception
		 */
		public GeneralInstances mergeResults(GeneralInstances resultData,GeneralInstances referenceData, String dataToAdd, GeneralInstances left)
				throws IllegalStateException, Exception {
			System.out.println("incoming resultData size, row="+resultData.numInstances()+" column="+resultData.numAttributes());
			System.out.println("incoming referenceData size, row="+referenceData.numInstances()+" column="+referenceData.numAttributes());
			System.out.println("Left data loaded, row="+left.numInstances()+" column="+left.numAttributes());
	
		    // 创建输出结果
		    GeneralInstances mergedResult = new DataInstances(left, 0);
		    BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(mergedResult);
		    mergedResult=instanceProcessor.AddAttribute(mergedResult,PredictResults.RESULT_PREDICTED_PROFIT, mergedResult.numAttributes());
		    mergedResult=instanceProcessor.AddAttribute(mergedResult,PredictResults.RESULT_PREDICTED_WIN_RATE, mergedResult.numAttributes());
		    mergedResult=instanceProcessor.AddAttribute(mergedResult,PredictResults.RESULT_SELECTED, mergedResult.numAttributes());
			
	
		    GeneralInstance leftCurr;
			GeneralInstance resultCurr;
			GeneralInstance referenceCurr;
			DataInstance newData;
			
			//左侧冗余信息文件属性
			GeneralAttribute leftMA=left.attribute(m_policy_group);//ArffFormat.SELECTED_AVG_LINE);
			GeneralAttribute shouyilvAtt=left.attribute(ArffFormat.SHOUYILV);	
			GeneralAttribute leftBias5=left.attribute(ArffFormat.BIAS5);
			
			//结果文件属性
			GeneralAttribute resultMA=resultData.attribute(m_policy_group);//ArffFormat.SELECTED_AVG_LINE);	
			GeneralAttribute resultBias5=resultData.attribute(ArffFormat.BIAS5);
			GeneralAttribute resultSelectedAtt=resultData.attribute(PredictResults.RESULT_SELECTED);
			
			//输出文件的属性
			GeneralAttribute outputSelectedAtt=mergedResult.attribute(PredictResults.RESULT_SELECTED);
			GeneralAttribute outputPredictAtt=mergedResult.attribute(PredictResults.RESULT_PREDICTED_PROFIT);
			GeneralAttribute outputWinrateAtt=mergedResult.attribute(PredictResults.RESULT_PREDICTED_WIN_RATE);

			
			//传入的结果集result不是排序的,而left的数据是按tradeDate日期排序的， 所以都先按ID排序。
			left.sort(ArffFormat.ID_POSITION-1);
			resultData.sort(ArffFormat.ID_POSITION-1);
			referenceData.sort(ArffFormat.ID_POSITION-1);
			
			double idInResults=0;
			double idInLeft=0;
			double idInReference=0;
			int resultIndex=0;
			int leftIndex=0;
			int referenceIndex=0;
			int referenceDataNum=referenceData.numInstances();
			
			//以下变量是为合并时修改选择结果而设
			int finalSelected=0;
			int resultChanged=0;
			int goodChangeNum=0;
			double changedShouyilv=0;
	
			while (leftIndex<left.numInstances() && resultIndex<resultData.numInstances()){				
				resultCurr=resultData.instance(resultIndex);
				leftCurr=left.instance(leftIndex);
				idInResults=resultCurr.value(0);
				idInLeft=leftCurr.value(0);
				if (idInLeft<idInResults){ // 如果左边有未匹配的数据，这是正常的，因为left数据是从2005年开始的全量
					leftIndex++;
					continue;
				}else if (idInLeft>idInResults){ // 如果右边result有未匹配的数据，这个不大正常，需要输出
					System.out.println("!!!unmatched result===="+ resultCurr.toString());	
					System.out.println("!!!current left   ====="+ leftCurr.toString());
					resultIndex++;
					continue;
				}else if (idInLeft==idInResults ){//找到相同ID的记录了
					//去reference数据里查找相应的ID记录
					referenceCurr=referenceData.instance(referenceIndex);
					idInReference=referenceCurr.value(0);
	
					//这段代码是用于应对reference的数据与result的数据不一致情形的。
					//reference数据也是按ID排序的，所以可以按序查找
					int oldIndex=referenceIndex;//暂存一下
					while (idInReference<idInResults ){ 
						if (referenceIndex<referenceDataNum-1){
							referenceIndex++;
							referenceCurr=referenceData.instance(referenceIndex);
							idInReference=referenceCurr.value(0);
						}else { //当前ID比result的ID小，需要向后找，但向后找到最后一条也没找到
							referenceCurr=new DataInstance(referenceData.numAttributes());
							referenceIndex=oldIndex; //这一条设为空，index恢复原状
							break;
						}
					}
					while (idInReference>idInResults ){
						if (referenceIndex>0){
							referenceIndex--;
							referenceCurr=referenceData.instance(referenceIndex);
							idInReference=referenceCurr.value(0);
						}else {  //当前ID比result的ID大，需要向前找，但向前找到第一条也没找到
							referenceCurr=new DataInstance(referenceData.numAttributes());
							referenceIndex=oldIndex; //这一条设为空，index恢复原状
							break;
						}
					}
					
					//接下来做冗余字段的数据校验
					if ( ArffFormat.checkSumBeforeMerge(leftCurr, resultCurr, leftMA, resultMA,leftBias5, resultBias5)) {
						newData=new DataInstance(mergedResult.numAttributes());
						newData.setDataset(mergedResult);
						int srcStartIndex=0;
						int srcEndIndex=leftCurr.numAttributes()-1;
						int targetStartIndex=0;
						BaseInstanceProcessor.copyToNewInstance(leftCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);
	
						//根据传入的参数判断需要当前有什么，需要补充的数据是什么
						double profit;
						double winrate;
						if (dataToAdd.equals(PredictResults.RESULT_PREDICTED_WIN_RATE)){
							//当前结果集里有什么数据
							profit=resultCurr.value(resultData.attribute(PredictResults.RESULT_PREDICTED_PROFIT));
							//需要考虑参考结果集里的数据
							winrate=referenceCurr.value(referenceData.attribute(PredictResults.RESULT_PREDICTED_WIN_RATE));
						}else{
							//当前结果集里有什么数据
							winrate=resultCurr.value(resultData.attribute(PredictResults.RESULT_PREDICTED_WIN_RATE));
							//需要添加参考集里的什么数据
							profit=referenceCurr.value(referenceData.attribute(PredictResults.RESULT_PREDICTED_PROFIT));							
						}
						
						//当前结果集的选股结果
						double selected=resultCurr.value(resultSelectedAtt);
						//参考结果的选股结果（-1.0的排除）
						double referenceSelected=referenceCurr.value(referenceData.attribute(PredictResults.RESULT_SELECTED));
						if (selected==ModelPredictor.VALUE_SELECTED){
							//当合并数据时，如果参照的二分类器的选择值为-1 则不选择该条记录
							if (referenceSelected==ModelPredictor.VALUE_NEVER_SELECT){
								selected=ModelPredictor.VALUE_NOT_SURE;
								resultChanged++;
								if (shouyilvAtt!=null){
									double shouyilv=leftCurr.value(shouyilvAtt);
									changedShouyilv+=shouyilv;
									if (shouyilv<=0){
										//如果变化的实际收益率小于0，说明这是一次正确的变换
										goodChangeNum++;
									}// end if shouyilv<=
								}
							}else{ //不需要修改选股结果
								finalSelected++;
							}
						}else if (selected==ModelPredictor.VALUE_NEVER_SELECT){
							//这个是因为要兼容交易程序（只接受0和1两个值，不接受-1）
							selected=ModelPredictor.VALUE_NOT_SURE;
						}
						
						newData.setValue(outputPredictAtt, profit);
						newData.setValue(outputWinrateAtt, winrate);
						newData.setValue(outputSelectedAtt,selected);						
	
						mergedResult.add(newData);
						resultIndex++;
						leftIndex++;

					}else {
						throw new Exception("data value in header data and result data does not equal left="+leftCurr.toString()+" /while result= "+resultCurr.toString());
					}// end else of ArffFormat.checkSumBeforeMerge
				}// end else if (idInLeft==idInResults )
			}// end left processed
			if (mergedResult.numInstances()!=resultData.numInstances()){
				System.err.println("------Attention!!! not all data in result have been processed , processed= "+mergedResult.numInstances()+" ,while total result="+resultData.numInstances());
			}else {
				System.out.println("number of results merged and processed: "+ mergedResult.numInstances());
			}
			System.out.println("###### Finally selected count="+finalSelected+ "  ######");
//			System.out.println(EvaluationConfDefinition.showMergeParameters());
			System.out.println(" result changed because of reference data not matched="+resultChanged+" while good change number="+goodChangeNum);
			if (resultChanged>0){
				double goodRatio=new Double(goodChangeNum).doubleValue()/resultChanged;
				System.out.print(" good ratio="+FormatUtility.formatPercent(goodRatio));
				System.out.println(" average changed shouyilv="+FormatUtility.formatPercent(changedShouyilv/resultChanged));
			}	

			return mergedResult;
		}

		
	
}
