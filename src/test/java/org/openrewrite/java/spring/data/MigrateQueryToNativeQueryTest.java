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
package org.openrewrite.java.spring.data;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class MigrateQueryToNativeQueryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new MigrateQueryToNativeQuery())
          .parser(JavaParser.fromJavaVersion()
//            .classpath("spring-data-jpa", "spring-data-commons"));
            .classpathFromResources(new InMemoryExecutionContext(), "spring-data-jpa", "spring-data-commons"));
    }

    @DocumentExample
    @Test
    void queryWithNativeEqualsTrue() {
        rewriteRun(
          //language=java
          java(
            """
              import org.springframework.data.jpa.repository.Query;

              interface Test {

                  @Query("select * from foo", nativeQuery = true)
                  void customQuery();
              }
              """,
            """
              import org.springframework.data.jpa.repository.NativeQuery;

              interface Test {

                  @NativeQuery("select * from foo")
                  void customQuery();
              }
              """
          )
        );
    }

    @Test
    void queryWithNativeEqualsTrueUsingValueArg() {
        rewriteRun(
          //language=java
          java(
            """
              import org.springframework.data.jpa.repository.Query;

              interface Test {

                  @Query(value = "select * from foo", nativeQuery = true)
                  void customQuery();
              }
              """,
            """
              import org.springframework.data.jpa.repository.NativeQuery;

              interface Test {

                  @NativeQuery(value = "select * from foo")
                  void customQuery();
              }
              """
          )
        );
    }

    @Test
    void queryWithNativeEqualsTrueWithShuffledArgs() {
        rewriteRun(
          //language=java
          java(
            """
              import org.springframework.data.jpa.repository.Query;

              interface Test {

                  @Query(countQuery = "select count(*) from foo", nativeQuery = true, value = "select * from foo")
                  void customQuery();
              }
              """,
            """
              import org.springframework.data.jpa.repository.NativeQuery;

              interface Test {

                  @NativeQuery(countQuery = "select count(*) from foo", value = "select * from foo")
                  void customQuery();
              }
              """
          )
        );
    }

    @Test
    void queryWithNativeEqualsFalse() {
        rewriteRun(
          //language=java
          java(
            """
              import org.springframework.data.jpa.repository.Query;

              interface Test {

                  @Query("select * from foo", nativeQuery = false)
                  void customQuery();
              }
              """
          )
        );
    }

    @Test
    void queryWithNativeUnspecified() {
        rewriteRun(
          //language=java
          java(
            """
              import org.springframework.data.jpa.repository.Query;

              interface Test {

                  @Query("select * from foo")
                  void customQuery();
              }
              """
          )
        );
    }

}
