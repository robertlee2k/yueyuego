package yueyueGo.utility.analysis;

import java.util.ArrayList;

public class ShouyilvDescriptiveList {

	protected String marketName;
	protected ArrayList<ShouyilvDescriptive> shouyilvDescriptions;
	public static final String[] HEADER={"所属区间","所用模型","均线分组","总机会数","收益率平均值","正收益数","正收益率平均值","负收益数","负收益率平均值","正值率"};
	
	
	public ShouyilvDescriptiveList(String marketName) {
		this.marketName = marketName;
		this.shouyilvDescriptions=new ArrayList<ShouyilvDescriptive>();
	}

	public int size(){
		return shouyilvDescriptions.size();
	}
	
	public ShouyilvDescriptive getDescriptionAt(int index){
		return shouyilvDescriptions.get(index);
	}
	
	
	public void addDescription(ShouyilvDescriptive one){
		shouyilvDescriptions.add(one);
	}
	
	public void mergeDescriptionList(ShouyilvDescriptiveList list){
		shouyilvDescriptions.addAll(list.shouyilvDescriptions);
	}
	
	public String toDescriptionList(){
		StringBuffer result=new StringBuffer();
		for (ShouyilvDescriptive shouyilvDescriptive : shouyilvDescriptions) {
			result.append(shouyilvDescriptive.toDescriptions());
		}
		return result.toString();
	}
	
	/**
	 * 将列表纵向输出
	 */
	public String convertVerticallyToCSV() {
		StringBuffer outputCSV=new StringBuffer(ShouyilvDescriptiveList.HEADER+"\r\n");
		   for (ShouyilvDescriptive shouyilvDescribe : shouyilvDescriptions) {
			   outputCSV.append(shouyilvDescribe.toString()+"\r\n");
		   }
		return outputCSV.toString();
	}

	/*
	 * 将同样大小的收益率描述列表横向合并
	 * commonFields 表示可以不输出的相同字段
	 */
	public static String mergeHorizontallyToCSV(ShouyilvDescriptiveList[] shouyilvDescriptionsArray,int commonFields) {
		int listNumber=shouyilvDescriptionsArray.length;
		int startIndex=0;
		StringBuffer outputCSV=new StringBuffer();
		
		//在第一行将标识市场的分类输出。
		for (int i=0;i<listNumber;i++){
			if (i==0){
				startIndex=0;
			}else{
				startIndex=commonFields;
			}
			for (int j=startIndex;j<HEADER.length;j++){
				outputCSV.append(shouyilvDescriptionsArray[i].marketName);
				outputCSV.append(",");
			}
		}
		outputCSV.append("\r\n");

		//在第二行输出收益率描述表头
		for (int i=0;i<listNumber;i++){
			if (i==0){
				startIndex=0;
			}else{
				startIndex=commonFields;
			}
			for (int j=startIndex;j<HEADER.length;j++){
				outputCSV.append(ShouyilvDescriptiveList.HEADER[j]);
				outputCSV.append(",");
			}
		}
		outputCSV.append("\r\n");

		//接下来按行合并
		//TODO 跳过相应字段
		for (int i=0;i<shouyilvDescriptionsArray[0].size();i++){
			for (int j=0;j<listNumber;j++){
			  outputCSV.append(shouyilvDescriptionsArray[j].getDescriptionAt(i).toString());
			}
			outputCSV.append("\r\n");
		}
		return outputCSV.toString();
	}

}
