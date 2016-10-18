package yueyueGo.utility;

import yueyueGo.ArffFormat;
import yueyueGo.databeans.BaseInstance;
import yueyueGo.databeans.BaseInstances;
import yueyueGo.databeans.WekaAttribute;
import yueyueGo.databeans.WekaInstance;
import yueyueGo.databeans.WekaInstances;

public class MergeClassifyResults {
	private  double[] shouyilv_thresholds; //对于胜率优先算法的收益率筛选阀值
	private  double[] winrate_thresholds; //对于收益率优先算法的胜率筛选阀值

	public MergeClassifyResults(double[] shouyilv,
			double[] winrate) {
		this.shouyilv_thresholds = shouyilv;
		this.winrate_thresholds = winrate;
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
		public BaseInstances mergeResults(BaseInstances resultData,BaseInstances referenceData, String dataToAdd, BaseInstances left)
				throws IllegalStateException, Exception {
			System.out.println("incoming resultData size, row="+resultData.numInstances()+" column="+resultData.numAttributes());
			System.out.println("incoming referenceData size, row="+referenceData.numInstances()+" column="+referenceData.numAttributes());
			System.out.println("Left data loaded, row="+left.numInstances()+" column="+left.numAttributes());
	
		    // 创建输出结果
		    BaseInstances mergedResult = new WekaInstances(left, 0);
		    mergedResult=InstanceUtility.AddAttribute(mergedResult,ArffFormat.RESULT_PREDICTED_PROFIT, mergedResult.numAttributes());
		    mergedResult=InstanceUtility.AddAttribute(mergedResult,ArffFormat.RESULT_PREDICTED_WIN_RATE, mergedResult.numAttributes());
		    mergedResult=InstanceUtility.AddAttribute(mergedResult,ArffFormat.RESULT_SELECTED, mergedResult.numAttributes());
			
	
		    BaseInstance leftCurr;
			BaseInstance resultCurr;
			BaseInstance referenceCurr;
			WekaInstance newData;
			
			//左侧冗余信息文件属性
			WekaAttribute leftMA=(WekaAttribute)left.attribute(ArffFormat.SELECTED_AVG_LINE);
			WekaAttribute shouyilvAtt=(WekaAttribute)left.attribute(ArffFormat.SHOUYILV);	
			WekaAttribute leftBias5=(WekaAttribute)left.attribute(ArffFormat.BIAS5);
			
			//结果文件属性
		    WekaAttribute resultMA=(WekaAttribute)resultData.attribute(ArffFormat.SELECTED_AVG_LINE);	
			WekaAttribute resultBias5=(WekaAttribute)resultData.attribute(ArffFormat.BIAS5);
			WekaAttribute resultSelectedAtt=(WekaAttribute)resultData.attribute(ArffFormat.RESULT_SELECTED);
			
			//输出文件的属性
			WekaAttribute outputSelectedAtt=(WekaAttribute)mergedResult.attribute(ArffFormat.RESULT_SELECTED);
			WekaAttribute outputPredictAtt=(WekaAttribute)mergedResult.attribute(ArffFormat.RESULT_PREDICTED_PROFIT);
			WekaAttribute outputWinrateAtt=(WekaAttribute)mergedResult.attribute(ArffFormat.RESULT_PREDICTED_WIN_RATE);
	//		Attribute outputMAAtt=mergedResult.attribute(ArffFormat.SELECTED_AVG_LINE);
			
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
							referenceCurr=new WekaInstance(referenceData.numAttributes());
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
							referenceCurr=new WekaInstance(referenceData.numAttributes());
							referenceIndex=oldIndex; //这一条设为空，index恢复原状
							break;
						}
					}
					
					//接下来做冗余字段的数据校验
					if ( ArffFormat.checkSumBeforeMerge(leftCurr, resultCurr, leftMA, resultMA,leftBias5, resultBias5)) {
						newData=new WekaInstance(mergedResult.numAttributes());
						newData.setDataset(mergedResult);
						int srcStartIndex=0;
						int srcEndIndex=leftCurr.numAttributes()-1;
						int targetStartIndex=0;
						InstanceUtility.copyToNewInstance(leftCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);
	
						//根据传入的参数判断需要当前有什么，需要补充的数据是什么
						double profit;
						double winrate;
						double selected=resultCurr.value(resultSelectedAtt);
						
	
						if (dataToAdd.equals(ArffFormat.RESULT_PREDICTED_WIN_RATE)){
							//当前结果集里有什么数据
							profit=resultCurr.value(resultData.attribute(ArffFormat.RESULT_PREDICTED_PROFIT));
							//需要添加参考集里的什么数据
							winrate=referenceCurr.value(referenceData.attribute(ArffFormat.RESULT_PREDICTED_WIN_RATE));
							//当为连续分类器合并胜率时，如果参照的二分类器预期胜率小于等于0.5，则不选择该条记录?
							if (selected==1){
								
								int index;
								if (resultMA==null){ //非均线策略时默认选择THREDHOLD的第一个
									index=0;
								}else{//均线策略时按序选择THREDHOLD的第一个
									index=new Double(resultCurr.value(resultMA)).intValue();	
								}
								if (winrate<winrate_thresholds[index]){ //需要修改选股结果 
									selected=0;
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
							}// end if (selected
						}else{ 
							//当前结果集里有什么数据
							winrate=resultCurr.value(resultData.attribute(ArffFormat.RESULT_PREDICTED_WIN_RATE));
							//需要添加参考集里的什么数据
							profit=referenceCurr.value(referenceData.attribute(ArffFormat.RESULT_PREDICTED_PROFIT));
							//当为二分类器合并收益率时，如果参照的连续分类器预期收益率小于等于某阀值（0或1%）时，则不选择该条记录。
	
							
							if (selected==1){
								
								int index;
								if (resultMA==null){ //非均线策略时默认选择SHOUYILV_THREDHOLD的第一个
									index=0;
								}else{//均线策略时按序选择SHOUYILV_THREDHOLD的第一个
									index=new Double(resultCurr.value(resultMA)).intValue();	
								}
								if (profit<=shouyilv_thresholds[index]){  //需要修改选股结果
									selected=0;
									resultChanged++;
									if (shouyilvAtt!=null){
										double shouyilv=leftCurr.value(shouyilvAtt);
										changedShouyilv+=shouyilv;
										if (shouyilv<=shouyilv_thresholds[index]){
											//如果变化的实际收益率也小于阀值，说明这是一次正确的变换
											goodChangeNum++;
										}// end if shouyilv<=
									}
								}else{ //不需要修改选股结果
									finalSelected++;
								}// end if profit<=
							}// end if (selected
						}//end else of dataToAdd
	
						newData.setValue(outputPredictAtt, profit);
						newData.setValue(outputWinrateAtt, winrate);
						newData.setValue(outputSelectedAtt,selected);						
	
						mergedResult.add(newData);
						resultIndex++;
						leftIndex++;
						if (mergedResult.numInstances() % 100000 ==0){
							System.out.println("number of results processed:"+ mergedResult.numInstances());
						}
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
			System.out.println(" result changed because of reference data not matched="+resultChanged+" while good change number="+goodChangeNum);
			if (resultChanged>0){
				double goodRatio=new Double(goodChangeNum).doubleValue()/resultChanged;
				System.out.print(" good ratio="+FormatUtility.formatPercent(goodRatio));
				System.out.print(" average changed shouyilv="+FormatUtility.formatPercent(changedShouyilv/resultChanged));
			}	
			if (dataToAdd.equals(ArffFormat.RESULT_PREDICTED_WIN_RATE)){
				System.out.print(" @ winrate thredhold=");
				for (int i = 0; i < winrate_thresholds.length; i++) {
					System.out.print(" /"+FormatUtility.formatPercent(winrate_thresholds[i]));
				}
				System.out.println(" /");
			}
			else{
				System.out.print(" @ shouyilv thredhold=");
				for (int i = 0; i < shouyilv_thresholds.length; i++) {
					System.out.print(" /"+FormatUtility.formatPercent(shouyilv_thresholds[i]));
				}
				System.out.println(" /");
			}

			return mergedResult;
		}

}
