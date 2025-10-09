package org.openrewrite.java.spring.batch;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class MigrateItemWriterWriteInvocationTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new MigrateItemWriterWriteInvocation())
          .parser(JavaParser.fromJavaVersion()
            .classpathFromResources(new InMemoryExecutionContext(),
              "spring-batch-core-4.3.+", "spring-batch-infrastructure-4.3.+"));
    }

    @Test
    @DocumentExample
    void replaceItemWriterWriteInvocation_ArrayList() {
        // language=java
        rewriteRun(
          java(
            """
              import java.util.ArrayList;
              import org.springframework.batch.item.ItemWriter;
              import org.springframework.batch.item.support.ListItemWriter;

              class MyTest {

                  public static void main(String[] args) {
                      ListItemWriter<String> itemWriter = new ListItemWriter<>();
                      itemWriter.write(new ArrayList<>());
                  }

              }
              """,
            """
              import java.util.ArrayList;

              import org.springframework.batch.item.Chunk;
              import org.springframework.batch.item.ItemWriter;
              import org.springframework.batch.item.support.ListItemWriter;

              class MyTest {

                  public static void main(String[] args) {
                      ListItemWriter<String> itemWriter = new ListItemWriter<>();
                      itemWriter.write(new Chunk<>(new ArrayList<>()));
                  }

              }
              """
          )
        );
    }

    @Test
    @Disabled
    void replaceItemWriterWriteInvocation_nullValue() {
        // language=java
        rewriteRun(
          java(
            """
              import org.springframework.batch.item.ItemWriter;
              import org.springframework.batch.item.support.ListItemWriter;

              class MyTest {

                  public static void main(String[] args) {
                      ListItemWriter<String> itemWriter = new ListItemWriter<>();
                      itemWriter.write(null);
                  }

              }
              """,
            """
              import org.springframework.batch.item.Chunk;
              import org.springframework.batch.item.ItemWriter;
              import org.springframework.batch.item.support.ListItemWriter;

              class MyTest {

                  public static void main(String[] args) {
                      ListItemWriter<String> itemWriter = new ListItemWriter<>();
                      itemWriter.write(new Chunk<>(null));
                  }

              }
              """
          )
        );
    }

}
