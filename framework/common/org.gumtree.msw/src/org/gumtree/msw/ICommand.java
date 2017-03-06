package org.gumtree.msw;

public interface ICommand {
	// properties
	public RefId getId();
	public boolean isUndo();
	
	// methods
	public ICommand execute(IModel model); // returns null if unsuccessful (!!! should throw exception if unsuccessful)
	
	// if command is not reversible, execute should return IRREVERSIBLE
	public final ICommand IRREVERSIBLE = new ICommand() {
		// properties
		@Override
		public boolean isUndo() {
			throw new Error();
		}
		@Override
		public RefId getId() {
			throw new Error();
		}

		// methods
		@Override
		public ICommand execute(IModel model) {
			throw new Error();
		}
	};
}
