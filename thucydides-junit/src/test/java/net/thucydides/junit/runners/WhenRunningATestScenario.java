package net.thucydides.junit.runners;

import net.thucydides.core.guice.Injectors;
import net.thucydides.core.model.Story;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestResult;
import net.thucydides.core.model.TestStep;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.core.webdriver.SystemPropertiesConfiguration;
import net.thucydides.core.webdriver.ThucydidesWebdriverManager;
import net.thucydides.core.webdriver.WebDriverFactory;
import net.thucydides.core.webdriver.WebdriverInstanceFactory;
import net.thucydides.core.webdriver.WebdriverManager;
import net.thucydides.junit.rules.DisableThucydidesHistoryRule;
import net.thucydides.junit.rules.QuietThucydidesLoggingRule;
import net.thucydides.samples.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class WhenRunningATestScenario extends AbstractTestStepRunnerTest {

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    WebdriverInstanceFactory webdriverInstanceFactory;

    @Mock
    FirefoxDriver firefoxDriver;

    EnvironmentVariables environmentVariables;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public QuietThucydidesLoggingRule quietThucydidesLoggingRule = new QuietThucydidesLoggingRule();

    @Rule
    public DisableThucydidesHistoryRule disableThucydidesHistoryRule = new DisableThucydidesHistoryRule();

    WebDriverFactory webDriverFactory;

    @Before
    public void createATestableDriverFactory() throws Exception {
        
        MockitoAnnotations.initMocks(this);

        webdriverInstanceFactory = new WebdriverInstanceFactory() {
            @Override
            public WebDriver newInstanceOf(Class<? extends WebDriver> webdriverClass, FirefoxProfile profile) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
                return firefoxDriver;
            }

            @Override
            public WebDriver newInstanceOf(Class<? extends WebDriver> webdriverClass) throws IllegalAccessException, InstantiationException {
                return firefoxDriver;
            }
        };

        environmentVariables = Injectors.getInjector().getInstance(EnvironmentVariables.class);
        webDriverFactory = new WebDriverFactory(webdriverInstanceFactory, environmentVariables);
        StepEventBus.getEventBus().clear();

    }

    @Test
    public void the_test_can_specify_a_different_driver() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SamplePassingScenarioUsingHtmlUnit.class, webDriverFactory);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
    }

    @Test
    public void the_test_can_specify_a_diffrent_driver_for_an_individual_test() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SamplePassingScenarioUsingHtmlUnitForOneTest.class, webDriverFactory);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
    }

    @Test
    public void the_test_runner_records_the_steps_as_they_are_executed() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SamplePassingScenario.class, webDriverFactory);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
        TestOutcome testOutcome1 = executedSteps.get(0);
        TestOutcome testOutcome2 = executedSteps.get(1);
        TestOutcome testOutcome3 = executedSteps.get(2);

        assertThat(testOutcome1.getTitle(), is("Happy day scenario"));
        assertThat(testOutcome1.getMethodName(), is("happy_day_scenario"));
        assertThat(testOutcome1.getTestSteps().size(), is(4));

        assertThat(testOutcome2.getTitle(), is("Edge case 1"));
        assertThat(testOutcome2.getMethodName(), is("edge_case_1"));
        assertThat(testOutcome2.getTestSteps().size(), is(3));

        assertThat(testOutcome3.getTitle(), is("Edge case 2"));
        assertThat(testOutcome3.getMethodName(), is("edge_case_2"));
        assertThat(testOutcome3.getTestSteps().size(), is(2));
    }

    @Test
    public void the_test_runner_stores_state_between_steps() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SampleScenarioWithStateVariables.class, webDriverFactory);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
        TestOutcome testOutcome1 = executedSteps.get(0);
        TestOutcome testOutcome2 = executedSteps.get(1);
        TestOutcome testOutcome3 = executedSteps.get(2);

        assertThat(testOutcome1.getResult(), is(TestResult.SUCCESS));
        assertThat(testOutcome2.getResult(), is(TestResult.SUCCESS));
        assertThat(testOutcome3.getResult(), is(TestResult.PENDING));
    }

    @Test
    public void private_annotated_fields_should_be_allowed() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SamplePassingScenarioWithPrivateFields.class, webDriverFactory);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
        TestOutcome testOutcome1 = executedSteps.get(0);
        TestOutcome testOutcome2 = executedSteps.get(1);
        TestOutcome testOutcome3 = executedSteps.get(2);

        assertThat(testOutcome1.getTitle(), is("Happy day scenario"));
        assertThat(testOutcome1.getMethodName(), is("happy_day_scenario"));
        assertThat(testOutcome1.getTestSteps().size(), is(4));

        assertThat(testOutcome2.getTitle(), is("Edge case 1"));
        assertThat(testOutcome2.getMethodName(), is("edge_case_1"));
        assertThat(testOutcome2.getTestSteps().size(), is(3));

        assertThat(testOutcome3.getTitle(), is("Edge case 2"));
        assertThat(testOutcome3.getMethodName(), is("edge_case_2"));
        assertThat(testOutcome3.getTestSteps().size(), is(2));
    }

    @Test
    public void annotated_fields_should_be_allowed_in_parent_classes() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SamplePassingScenarioWithFieldsInParent.class, webDriverFactory);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
        TestOutcome testOutcome1 = executedSteps.get(0);
        TestOutcome testOutcome2 = executedSteps.get(1);
        TestOutcome testOutcome3 = executedSteps.get(2);

        assertThat(testOutcome1.getTitle(), is("Happy day scenario"));
        assertThat(testOutcome1.getMethodName(), is("happy_day_scenario"));
        assertThat(testOutcome1.getTestSteps().size(), is(4));

        assertThat(testOutcome2.getTitle(), is("Edge case 1"));
        assertThat(testOutcome2.getMethodName(), is("edge_case_1"));
        assertThat(testOutcome2.getTestSteps().size(), is(3));

        assertThat(testOutcome3.getTitle(), is("Edge case 2"));
        assertThat(testOutcome3.getMethodName(), is("edge_case_2"));
        assertThat(testOutcome3.getTestSteps().size(), is(2));
    }
    @Test
    public void tests_marked_as_pending_should_be_pending() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SamplePassingScenarioWithPendingTests.class, webDriverFactory);
//        runner.setWebDriverFactory(webDriverFactory);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
        TestOutcome testOutcome1 = executedSteps.get(0);
        TestOutcome testOutcome2 = executedSteps.get(1);
        TestOutcome testOutcome3 = executedSteps.get(2);

        assertThat(testOutcome1.getResult(), is(TestResult.SUCCESS));
        assertThat(testOutcome2.getResult(), is(TestResult.PENDING));
        assertThat(testOutcome3.getResult(), is(TestResult.PENDING));
    }

    @Test
    public void tests_marked_as_ignored_should_be_skipped() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SamplePassingScenarioWithIgnoredTests.class, webDriverFactory);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
        TestOutcome testOutcome1 = executedSteps.get(0);
        TestOutcome testOutcome2 = executedSteps.get(1);
        TestOutcome testOutcome3 = executedSteps.get(2);

        assertThat(testOutcome1.getResult(), is(TestResult.SUCCESS));
        assertThat(testOutcome2.getResult(), is(TestResult.IGNORED));
        assertThat(testOutcome3.getResult(), is(TestResult.IGNORED));
    }

    @Test
    public void non_tests_with_no_steps_should_be_marked_as_pending() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SamplePassingScenarioWithEmptyTests.class, webDriverFactory);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
        TestOutcome testOutcome1 = executedSteps.get(0);
        TestOutcome testOutcome2 = executedSteps.get(1);
        TestOutcome testOutcome3 = executedSteps.get(2);

        assertThat(testOutcome1.getResult(), is(TestResult.SUCCESS));
        assertThat(testOutcome2.getResult(), is(TestResult.PENDING));
        assertThat(testOutcome3.getResult(), is(TestResult.PENDING));
    }


    @Test
    public void tests_should_be_run_after_an_assertion_error() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(MockOpenStaticDemoPageWithFailureSample.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
        TestOutcome testOutcome1 = executedSteps.get(0);
        TestOutcome testOutcome2 = executedSteps.get(1);
        TestOutcome testOutcome3 = executedSteps.get(2);

        assertThat(testOutcome1.getResult(), is(TestResult.FAILURE));
        assertThat(testOutcome2.getResult(), is(TestResult.SUCCESS));
        assertThat(testOutcome3.getResult(), is(TestResult.SUCCESS));
    }

    @Test
    public void tests_should_be_run_after_a_webdriver_error() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(MockOpenPageWithWebdriverErrorSample.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
        TestOutcome testOutcome1 = executedSteps.get(0);
        TestOutcome testOutcome2 = executedSteps.get(1);
        TestOutcome testOutcome3 = executedSteps.get(2);

        assertThat(testOutcome1.getResult(), is(TestResult.FAILURE));
        assertThat(testOutcome1.getTestSteps().get(1).getResult(), is(TestResult.FAILURE));
        assertThat(testOutcome2.getResult(), is(TestResult.SUCCESS));
        assertThat(testOutcome3.getResult(), is(TestResult.SUCCESS));
    }

    @Test
    public void webdriver_error_should_be_recorded_when_at_the_end_of_the_test() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(MockOpenPageWithWebdriverErrorAtTheEndSample.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(3));
        TestOutcome testOutcome1 = executedSteps.get(0);
        TestOutcome testOutcome2 = executedSteps.get(1);
        TestOutcome testOutcome3 = executedSteps.get(2);

        assertThat(testOutcome1.getResult(), is(TestResult.SUCCESS));
        assertThat(testOutcome2.getResult(), is(TestResult.SUCCESS));
        assertThat(testOutcome3.getResult(), is(TestResult.FAILURE));
        assertThat(testOutcome3.getTestSteps().size(), is(3));
        assertThat(testOutcome3.getTestSteps().get(2).getResult(), is(TestResult.FAILURE));
    }


    @Test
    public void failing_tests_with_no_steps_should_still_record_the_error() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SampleFailingScenarioWithEmptyTests.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(1));
        assertThat(executedSteps.get(0).getResult(), is(TestResult.FAILURE));
        assertThat(executedSteps.get(0).getTestFailureCause().getMessage(), is("Failure without any steps."));
    }

    @Test
    public void the_test_runner_skips_any_tests_after_a_failure() throws Exception {

        ThucydidesRunner runner = new ThucydidesRunner(SingleHtmlUnitTestScenario.class, webDriverFactory);

        runner.run(new RunNotifier());
        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        TestOutcome testOutcome = executedScenarios.get(0);

        List<TestStep> steps = testOutcome.getTestSteps();
        assertThat(steps.size(), is(6));
        assertThat(steps.get(0).isSuccessful(), is(true));
        assertThat(steps.get(1).isSuccessful(), is(true));
        assertThat(steps.get(2).isIgnored(), is(true));
        assertThat(steps.get(3).isSuccessful(), is(true));
        assertThat(steps.get(4).isFailure(), is(true));
        assertThat(steps.get(5).isSkipped(), is(true));
    }

    @Test
    public void the_test_runner_skips_any_ignored_tests() throws Exception {

        ThucydidesRunner runner = new ThucydidesRunner(TestIgnoredScenario.class);

        runner.run(new RunNotifier());
        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        TestOutcome testOutcome = executedScenarios.get(0);
        
        assertThat(testOutcome.getResult(), is(TestResult.IGNORED));
        assertThat(testOutcome.getTestSteps().size(), is(0));
    }



    @Test
    public void the_test_runner_skips_any_tests_after_a_webdriver_error() throws Exception {

        ThucydidesRunner runner = new ThucydidesRunner(SampleNoSuchElementExceptionScenario.class);

        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        TestOutcome testOutcome = executedScenarios.get(0);

        List<TestStep> steps = testOutcome.getTestSteps();
        assertThat(steps.size(), is(5));
        assertThat(steps.get(0).isSuccessful(), is(true));
        assertThat(steps.get(1).isIgnored(), is(true));
        assertThat(steps.get(2).isSuccessful(), is(true));
        assertThat(steps.get(3).isFailure(), is(true));
        assertThat(steps.get(4).isSkipped(), is(true));
    }


    @Test
    public void when_a_test_fails_the_message_is_recorded_in_the_test_step() throws Exception {

        ThucydidesRunner runner = new ThucydidesRunner(SingleHtmlUnitTestScenario.class, webDriverFactory);

        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        TestOutcome testOutcome = executedScenarios.get(0);

        List<TestStep> steps = testOutcome.getTestSteps();
        TestStep failingStep = (TestStep) steps.get(4);
        assertThat(failingStep.getErrorMessage(), containsString("Expected: is <2>"));
    }

    @Test
    public void when_a_test_fails_the_exception_is_recorded_in_the_test_step() throws Exception {

        ThucydidesRunner runner = new ThucydidesRunner(SingleHtmlUnitTestScenario.class, webDriverFactory);

        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        TestOutcome testOutcome = executedScenarios.get(0);

        List<TestStep> steps = testOutcome.getTestSteps();
        TestStep failingStep = steps.get(4);
        assertThat(failingStep.getException().getClass().toString(), containsString("StepFailureException"));
    }

    @Test
    public void when_a_test_throws_a_webdriver_exception_it_is_recorded_in_the_test_step() throws Exception {

        ThucydidesRunner runner = new ThucydidesRunner(SingleTestScenarioWithWebdriverException.class);

        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        TestOutcome testOutcome = executedScenarios.get(0);

        List<TestStep> steps = testOutcome.getTestSteps();
        TestStep failingStep = (TestStep) steps.get(3);
        assertThat(failingStep.getException().getClass().toString(), containsString("StepFailureException"));
    }

    @Test
    public void when_a_test_throws_a_runtime_exception_it_is_recorded_in_the_test_step() throws Exception {

        ThucydidesRunner runner = new ThucydidesRunner(SingleTestScenarioWithRuntimeException.class);

        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        TestOutcome testOutcome = executedScenarios.get(0);

        List<TestStep> steps = testOutcome.getTestSteps();
        TestStep failingStep = steps.get(3);
        assertThat(failingStep.getException().getClass().toString(), containsString("StepFailureException"));
    }

    @Test
    public void the_test_runner_initializes_the_steps_object() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SamplePassingScenario.class, webDriverFactory);
        runner.run(new RunNotifier());


    }

    @Test
    public void the_test_runner_records_the_name_of_the_test_scenario() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SuccessfulSingleTestScenario.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        assertThat(executedScenarios.size(), greaterThan(0));

        TestOutcome testOutcome = executedScenarios.get(0);

        assertThat(testOutcome.getTitle(), is("Happy day scenario"));
    }

    @Test
    public void the_test_runner_records_each_step_of_the_test_scenario() throws InitializationError {
        ThucydidesRunner runner = new ThucydidesRunner(SamplePassingScenario.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        assertThat(executedScenarios.size(), is(3));

        TestOutcome testOutcome = executedScenarios.get(0);
        assertThat(testOutcome.getTestSteps().size(), is(4));

        TestOutcome testOutcome2 = executedScenarios.get(1);
        assertThat(testOutcome2.getTestSteps().size(), is(3));

        TestOutcome testOutcome3 = executedScenarios.get(2);
        assertThat(testOutcome3.getTestSteps().size(), is(2));
    }

    @Test
    public void the_test_runner_distinguishes_between_ignored_and_skipped_steps() throws InitializationError {
        ThucydidesRunner runner = new ThucydidesRunner(SingleHtmlUnitTestScenario.class, webDriverFactory);

        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        assertThat(executedScenarios.size(), is(1));
        TestOutcome testOutcome = executedScenarios.get(0);
        TestStep ignored = testOutcome.getTestSteps().get(2);
        TestStep skipped = testOutcome.getTestSteps().get(5);

        assertThat(ignored.getResult(), is(TestResult.IGNORED));
        assertThat(skipped.getResult(), is(TestResult.SKIPPED));
    }

    @Test
    public void the_test_runner_executes_steps_with_parameters() throws InitializationError {
        ThucydidesRunner runner = new ThucydidesRunner(SingleHtmlUnitTestScenario.class, webDriverFactory);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        assertThat(executedScenarios.size(), greaterThan(0));
        TestOutcome testOutcome = executedScenarios.get(0);
        TestStep ignored = testOutcome.getTestSteps().get(2);
        TestStep skipped = testOutcome.getTestSteps().get(5);

        assertThat(ignored.getResult(), is(TestResult.IGNORED));
        assertThat(skipped.getResult(), is(TestResult.SKIPPED));
    }

    @Test
    public void the_test_runner_executes_tests_in_groups() throws InitializationError {
        ThucydidesRunner runner = new ThucydidesRunner(TestScenarioWithGroups.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        assertThat(executedScenarios.size(), is(1));
        List<TestStep> testSteps = executedScenarios.get(0).getTestSteps();
        assertThat(testSteps.size(), is(3));
    }

    @Test
    public void the_test_runner_records_an_acceptance_test_result_for_each_test() throws InitializationError {
        ThucydidesRunner runner = new ThucydidesRunner(SamplePassingScenario.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();
        assertThat(executedScenarios.size(), is(3));
    }

    @Test
    public void the_test_runner_derives_the_user_story_from_the_test_case_class() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SuccessfulSingleTestScenario.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();

        TestOutcome testOutcome = executedScenarios.get(0);
        Story userStory = testOutcome.getUserStory();

        assertThat(userStory.getName(), is("Successful single test scenario"));
    }

    @Test
    public void the_test_runner_records_each_step_with_a_nice_name() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SuccessfulSingleTestScenario.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();

        TestOutcome testOutcome = executedScenarios.get(0);
        TestStep firstStep = testOutcome.getTestSteps().get(0);

        assertThat(firstStep.getDescription(), is("Step that succeeds"));
    }

    @Test
    public void default_test_names_can_be_overriden_in_the_Test_annotation() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(SuccessfulSingleTestScenario.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();

        TestOutcome testOutcome = executedScenarios.get(0);
        TestStep pendingStep = testOutcome.getTestSteps().get(2);

        assertThat(pendingStep.getDescription(), is("A pending step"));
    }

    @Test
    public void the_test_runner_records_each_step_with_a_nice_name_when_steps_have_parameters() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(TestScenarioWithParameterizedSteps.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();

        TestOutcome testOutcome = executedScenarios.get(0);
        TestStep firstStep = testOutcome.getTestSteps().get(0);

        assertThat(firstStep.getDescription(), is("Step with a parameter: <span class='single-parameter'>foo</span>"));
    }

    @Test
    public void the_test_runner_records_each_step_with_a_nice_name_when_steps_have_multiple_parameters() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(TestScenarioWithParameterizedSteps.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();

        TestOutcome testOutcome = executedScenarios.get(0);
        TestStep secondStep = testOutcome.getTestSteps().get(1);

        assertThat(secondStep.getDescription(), is("Step with two parameters: <span class='parameters'>foo, 2</span>"));
    }

    @Test
    public void step_titles_can_be_overridden_with_the_StepDescription_annotation() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(AnnotatedSingleTestScenario.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();

        TestOutcome testOutcome = executedScenarios.get(0);
        TestStep firstStep = testOutcome.getTestSteps().get(0);

        assertThat(firstStep.getDescription(), is("A step that succeeds indeed!"));
    }

    @Test
    public void scenario_titles_can_be_overridden_with_the_Title_annotation() throws InitializationError {

        ThucydidesRunner runner = new ThucydidesRunner(AnnotatedSingleTestScenario.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedScenarios = runner.getTestOutcomes();

        TestOutcome testOutcome = executedScenarios.get(0);

        assertThat(testOutcome.getTitle(), is("Oh happy days!"));
    }

    @Test
    public void the_test_scenario_does_not_need_a_steps_field() throws InitializationError {
        ThucydidesRunner runner = new ThucydidesRunner(SampleScenarioWithoutStepAnnotations.class);
        runner.run(new RunNotifier());
    }

    @Test
    public void the_test_scenario_does_not_need_steps() throws InitializationError {
        ThucydidesRunner runner = new ThucydidesRunner(SampleScenarioWithoutSteps.class);
        runner.run(new RunNotifier());
    }

    @Test
    public void the_manager_should_ignore_close_if_the_webdriver_if_not_defined() {
        WebdriverManager manager = new ThucydidesWebdriverManager(webDriverFactory, new SystemPropertiesConfiguration(environmentVariables));

        manager.closeDriver();
    }

    class TestableThucydidesRunner extends ThucydidesRunner {

        private final File testOutputDirectory;

        public TestableThucydidesRunner(final Class<?> klass, File outputDirectory) throws InitializationError {
            super(klass);
            testOutputDirectory = outputDirectory;
        }

        public TestableThucydidesRunner(Class<?> klass,
                                        WebDriverFactory webDriverFactory,
                                        File outputDirectory) throws InitializationError {
            super(klass, webDriverFactory);
            testOutputDirectory = outputDirectory;
        }

        @Override
        public File getOutputDirectory() {
            return testOutputDirectory;
        }
    }

    @Test
    public void xml_test_results_are_written_to_the_output_directory() throws InitializationError {

        File outputDirectory = temporaryFolder.newFolder("output");

        ThucydidesRunner runner = new TestableThucydidesRunner(SamplePassingScenario.class,
                                                               webDriverFactory,
                                                               outputDirectory);
        runner.run(new RunNotifier());

        List<String> generatedXMLReports = Arrays.asList(outputDirectory.list(new XMLFileFilter()));
        assertThat(generatedXMLReports.size(), is(3));
        assertThat(generatedXMLReports, hasItems("sample_passing_scenario_edge_case_1.xml",
                                                 "sample_passing_scenario_edge_case_2.xml",
                                                 "sample_passing_scenario_happy_day_scenario.xml"));


    }

    @Test
    public void xml_test_results_for_multiple_stories_are_written_to_the_output_directory() throws InitializationError {

        File outputDirectory = temporaryFolder.newFolder("output");

        new TestableThucydidesRunner(SamplePassingScenarioUsingHtmlUnit.class,
                                       webDriverFactory,
                                       outputDirectory).run(new RunNotifier());

        new TestableThucydidesRunner(SampleFailingScenarioUsingHtmlUnit.class,
                                       webDriverFactory,
                                       outputDirectory).run(new RunNotifier());

        List<String> generatedXMLReports = Arrays.asList(outputDirectory.list(new XMLFileFilter()));
        assertThat(generatedXMLReports.size(), is(6));
        assertThat(generatedXMLReports, hasItems("sample_passing_scenario_using_html_unit_edge_case_1.xml",
                                                 "sample_passing_scenario_using_html_unit_edge_case_2.xml",
                                                 "sample_passing_scenario_using_html_unit_happy_day_scenario.xml",
                                                 "sample_failing_scenario_using_html_unit_edge_case_1.xml",
                                                 "sample_failing_scenario_using_html_unit_edge_case_2.xml",
                                                 "sample_failing_scenario_using_html_unit_happy_day_scenario.xml"));
    }

    @Test
    public void xml_test_results_for_multiple_successful_stories_are_written_to_the_output_directory() throws InitializationError {

        File outputDirectory = temporaryFolder.newFolder("output");

        new TestableThucydidesRunner(SamplePassingScenarioUsingHtmlUnit.class,
                                       webDriverFactory,
                                       outputDirectory).run(new RunNotifier());

        new TestableThucydidesRunner(AnotherSamplePassingScenario.class,
                                       webDriverFactory,
                                       outputDirectory).run(new RunNotifier());

        List<String> generatedXMLReports = Arrays.asList(outputDirectory.list(new XMLFileFilter()));
        assertThat(generatedXMLReports.size(), is(6));
        assertThat(generatedXMLReports, hasItems("sample_passing_scenario_using_html_unit_edge_case_1.xml",
                                                 "sample_passing_scenario_using_html_unit_edge_case_2.xml",
                                                 "sample_passing_scenario_using_html_unit_happy_day_scenario.xml",
                                                 "another_sample_passing_scenario_edge_case_1.xml",
                                                 "another_sample_passing_scenario_edge_case_2.xml",
                                                 "another_sample_passing_scenario_happy_day_scenario.xml"));
    }

    @Test
    public void html_test_results_are_written_to_the_output_directory() throws InitializationError {

        File outputDirectory = temporaryFolder.newFolder("output");

        ThucydidesRunner runner = new TestableThucydidesRunner(SamplePassingScenarioUsingHtmlUnit.class,
                                                               webDriverFactory,
                                                               outputDirectory);
        runner.run(new RunNotifier());

        List<String> generatedXMLReports = Arrays.asList(outputDirectory.list(new HTMLFileFilter()));
        assertThat(generatedXMLReports.size(), is(3));
        assertThat(generatedXMLReports, hasItems("sample_passing_scenario_using_html_unit_edge_case_1.html",
                                                 "sample_passing_scenario_using_html_unit_edge_case_2.html",
                                                 "sample_passing_scenario_using_html_unit_happy_day_scenario.html"));


    }

    @Test
    public void test_scenarios_should_not_need_a_webdriver() throws InitializationError {
        ThucydidesRunner runner = new ThucydidesRunner(SimpleNonWebScenario.class);
        runner.run(new RunNotifier());
    }


    private class XMLFileFilter implements FilenameFilter {
        public boolean accept(File file, String filename) {
            return filename.endsWith(".xml");
        }
    }

    private class HTMLFileFilter implements FilenameFilter {
        public boolean accept(File file, String filename) {
            return filename.endsWith(".html");
        }
    }

}