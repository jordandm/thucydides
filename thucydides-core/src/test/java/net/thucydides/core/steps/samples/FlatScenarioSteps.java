package net.thucydides.core.steps.samples;

import net.thucydides.core.Thucydides;
import net.thucydides.core.annotations.Pending;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.StepGroup;
import net.thucydides.core.annotations.Title;
import net.thucydides.core.pages.Pages;
import net.thucydides.core.steps.ScenarioSteps;
import org.junit.Ignore;

public class FlatScenarioSteps extends ScenarioSteps {

    public FlatScenarioSteps(Pages pages) {
        super(pages);
    }

    @Step
    public void step_one(){
    }
    
    @Step
    public void step_two() {
    }

    @Step
    public void step_three() {
    }

    @Step
    public void nested_step_one(){
    }

    @Step
    public void nested_step_two() {
    }

    @Step
    public void nested_step_three() {
    }

    @Step
    public void failingStep() {
        throw new AssertionError("Step failed");
    }

    @Ignore
    @Step
    public void ignoredStep() {}

    @Pending
    @Step
    public void pendingStep() {}

    @Pending
    @Step
    public void pending_group() {
        step_three();
        step_two();
        step_one();
    }

    @Title("A step with a title")
    @Step
    public void step_with_title() {}

    @Ignore
    @Step
    public void ignored_group() {
        step_three();
        step_two();
        step_one();
    }

    @Step
    public void grouped_steps() {
        nested_step_one();
        nested_step_two();
        nested_step_one();
        nested_step_two();
    }

    @Step
    public void deeply_grouped_steps() {
        step_one();
        step_two();
        grouped_steps();
        step_two();
        step_one();
    }

    @Step
    public void stepWithLongName() {}

    @Step
    public void stepWithParameters(String name) {}

    @Step
    public void step_with_long_name_and_underscores() {}

    @StepGroup("Annotated step group title")
    public void a_step_group() {
        stepWithLongName();
        step_with_long_name_and_underscores();
    }

    @Step
    public void a_plain_step_group() {
        stepWithLongName();
        step_with_long_name_and_underscores();
    }

    @Step
    public void stepCausingANullPointerException() {
        String nullValue = null;
        nullValue.length();
    }

    public void unannotatedStepCausingANullPointerException() {
        String nullValue = null;
        nullValue.length();
    }

    @Step
    public void programmaticallyIgnoredStep() {
        Thucydides.ignoredStep("This test should be skipped");

    }

    @Step
    public void programmaticallyPendingStep() {
        Thucydides.pendingStep("This test should be skipped");

    }

}
