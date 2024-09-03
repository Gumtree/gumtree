package org.gumtree.control.model;

public class PropertyConstants {

	public final static String PROP_COMMAND_CMD = "cmd";
	public final static String PROP_COMMAND_FLAG = "flag";
	public final static String PROP_COMMAND_REPLY = "reply";
	public final static String PROP_COMMAND_FINAL = "final";
	public final static String PROP_COMMAND_TEXT = "text";
	public final static String PROP_COMMAND_TRANS = "trans";
	public static final String PROP_COMMAND_INTERRUPT = "interrupt";
	
	
	public final static String PROP_UPDATE_TYPE = "type";
	public final static String PROP_UPDATE_NAME = "name";
	public final static String PROP_UPDATE_VALUE = "value";
	
	public final static String PROP_UPDATE_SEQ = "seq";
	public final static String PROP_UPDATE_TS = "ts";

//	public final static String PROP_BATCH_NAME = "batch_name";
//	public final static String PROP_BATCH_RANGE = "batch_range";
	public final static String PROP_BATCH_TEXT = "batch_text";

	public final static String PROP_BATCH_START = "STARTED";
	public final static String PROP_BATCH_RANGE = "RANGE";
	public final static String PROP_BATCH_FINISH = "FINISH";
	
//	public final static String PROP_TYPE_UPDATE = "UPDATE";
//	public final static String PROP_TYPE_STATE = "STATE";
//	public final static String PROP_TYPE_STATUS = "STATUS";

	public enum FlagType{
		OK("OK"), ERROR("ERROR");
		
		private FlagType(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public static FlagType parseString(String id) {
			for (FlagType type : values()) {
				if (type.getId().equalsIgnoreCase(id)) {
					return type;
				}
			}
			return null;
		}

		private String id;
	}
	
	public enum MessageType{
		VALUE("VALUE"), STATE("STATE"), STATUS("STATUS"), BATCH("BATCH");
		
		private MessageType(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public static MessageType parseString(String id) {
			for (MessageType type : values()) {
				if (type.getId().equalsIgnoreCase(id)) {
					return type;
				}
			}
			return null;
		}

		private String id;
	}
	
	
	public enum ControllerState{
		BUSY("BUSY"), IDLE("IDLE"), ERROR("ERROR"), STARTED("STARTED"), FINISH("FINISH");
		
		private ControllerState(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public static ControllerState getState(String id) {
//			for (ControllerState state : values()) {
//				if (state.getId().equals(id)) {
//					return state;
//				}
//			}
			if (id != null && (id.equalsIgnoreCase(BUSY.id) || id.equalsIgnoreCase(STARTED.id))) {
				return BUSY;
			}
			if (id != null && (id.equalsIgnoreCase(IDLE.id) || id.equalsIgnoreCase(FINISH.id))) {
				return IDLE;
			}
			return null;
		}

		private String id;
	}
	
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
