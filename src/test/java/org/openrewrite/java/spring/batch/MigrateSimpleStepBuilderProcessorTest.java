package org.openrewrite.java.spring.batch;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class MigrateSimpleStepBuilderProcessorTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new MigrateSimpleStepBuilderProcessor())
          .parser(JavaParser.fromJavaVersion()
            .classpathFromResources(new InMemoryExecutionContext(),
              "spring-batch-core-4.3.+",
              "spring-batch-infrastructure-4.3.+"
            ));
    }

    @Test
    @DocumentExample
    void replaceSimpleStepBuilderProcessor_FunctionParameter() {
        // language=java
        rewriteRun(
          java(
            """
              import java.util.List;
              import java.util.Function;

              import org.springframework.batch.core.Step;
              import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
              import org.springframework.batch.core.step.builder.StepBuilder;
              import org.springframework.batch.item.support.ListItemReader;

              public class MyBatchs {

                public Step step1(StepBuilderFactory stepBuilderFactory) {
                    return stepBuilderFactory.get("step1")
                            .<String, String>chunk(2)
                            .reader(new ListItemReader<>(List.of("a", "b", "c", "d")))
                            .processor(logItem())
                            .writer(items -> items.forEach(System.out::println))
                            .build();
                }

                private Function<? super String, String> logItem() {
                    return item -> {
                        System.out.println("Item: " + item);
                        return item;
                    };
                }

              }
              """
          )
        );
    }

    /**
     * Valid with Spring Batch 4.
     */
    @Test
    void replaceSimpleStepBuilderProcessor_FunctionParameter_lambda() {
        // language=java
        rewriteRun(
          java(
            """
              import java.util.List;
              import java.util.Function;

              import org.springframework.batch.core.Step;
              import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
              import org.springframework.batch.core.step.builder.StepBuilder;
              import org.springframework.batch.item.support.ListItemReader;

              public class MyBatchs {

                public Step step1(StepBuilderFactory stepBuilderFactory) {
                    return stepBuilderFactory.get("step1")
                            .<String, String>chunk(2)
                            .reader(new ListItemReader<>(List.of("a", "b", "c", "d")))
                            .processor((Function<String, String>) item -> item)
                            .writer(items -> items.forEach(System.out::println))
                            .build();
                }

              }
              """
          )
        );
    }

    /**
     * Valid with Spring Batch 5.
     */
    @Test
    void replaceSimpleStepBuilderProcessor_lambda() {
        // language=java
        rewriteRun(
          java(
            """
              import java.util.List;
              import java.util.Function;

              import org.springframework.batch.core.Step;
              import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
              import org.springframework.batch.core.step.builder.StepBuilder;
              import org.springframework.batch.item.support.ListItemReader;

              public class MyBatchs {

                public Step step1(StepBuilderFactory stepBuilderFactory) {
                    return stepBuilderFactory.get("step1")
                            .<String, String>chunk(2)
                            .reader(new ListItemReader<>(List.of("a", "b", "c", "d")))
                            .processor(item -> item)
                            .writer(items -> items.forEach(System.out::println))
                            .build();
                }

              }
              """
          )
        );
    }

    @Test
    void replaceSimpleStepBuilderProcessor_ItemProcessorParameter() {
        // language=java
        rewriteRun(
          java(
            """
              import java.util.List;

              import org.springframework.batch.core.Step;
              import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
              import org.springframework.batch.item.support.ListItemReader;
              import org.springframework.batch.item.ItemProcessor;

              public class MyBatchs {

                public Step step1(StepBuilderFactory stepBuilderFactory) {
                    return stepBuilderFactory.get("step1")
                            .<String, String>chunk(2)
                            .reader(new ListItemReader<>(List.of("a", "b", "c", "d")))
                            .processor(processItem())
                            .writer(items -> items.forEach(System.out::println))
                            .build();
                }

                private ItemProcessor<String, String> processItem() {
                    return item -> {
                        System.out.println("Item: " + item);
                        return item;
                    };
                }

              }
              """
          )
        );
    }

}
