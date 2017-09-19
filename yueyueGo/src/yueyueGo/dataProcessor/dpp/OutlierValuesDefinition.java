package yueyueGo.dataProcessor.dpp;

import java.util.ArrayList;

/*
 * 获取离群值定义的列表
 * 
字段名						下限	       上限        截尾含义
PE_LYR						-2		2	每股亏损/盈利是股价的2倍之内
PE_TTM						-2		2	每股亏损/盈利是股价的2倍之内
HV5_gupiao					0		1	剔除过大涨幅的波动率
HV10_gupiao					0		1	剔除过大涨幅的波动率
HV20_gupiao					0		1	剔除过大涨幅的波动率
HV60_gupiao					0		1	剔除过大涨幅的波动率
HV30_gupiao					0		1	剔除过大涨幅的波动率
zhangdiefu					-0.1	0.3	单日跌停到涨30%
huanshoulv					0.02	50	放量缩量50倍
huanshoulv_preday_perc		0.02	50	放量缩量50倍
huanshoulv_pre2day_perc		0.02	50	放量缩量50倍
huanshoulv_pre3day_perc		0.02	50	放量缩量50倍
leijizhangdiefu5_gupiao		-0.5	1	连续跌停和连续涨停
leijizhangdiefu10_gupiao	-1		1	连续跌停和连续涨停
leijizhangdiefu20_gupiao	-1		1	连续跌停和连续涨停
leijizhangdiefu30_gupiao	-1		1	连续跌停和连续涨停
leijizhangdiefu60_gupiao	-1		1	连续跌停和连续涨停
jun_huanhoulv_bilv5_gupiao	0.02	50	放量缩量50倍
jun_huanhoulv_bilv10_gupiao	0.02	50	放量缩量50倍
jun_huanhoulv_bilv20_gupiao	0.02	50	放量缩量50倍
jun_huanhoulv_bilv30_gupiao	0.02	50	放量缩量50倍
jun_huanhoulv_bilv60_gupiao	0.02	50	放量缩量50倍
shouyilv					-1		1	涨幅翻倍的离群值截尾
 */
public class OutlierValuesDefinition {

	protected static ArrayList<AttributeValueRange> definitions=new ArrayList<AttributeValueRange>();
	
	static{
		definitions.add(new AttributeValueRange("PE_LYR",-2,2));
		definitions.add(new AttributeValueRange("PE_TTM",-2,2));
		definitions.add(new AttributeValueRange("HV5_gupiao",0,1));
		definitions.add(new AttributeValueRange("HV10_gupiao",0,1));
		definitions.add(new AttributeValueRange("HV20_gupiao",0,1));
		definitions.add(new AttributeValueRange("HV60_gupiao",0,1));
		definitions.add(new AttributeValueRange("HV30_gupiao",0,1));
		definitions.add(new AttributeValueRange("zhangdiefu",-0.1,0.3));
		definitions.add(new AttributeValueRange("huanshoulv",0.02,50));
		definitions.add(new AttributeValueRange("huanshoulv_preday_perc",0.02,50));
		definitions.add(new AttributeValueRange("huanshoulv_pre2day_perc",0.02,50));
		definitions.add(new AttributeValueRange("huanshoulv_pre3day_perc",0.02,50));
		definitions.add(new AttributeValueRange("leijizhangdiefu5_gupiao",-0.5,1));
		definitions.add(new AttributeValueRange("leijizhangdiefu10_gupiao",-1,1));
		definitions.add(new AttributeValueRange("leijizhangdiefu20_gupiao",-1,1));
		definitions.add(new AttributeValueRange("leijizhangdiefu30_gupiao",-1,1));
		definitions.add(new AttributeValueRange("leijizhangdiefu60_gupiao",-1,1));
		definitions.add(new AttributeValueRange("jun_huanhoulv_bilv5_gupiao",0.02,50));
		definitions.add(new AttributeValueRange("jun_huanhoulv_bilv10_gupiao",0.02,50));
		definitions.add(new AttributeValueRange("jun_huanhoulv_bilv20_gupiao",0.02,50));
		definitions.add(new AttributeValueRange("jun_huanhoulv_bilv30_gupiao",0.02,50));
		definitions.add(new AttributeValueRange("jun_huanhoulv_bilv60_gupiao",0.02,50));
		definitions.add(new AttributeValueRange("shouyilv",-1,1));
	}
	
	public static ArrayList<AttributeValueRange> getRangeDefinitions(){
		return definitions;
	}
}
