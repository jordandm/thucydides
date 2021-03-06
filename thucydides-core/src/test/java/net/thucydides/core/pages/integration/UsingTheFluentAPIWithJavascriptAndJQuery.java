package net.thucydides.core.pages.integration;


import net.thucydides.core.webdriver.jquery.ByJQuery;
import net.thucydides.core.webdriver.jquery.ByJQuerySelector;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class UsingTheFluentAPIWithJavascriptAndJQuery extends FluentElementAPITestsBaseClass {

    @Test
    public void should_inject_jquery_into_the_page() {
        StaticSitePage page = getFirefoxPage();

        page.evaluateJavascript("$('#firstname').focus();");

        Boolean jqueryInjected = (Boolean) page.evaluateJavascript("return (typeof jQuery === 'function')");
        assertThat(jqueryInjected, is(true));
    }

    @Test
    public void should_support_jquery_queries_in_the_page() {

        StaticSitePage page = getFirefoxPage();
        page.evaluateJavascript("$('#firstname').focus();");

        assertThat(page.element(page.firstName).hasFocus(), is(true));

        page.evaluateJavascript("$('#lastname').focus();");

        assertThat(page.element(page.lastName).hasFocus(), is(true));
    }

    @Test
    public void should_support_jquery_queries_that_return_values_in_the_page() {

        StaticSitePage page = getFirefoxPage();
        Object result = page.evaluateJavascript("return $('#country').val();");

        assertThat(result.toString(), is("Australia"));
    }

    @Test
    public void should_be_able_to_find_an_element_using_a_jquery_expression() {
        StaticSitePage page = getFirefoxPage();

        WebElement link = page.getDriver().findElement(ByJQuery.selector("a[title='Click Me']"));
        assertThat(link.isDisplayed(), is(true));
    }

    @Test
    public void should_be_able_to_find_multiple_elements_using_a_jquery_expression() {
        StaticSitePage page = getFirefoxPage();
        List<WebElement> links = page.getDriver().findElements(ByJQuery.selector("h2"));
        assertThat(links.size(), is(2));
    }

    @Test(expected = WebDriverException.class)
    public void should_fail_gracefully_if_no_jquery_element_is_found() {
        StaticSitePage page = getFirefoxPage();
        page.getDriver().findElement(ByJQuery.selector("a[title='Does Not Exist']"));
    }

    @Test(expected = WebDriverException.class)
    public void should_fail_gracefully_if_jquery_selector_is_invalid() {
        StaticSitePage page = getFirefoxPage();
        page.getDriver().findElement(ByJQuery.selector("a[title='Does Not Exist'"));
    }

    @Test
    public void should_evaluate_javascript_within_browser() {
        StaticSitePage page = getFirefoxPage();
        String result = (String) page.evaluateJavascript("return document.title");
        assertThat(result, is("Thucydides Test Site"));
    }

    @Test
    public void should_execute_javascript_within_browser() {
        StaticSitePage page = getFirefoxPage();
        assertThat(page.element(page.firstName).hasFocus(), is(false));
        page.evaluateJavascript("document.getElementById('firstname').focus()");
        assertThat(page.element(page.firstName).hasFocus(), is(true));
    }


    @Test
    public void a_jquery_selector_should_be_described_by_the_corresponding_jquery_expression() {
        ByJQuerySelector jQuerySelector = ByJQuery.selector("a[title='Click Me']");
        
        assertThat(jQuerySelector.toString(), containsString("a[title='Click Me']"));
    }
}
