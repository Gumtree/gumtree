package org.gumtree.control.model;

public class PropertyConstants {

	public final static String PROP_MESSAGE_TYPE = "type";
	public final static String PROP_UPDATE_PATH = "path";
	public final static String PROP_UPDATE_VALUE = "value";

	public final static String PROP_TYPE_UPDATE = "UPDATE";

	public enum PropertyType {
		DIM("dim"), PRIVILEGE("privilege"), TYPE("type"), VIEWER("viewer"), SICS_DEV("sicsdev"), RANK("rank");

		private PropertyType(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public static PropertyType getPropertyType(String id) {
			for (PropertyType property : values()) {
				if (property.getId().equals(id)) {
					return property;
				}
			}
			return null;
		}

		private String id;
	}

	public enum Privilege {
		READ_ONLY("READ_ONLY"), INTERNAL("internal"), MANAGER("manager"), USER("user");

		private Privilege(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public static Privilege getPrivilege(String id) {
			for (Privilege privilege : values()) {
				if (privilege.getId().equals(id)) {
					return privilege;
				}
			}
			return null;
		}

		private String id;
	}

	public enum ComponentType {
		AXIS("axis"),
		COMMAND("command"),
		COMMAND_SET("commandset"),
		DATA("data"),
		DRIVABLE("drivable"),
		GRAPH_DATA("graphdata"),
		GRAPH_SET("graphset"),
		INSTRUMENT("instrument"),
		PART("part"),
		VIEW("view"),
		SCRIPT_CONTEXT_OBJECT("sct_object");

		private ComponentType(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public static ComponentType getType(String id) {
			for (ComponentType type : values()) {
				if (type.getId().equals(id)) {
					return type;
				}
			}
			return null;
		}

		private String id;
	}

	private PropertyConstants() {
		super();
	}

}
