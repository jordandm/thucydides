package net.thucydides.core.webdriver;

import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.steps.StepFailure;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.lang.reflect.Field;

import static org.hamcrest.Matchers.containsString;

public class WhenLocatingWebElements {

    @Mock
    WebDriver driver;

    @Mock
    StepFailure failure;

    Field field;

    DisplayedElementLocator locator;

    static class SomePageObject {

        @FindBy(id="someId")
        public WebElement someField;

    }

    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);

        field = SomePageObject.class.getField("someField");

        StepEventBus.getEventBus().clear();
    }

    @Test(timeout = 500)
    public void should_find_element_immediately_if_a_previous_step_has_failed() {
        DisplayedElementLocator locator = new DisplayedElementLocator(driver, field, 5);
        StepEventBus.getEventBus().stepFailed(failure);
        locator.findElement();
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_wait_for_find_element_immediately_if_no_previous_step_has_failed() {

        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage(containsString("Timed out after 1 second"));

        DisplayedElementLocator locator = new DisplayedElementLocator(driver, field, 1);
        locator.findElement();
    }

}
