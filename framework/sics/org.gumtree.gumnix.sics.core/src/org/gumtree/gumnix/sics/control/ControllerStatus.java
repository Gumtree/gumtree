package org.gumtree.gumnix.sics.control;

/**
 * ComponentStatus indicates the current status of the component.
 *
 * @since 1.0
 *
 */
public enum ControllerStatus {
	
		OK(2), RUNNING(1), ERROR(0);

		private ControllerStatus(int priority) {
			this.priority = priority;
		}

		/**
		 * Returns the preset priority of this status object.
		 *
		 * @return priority of the component status
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * Compares a give status to this status object.
		 *
		 * @param anotherStatus
		 *            status for comparison
		 * @return true if this status is more important than the give status;
		 *         false otherwise
		 */
		public boolean isMoreImportantThan(ControllerStatus anotherStatus) {
			return getPriority() < anotherStatus.getPriority();
		}

		private int priority;

}
