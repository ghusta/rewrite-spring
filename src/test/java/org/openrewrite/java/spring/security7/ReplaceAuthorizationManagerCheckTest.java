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
package org.openrewrite.java.spring.security7;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class ReplaceAuthorizationManagerCheckTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("org.openrewrite.java.spring.security7.ReplaceAuthorizationManagerCheck")
          .parser(JavaParser.fromJavaVersion()
            .classpathFromResources(new InMemoryExecutionContext(), "spring-security-core-6.+", "spring-security-web-6.+"));
    }

    @Test
    void migrateWithChange() {
        //language=java
        rewriteRun(
          java(
            """
              import java.util.function.Supplier;
              import org.springframework.security.authorization.AuthorizationManager;
              import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
              import org.springframework.security.core.Authentication;
              import org.springframework.security.authorization.AuthorizationDecision;

              public final class CommonAuthorizationManagers {

                AuthorizationManager<RequestAuthorizationContext> customAuthManager() {
                    return new AuthorizationManager<>() {
                        @Override
                        public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
                            return new AuthorizationDecision(true);
                        }
                    };
                }

              }
              """,
            """
              import java.util.function.Supplier;
              import org.springframework.security.authorization.AuthorizationManager;
              import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
              import org.springframework.security.authorization.AuthorizationResult;
              import org.springframework.security.core.Authentication;
              import org.springframework.security.authorization.AuthorizationDecision;

              public final class CommonAuthorizationManagers {

                AuthorizationManager<RequestAuthorizationContext> customAuthManager() {
                    return new AuthorizationManager<>() {
                        @Override
                        public AuthorizationResult authorize(Supplier<? extends Authentication> authentication, RequestAuthorizationContext object) {
                            return new AuthorizationDecision(true);
                        }
                    };
                }

              }
              """));
    }

    @Test
    void migrateNoChangeUsingLambda() {
        //language=java
        rewriteRun(
          java(
            """
              import org.springframework.security.authorization.AuthorizationManager;
              import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
              import org.springframework.security.authorization.AuthorizationDecision;

              public final class CommonAuthorizationManagers {

                AuthorizationManager<RequestAuthorizationContext> customAuthManager() {
                    return (authentication, object) -> new AuthorizationDecision(true);
                }

              }
              """));
    }

}
