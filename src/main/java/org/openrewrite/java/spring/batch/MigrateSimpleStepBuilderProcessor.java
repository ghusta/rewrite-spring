package org.openrewrite.java.spring.batch;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;

public class MigrateSimpleStepBuilderProcessor extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate `SimpleStepBuilder.processor()`";
    }

    @Override
    public String getDescription() {
        return "When `SimpleStepBuilder.processor()` uses a `java.util.Function`, it should be removed for Spring Batch 5. "
                + "Only using parameter of type `ItemProcessor` is allowed as of Spring Batch 5.";
    }

    private static final MethodMatcher SIMPLE_STEP_BUILDER_PROCESSOR_FUNCTION_MATCHER = new MethodMatcher("org.springframework.batch.core.step.builder.SimpleStepBuilder processor(java.util.Function)", true);
    private static final MethodMatcher SIMPLE_STEP_BUILDER_PROCESSOR_ITEM_PROCESSOR_MATCHER = new MethodMatcher("org.springframework.batch.core.step.builder.SimpleStepBuilder processor(org.springframework.batch.item.ItemProcessor)", true);

    private static final String FUNCTION_FQN = "java.util.Function";
    private static final String ITEM_PROCESSOR_FQN = "org.springframework.batch.item.ItemProcessor";

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(SIMPLE_STEP_BUILDER_PROCESSOR_FUNCTION_MATCHER),
                new JavaIsoVisitor<ExecutionContext>() {

                    @Override
                    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                        if (SIMPLE_STEP_BUILDER_PROCESSOR_FUNCTION_MATCHER.matches(method.getMethodType())) {

                            System.out.println("Method: " + method);
                            System.out.println("Method type: " + method.getMethodType());
                            System.out.println("*** SHOULD BE REPLACED ! ***");

                            return method;
                        } else {
                            return super.visitMethodInvocation(method, ctx);
                        }
                    }

                }
        );
    }
}
