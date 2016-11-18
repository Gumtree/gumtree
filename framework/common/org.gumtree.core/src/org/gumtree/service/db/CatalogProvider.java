package org.gumtree.service.db;

import java.util.List;

public class CatalogProvider {

	private List<String> columnNames;
	
	public CatalogProvider(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public List<String> getColumnNames(){
		return columnNames;
	}
	
	public void setColumnNames(List<String> names){
		this.columnNames = names;
	}
	
	public int getIndex(String columnName) {
		return columnName.indexOf(columnName);
	}
	
}
