package com.avaje.ebeaninternal.server.deploy;

public class DRawSqlColumnInfo {

		final String name;

		final String label;

		final String propertyName;

		final boolean scalarProperty;
		
		public DRawSqlColumnInfo(String name, String label, String propertyName, boolean scalarProperty) {
			this.name = name;
			this.label = label;
			this.propertyName = propertyName;
			this.scalarProperty = scalarProperty;
		}
		
		public String getName() {
			return name;
		}

		public String getLabel() {
			return label;
		}

		public String getPropertyName() {
			return propertyName;
		}
		
		public boolean isScalarProperty() {
			return scalarProperty;
		}

		public String toString() {
			return "name:" + name + " label:" + label + " prop:" + propertyName;
		}
	}