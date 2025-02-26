package org.jvnet.hudson.test.junit.jupiter;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.runner.Description;
import org.jvnet.hudson.test.JenkinsRecipe;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

/**
 * Provides JUnit 5 compatibility for {@link JenkinsRule}.
 */
class JUnit5JenkinsRule extends JenkinsRule {
    private final ParameterContext context;
    private final ExtensionContext extensionContext;

    JUnit5JenkinsRule(@NonNull ParameterContext context, @NonNull ExtensionContext extensionContext) {
        this.context = context;
        this.extensionContext = extensionContext;
        this.testDescription = Description.createTestDescription(
                extensionContext.getTestClass().map(Class::getName).orElse(null),
                extensionContext.getTestMethod().map(Method::getName).orElse(null));
    }

    @Override
    public void recipe() throws Exception {
        JenkinsRecipe jenkinsRecipe =
                context.findAnnotation(JenkinsRecipe.class).orElse(null);
        if (jenkinsRecipe != null) {
            @SuppressWarnings("unchecked")
            final JenkinsRecipe.Runner<JenkinsRecipe> runner = (JenkinsRecipe.Runner<JenkinsRecipe>)
                    jenkinsRecipe.value().getDeclaredConstructor().newInstance();
            recipes.add(runner);
            tearDowns.add(() -> runner.tearDown(this, jenkinsRecipe));
        }

        Method testMethod = extensionContext.getTestMethod().orElse(null);
        if (testMethod != null) {
            LocalData localData = testMethod.getAnnotation(LocalData.class);
            if (localData != null) {
                final JenkinsRecipe.Runner<LocalData> runner = new LocalData.RuleRunnerImpl();
                recipes.add(runner);
                runner.setup(this, localData);
                tearDowns.add(() -> runner.tearDown(this, localData));
            }
        }
    }
}
