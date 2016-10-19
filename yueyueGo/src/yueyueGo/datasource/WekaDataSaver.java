package yueyueGo.datasource;

import java.io.File;
import java.io.IOException;

import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;

public class WekaDataSaver implements GeneralDataSaver {
	/* (non-Javadoc)
	 * @see yueyueGo.datasource.GeneralDataSaver#SaveDataIntoFile(yueyueGo.databeans.GeneralInstances, java.lang.String)
	 */
	@Override
	public void SaveDataIntoFile(GeneralInstances dataSet, String fileName) throws IOException {
	
		ArffSaver saver = new ArffSaver();
		saver.setInstances(WekaInstances.convertToWekaInstances(dataSet));
		saver.setFile(new File(fileName));
		saver.writeBatch();
	
	}

	/* (non-Javadoc)
	 * @see yueyueGo.datasource.GeneralDataSaver#saveCSVFile(yueyueGo.databeans.GeneralInstances, java.lang.String)
	 */
	@Override
	public void saveCSVFile(GeneralInstances data, String fileName) throws IOException {
		CSVSaver saver = new CSVSaver();
		saver.setInstances(WekaInstances.convertToWekaInstances(data));
		saver.setFile(new File(fileName));
		saver.writeBatch();
	}

}
