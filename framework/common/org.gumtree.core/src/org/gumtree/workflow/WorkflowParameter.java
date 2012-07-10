package org.gumtree.workflow;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WorkflowParameter {

	public String label() default "";

	public Class<?> type() default NullClass.class;

	public class NullClass {
	}

}
