package org.gumtree.control.model;

/**
 * ComponentStatus indicates the current status of the component.
 *
 * @since 1.0
 *
 */
public enum ModelStatus {
	
		OK(2), RUNNING(1), ERROR(0);

		private ModelStatus(int priority) {
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
		public boolean isMoreImportantThan(ModelStatus anotherStatus) {
			return getPriority() < anotherStatus.getPriority();
		}

		private int priority;

}
