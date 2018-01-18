package yueyueGo.dataProcessor.dpp;

import java.util.ArrayList;

/*
 * 获取离群值定义的列表
 * 
字段名						下限	       上限        截尾含义
PE_TTM						-1.5	1.5	每股亏损/盈利是股价的1.5倍之内
HV5_gupiao					0		1	剔除过大涨幅的波动率
HV10_gupiao					0		1	剔除过大涨幅的波动率
HV20_gupiao					0		1	剔除过大涨幅的波动率
HV60_gupiao					0		1	剔除过大涨幅的波动率
HV30_gupiao					0		1	剔除过大涨幅的波动率
zhangdiefu					-0.1	0.3	单日跌停到涨30%
huanshoulv					0.03	30	放量缩量50倍
huanshoulv_preday_perc		0.03	30	放量缩量50倍
huanshoulv_pre2day_perc		0.03	30	放量缩量50倍
huanshoulv_pre3day_perc		0.03	30	放量缩量50倍
huanshoulv_totalshare_preday_perc
huanshoulv_totalshare_pre2day_perc
huanshoulv_totalshare_pre3day_perc
leijizhangdiefu5_gupiao		-0.5	1	连续跌停和连续涨停
leijizhangdiefu10_gupiao	-0.8	1	连续跌停和连续涨停
leijizhangdiefu20_gupiao	-0.8	1	连续跌停和连续涨停
leijizhangdiefu30_gupiao	-0.8	1	连续跌停和连续涨停
leijizhangdiefu60_gupiao	-0.8	1	连续跌停和连续涨停
jun_huanhoulv_bilv5_gupiao	0.03	30	放量缩量50倍
jun_huanhoulv_bilv10_gupiao	0.03	30	放量缩量50倍
jun_huanhoulv_bilv20_gupiao	0.03	30	放量缩量50倍
jun_huanhoulv_bilv30_gupiao	0.03	30	放量缩量50倍
jun_huanhoulv_bilv60_gupiao	0.03	30	放量缩量50倍
jun_huanhoulv_totalshare_bilv5_gupiao
jun_huanhoulv_totalshare_bilv10_gupiao
jun_huanhoulv_totalshare_bilv20_gupiao
jun_huanhoulv_totalshare_bilv30_gupiao
jun_huanhoulv_totalshare_bilv60_gupiao
shouyilv					-0.5	1	涨跌幅翻倍的离群值截尾
 */
public class OutlierValuesDefinition {

	protected static ArrayList<AttributeValueRange> definitions=new ArrayList<AttributeValueRange>();
	
	static{
		definitions.add(new AttributeValueRange("PE_TTM",-2,1.5));
		definitions.add(new AttributeValueRange("HV5_gupiao",0,1));
		definitions.add(new AttributeValueRange("HV10_gupiao",0,1));
		definitions.add(new AttributeValueRange("HV20_gupiao",0,1));
		definitions.add(new AttributeValueRange("HV60_gupiao",0,1));
		definitions.add(new AttributeValueRange("HV30_gupiao",0,1));
		definitions.add(new AttributeValueRange("zhangdiefu",-0.1,0.3));
		definitions.add(new AttributeValueRange("huanshoulv",0.03,30));
		definitions.add(new AttributeValueRange("huanshoulv_preday_perc",0.03,30));
		definitions.add(new AttributeValueRange("huanshoulv_pre2day_perc",0.03,30));
		definitions.add(new AttributeValueRange("huanshoulv_pre3day_perc",0.03,30));
		definitions.add(new AttributeValueRange("huanshoulv_totalshare_preday_perc",0.03,30));
		definitions.add(new AttributeValueRange("huanshoulv_totalshare_pre2day_perc",0.03,30));
		definitions.add(new AttributeValueRange("huanshoulv_totalshare_pre3day_perc",0.03,30));
		definitions.add(new AttributeValueRange("leijizhangdiefu5_gupiao",-0.5,1));
		definitions.add(new AttributeValueRange("leijizhangdiefu10_gupiao",-0.8,1));
		definitions.add(new AttributeValueRange("leijizhangdiefu20_gupiao",-0.8,1));
		definitions.add(new AttributeValueRange("leijizhangdiefu30_gupiao",-0.8,1));
		definitions.add(new AttributeValueRange("leijizhangdiefu60_gupiao",-0.8,1));
		definitions.add(new AttributeValueRange("jun_huanhoulv_bilv5_gupiao",0.03,30));
		definitions.add(new AttributeValueRange("jun_huanhoulv_bilv10_gupiao",0.03,30));
		definitions.add(new AttributeValueRange("jun_huanhoulv_bilv20_gupiao",0.03,30));
		definitions.add(new AttributeValueRange("jun_huanhoulv_bilv30_gupiao",0.03,30));
		definitions.add(new AttributeValueRange("jun_huanhoulv_bilv60_gupiao",0.03,30));
		definitions.add(new AttributeValueRange("jun_huanhoulv_totalshare_bilv5_gupiao",0.03,30));
		definitions.add(new AttributeValueRange("jun_huanhoulv_totalshare_bilv10_gupiao",0.03,30));
		definitions.add(new AttributeValueRange("jun_huanhoulv_totalshare_bilv20_gupiao",0.03,30));
		definitions.add(new AttributeValueRange("jun_huanhoulv_totalshare_bilv30_gupiao",0.03,30));
		definitions.add(new AttributeValueRange("jun_huanhoulv_totalshare_bilv60_gupiao",0.03,30));		
		definitions.add(new AttributeValueRange("shouyilv",-0.5,1));
	}
	
	public static ArrayList<AttributeValueRange> getRangeDefinitions(){
		return definitions;
	}
}
