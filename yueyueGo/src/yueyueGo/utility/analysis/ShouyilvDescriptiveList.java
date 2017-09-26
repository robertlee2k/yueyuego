package yueyueGo.utility.analysis;

import java.util.ArrayList;

public class ShouyilvDescriptiveList {

	protected String marketName;
	protected ArrayList<ShouyilvDescriptive> shouyilvDescriptions;
	public static final String[] HEADER={"所属区间","均线分组","总机会数","收益率平均值","正收益数","正收益率平均值","负收益数","负收益率平均值","正值率"};
	
	
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
			   outputCSV.append(shouyilvDescribe.toCSVString()+"\r\n");
		   }
		return outputCSV.toString();
	}

	/*
	 * 将同样大小的收益率描述列表横向合并
	 */
	public static String mergeHorizontallyToCSV(ShouyilvDescriptiveList[] shouyilvDescriptionsArray) {

		int listNumber=shouyilvDescriptionsArray.length;
		int commonFields=0; //跳过相同的部分
		int startIndex=0;
		StringBuffer outputCSV=new StringBuffer();
		
		StringBuffer headerCategory=new StringBuffer();		//在第一行将标识市场的分类输出。
		StringBuffer header=new StringBuffer();		//在第二行输出收益率描述表头

		//输出两行表头
		for (int i=0;i<listNumber;i++){
			if (i==0){
				startIndex=0;
			}else{
				startIndex=commonFields;
			}
			for (int j=startIndex;j<HEADER.length;j++){
				headerCategory.append(shouyilvDescriptionsArray[i].marketName);
				headerCategory.append(",");
				header.append(ShouyilvDescriptiveList.HEADER[j]);
				header.append(",");
			}
			
		}
		outputCSV.append(headerCategory);
		outputCSV.append("\r\n");
		outputCSV.append(header);
		outputCSV.append("\r\n");

		//接下来按行合并
		int dataRows=shouyilvDescriptionsArray[0].size();

		for (int i=0;i<dataRows;i++){
			for (int j=0;j<listNumber;j++){
			  outputCSV.append(shouyilvDescriptionsArray[j].getDescriptionAt(i).toCSVString());
			}
			outputCSV.append("\r\n");
		}
		return outputCSV.toString();
	}

}
