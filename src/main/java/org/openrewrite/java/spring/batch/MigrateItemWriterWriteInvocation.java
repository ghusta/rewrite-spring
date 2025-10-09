/*
 * Copyright 2025 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.spring.batch;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeUtils;

import java.util.Collections;

public class MigrateItemWriterWriteInvocation extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate `ItemWriter.write()` invocations";
    }

    @Override
    public String getDescription() {
        return "In `ItemWriter` the signature of the `write()` method has changed in spring-batch 5.x.";
    }

    private static final MethodMatcher ITEM_WRITER_WRITE_MATCHER = new MethodMatcher("org.springframework.batch.item.ItemWriter write(java.util.List)", true);

    private static final String CHUNK_FQN = "org.springframework.batch.item.Chunk";

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(ITEM_WRITER_WRITE_MATCHER),
                new JavaIsoVisitor<ExecutionContext>() {

                    @Override
                    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                        if (ITEM_WRITER_WRITE_MATCHER.matches(method.getMethodType())) {
                            if (method.getArguments().isEmpty()) {
                                return method;
                            }
                            Expression firstArg = method.getArguments().get(0);
                            if (firstArg.getType() == null) {
                                return method;
                            }
                            if (TypeUtils.isOfClassType(firstArg.getType(), CHUNK_FQN)) {
                                return method;
                            }

                            maybeAddImport(CHUNK_FQN);

                            JavaTemplate argTemplate = JavaTemplate.builder("new Chunk<>(#{any()})")
                                    .javaParser(JavaParser.fromJavaVersion()
                                            .classpathFromResources(ctx, "spring-batch-core-5", "spring-batch-infrastructure-5"))
                                    .contextSensitive()
                                    .imports(CHUNK_FQN)
                                    .build();

                            Expression newArg = argTemplate.apply(
                                            getCursor(),
                                            method.getCoordinates().replace(),
                                            firstArg)
                                    .withPrefix(Space.EMPTY);

                            return method.withArguments(Collections.singletonList(newArg));
                        } else {
                            return super.visitMethodInvocation(method, ctx);
                        }
                    }

                });
    }

}
