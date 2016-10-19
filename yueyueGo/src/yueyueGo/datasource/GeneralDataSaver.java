package yueyueGo.datasource;

import java.io.IOException;

import yueyueGo.databeans.GeneralInstances;

public interface GeneralDataSaver {

	public abstract void SaveDataIntoFile(GeneralInstances dataSet,
			String fileName) throws IOException;

	public abstract void saveCSVFile(GeneralInstances data, String fileName)
			throws IOException;

}