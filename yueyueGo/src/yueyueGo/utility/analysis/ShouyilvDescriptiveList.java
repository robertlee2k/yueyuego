package yueyueGo.utility.analysis;

import java.util.ArrayList;

import yueyueGo.utility.FormatUtility;

public class ShouyilvDescriptiveList {
	public static String ORIGINAL_DATA = "原始数据";
	protected String dataType;
	protected ArrayList<ShouyilvDescriptive> shouyilvDescriptions;
	public static final String[] HEADER = { "所属区间", "均线分组", "总机会数", "收益率平均值", "正收益数", "正收益率平均值", "负收益数", "负收益率平均值",
			"正值率" };
	public static final String[] COMPUTE_FIELDS = { "选股率", "收益率差", "正值提升率" };

	public ShouyilvDescriptiveList(String dataType) {
		this.dataType = dataType;
		this.shouyilvDescriptions = new ArrayList<ShouyilvDescriptive>();
	}

	public int size() {
		return shouyilvDescriptions.size();
	}

	public ShouyilvDescriptive getDescriptionAt(int index) {
		return shouyilvDescriptions.get(index);
	}

	public void addDescription(ShouyilvDescriptive one) {
		shouyilvDescriptions.add(one);
	}

	public void mergeDescriptionList(ShouyilvDescriptiveList list) {
		shouyilvDescriptions.addAll(list.shouyilvDescriptions);
	}

	public String toDescriptionList() {
		StringBuffer result = new StringBuffer();
		for (ShouyilvDescriptive shouyilvDescriptive : shouyilvDescriptions) {
			result.append(shouyilvDescriptive.toDescriptions());
		}
		return result.toString();
	}

	/**
	 * 将列表纵向输出
	 */
	public String convertVerticallyToCSV() {
		StringBuffer outputCSV = new StringBuffer(ShouyilvDescriptiveList.HEADER + "\r\n");
		for (ShouyilvDescriptive shouyilvDescribe : shouyilvDescriptions) {
			outputCSV.append(shouyilvDescribe.toCSVString() + "\r\n");
		}
		return outputCSV.toString();
	}

	public StringBuffer getListHeader() {
		StringBuffer header = new StringBuffer();
		header.append(dataType);
		header.append(",");
		for (int j = 0; j < HEADER.length; j++) {
			header.append(ShouyilvDescriptiveList.HEADER[j]);
			header.append(",");
		}
		if (dataType.equals(ORIGINAL_DATA) == false) {
			for (int j = 0; j < COMPUTE_FIELDS.length; j++) {
				header.append(ShouyilvDescriptiveList.COMPUTE_FIELDS[j]);
				header.append(",");
			}
		}
		return header;
	}

	private static StringBuffer getOneRowFromList(ShouyilvDescriptive last, ShouyilvDescriptive current) {
		StringBuffer content = new StringBuffer();
		content.append(current.toCSVString());

		if (last != null) { //如果有计算字段
			content.append(",");
			// 接下来处理"选股率"
			double selectedRatio = 0;
			if (last.count > 0) {
				selectedRatio = current.count / last.count;
			}
			content.append(FormatUtility.formatPercent(selectedRatio));
			content.append(",");

			// 接下来处理"收益率差"
			double shouyilvDiff = 0;
			shouyilvDiff = current.shouyilvAverage - last.shouyilvAverage;
			content.append(FormatUtility.formatPercent(shouyilvDiff));
			content.append(",");

			// 接下来处理"正值提升率"
			double liftUp = 0;
			if (last.getPositiveRatio() > 0) {
				liftUp = current.getPositiveRatio() / last.getPositiveRatio();
			}
			content.append(FormatUtility.formatPercent(liftUp));
			content.append(",");
		}

		return content;
	}

	/*
	 * 将同样大小的收益率描述列表横向合并 第一个是全市场的情况 第二个是主选股器的结果 第三个是次选股器过滤的结果
	 */
	public static String mergeHorizontallyToCSV(ShouyilvDescriptiveList all, ShouyilvDescriptiveList main,
			ShouyilvDescriptiveList secondary) {

		StringBuffer header = new StringBuffer(); // 收益率描述表头
		StringBuffer outputCSV = new StringBuffer();

		// 输出表头
		// 全市场
		header.append(all.getListHeader());
		// 主分类器
		header.append(main.getListHeader());
		// 第二分类器
		header.append(secondary.getListHeader());

		outputCSV.append(header);
		outputCSV.append("\r\n");

		// 接下来按行合并
		int dataRows = all.size();

		for (int i = 0; i < dataRows; i++) {
			// 全市场
			outputCSV.append(main.dataType);
			outputCSV.append(",");
			outputCSV.append(getOneRowFromList(null, all.getDescriptionAt(i)));
			outputCSV.append(",");

			// 主分类器
			outputCSV.append(main.dataType);
			outputCSV.append(",");
			outputCSV.append(getOneRowFromList(all.getDescriptionAt(i), main.getDescriptionAt(i)));
			outputCSV.append(",");

			// 第二分类器
			outputCSV.append(secondary.dataType);
			outputCSV.append(",");
			outputCSV.append(getOneRowFromList(main.getDescriptionAt(i), secondary.getDescriptionAt(i)));

			outputCSV.append("\r\n");
		}
		return outputCSV.toString();
	}

}
