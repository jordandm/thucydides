package net.thucydides.junit.runners;

import net.thucydides.junit.annotations.Qualifier;
import net.thucydides.junit.annotations.TestData;
import net.thucydides.junit.annotations.UseTestDataFrom;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WhenFindingTestDataInADataDrivenTest {

    final static class DataDrivenTestScenario {

        @TestData
        public static Collection<Object[]> testData() {
            return Arrays.asList(new Object[][]{
                    {"a", 1},
                    {"b", 2},
                    {"c", 3}
            });
        }
    }

    @UseTestDataFrom("test-data/simple-data.csv")
    final static class CSVDataDrivenTestScenario {}

    @Test
    public void the_parameterized_data_method_is_annotated_by_the_TestData_annotation() throws Exception {
        TestClass testClass = new TestClass(DataDrivenTestScenario.class);
        FrameworkMethod method = DataDrivenAnnotations.forClass(testClass).getTestDataMethod();

        assertThat(method.getName(), is("testData"));

    }

    @Test
    public void the_parameterized_data_method_returns_the_set_of_test_data() throws Throwable {
        TestClass testClass = new TestClass(DataDrivenTestScenario.class);
        Collection<Object[]> testData = DataDrivenAnnotations.forClass(testClass).getParametersList();

        assertThat(testData.size(), is(3));

    }

    @Test
    public void should_be_able_to_count_the_number_of_data_entries() throws Throwable {
        TestClass testClass = new TestClass(CSVDataDrivenTestScenario.class);
        int dataEntries = DataDrivenAnnotations.forClass(testClass).countDataEntries();

        assertThat(dataEntries, is(3));

    }

    @Test
    public void should_recognize_a_test_case_with_valid_test_data() {
        TestClass testClass = new TestClass(DataDrivenTestScenario.class);
        assertThat(DataDrivenAnnotations.forClass(testClass).hasTestDataDefined(), is(true));
    }

    final static class DataDrivenTestScenarioWithNoData {}

    @Test
    public void should_recognize_a_test_case_without_valid_test_data() {
        TestClass testClass = new TestClass(DataDrivenTestScenarioWithNoData.class);
        assertThat(DataDrivenAnnotations.forClass(testClass).hasTestDataDefined(), is(false));
    }

    @Test
    public void should_recognize_a_test_case_with_a_valid_test_data_source() {
        TestClass testClass = new TestClass(CSVDataDrivenTestScenario.class);
        assertThat(DataDrivenAnnotations.forClass(testClass).hasTestDataSourceDefined(), is(true));
    }

    @Test
    public void should_recognize_a_test_case_without_a_valid_test_data_source() {
        TestClass testClass = new TestClass(DataDrivenTestScenarioWithNoData.class);
        assertThat(DataDrivenAnnotations.forClass(testClass).hasTestDataSourceDefined(), is(false));
    }

    @Test
    public void should_load_test_class_instances_using_a_provided_test_data_source() throws IOException {
        TestClass testClass = new TestClass(CSVDataDrivenTestScenario.class);
        List<PersonTestScenario> testScenarios
                = DataDrivenAnnotations.forClass(testClass).getDataAsInstancesOf(PersonTestScenario.class);

        assertThat(testScenarios.size(), is(3));
        assertThat(testScenarios.get(0).getName(), is("Joe Smith"));
        assertThat(testScenarios.get(1).getName(), is("Jack Black"));
    }

    static class DataDrivenTestScenarioWithPrivateTestData {

        @TestData
        static Collection testData() {
            return Arrays.asList(new Object[][]{
                    {"a", 1},
                    {"b", 2},
                    {"c", 3}
            });
        }
    }
    @Test(expected = IllegalArgumentException.class)
    public void the_parameterized_data_method_must_be_public() throws Exception {
        TestClass testClass = new TestClass(DataDrivenTestScenarioWithPrivateTestData.class);
        FrameworkMethod method = DataDrivenAnnotations.forClass(testClass).getTestDataMethod();

        assertThat(method.getName(), is("testData"));

    }

    static class DataDrivenTestScenarioWithNonStaticTestData {

        @TestData
        public Collection testData() {
            return Arrays.asList(new Object[][]{
                    {"a", 1},
                    {"b", 2},
                    {"c", 3}
            });
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void the_parameterized_data_method_must_be_static() throws Exception {
        TestClass testClass = new TestClass(DataDrivenTestScenarioWithNonStaticTestData.class);
        FrameworkMethod method = DataDrivenAnnotations.forClass(testClass).getTestDataMethod();

        assertThat(method.getName(), is("testData"));
    }


    public class SimpleDataDrivenScenario {

        private String name;
        private String address;
        private String phone;

    }

    @Test
    public void toString_should_be_used_as_a_default_qualifier_for_test_case_instances() {
        SimpleDataDrivenScenario testCase = new SimpleDataDrivenScenario();
        String qualifier = QualifierFinder.forTestCase(testCase).getQualifier();

        assertThat(qualifier, is(testCase.toString()));
    }

    public class AnnotatedDataDrivenScenario {

        private String name;
        private String address;
        private String phone;

        @Qualifier
        public String getQualifier() {
            return name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    @Test
    public void should_use_the_Qualifier_method_as_a_qualifier_if_present() {
        AnnotatedDataDrivenScenario testCase = new AnnotatedDataDrivenScenario();
        testCase.setName("Joe");

        String qualifier = QualifierFinder.forTestCase(testCase).getQualifier();

        assertThat(qualifier, is("Joe"));
    }

    public static class DataDrivenScenarioWithStaticQualifier {

        @Qualifier
        public static String qualifier() {
            return "QUALIFIER";
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void the_qualifier_method_must_not_be_static() {
        DataDrivenScenarioWithStaticQualifier testCase = new DataDrivenScenarioWithStaticQualifier();
        QualifierFinder.forTestCase(testCase).getQualifier();
    }


    public static class DataDrivenScenarioWithNonPublicQualifier {

        @Qualifier
        protected String qualifier() {
            return "QUALIFIER";
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void the_qualifier_method_must_be_public() {
        DataDrivenScenarioWithNonPublicQualifier testCase = new DataDrivenScenarioWithNonPublicQualifier();
        QualifierFinder.forTestCase(testCase).getQualifier();
    }

    public static class DataDrivenScenarioWithWronlyTypedQualifier {

        @Qualifier
        public int qualifier() {
            return 0;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void the_qualifier_method_must_return_a_string() {
        DataDrivenScenarioWithWronlyTypedQualifier testCase = new DataDrivenScenarioWithWronlyTypedQualifier();
        QualifierFinder.forTestCase(testCase).getQualifier();
    }

    @UseTestDataFrom(value="test-data/simple-semicolon-data.csv", separator=';')
    final static class CSVDataDrivenTestScenarioUsingSemiColons {}

    @Test
    public void should_load_test_class_instances_using_semicolons() throws IOException {
        TestClass testClass = new TestClass(CSVDataDrivenTestScenarioUsingSemiColons.class);
        List<PersonTestScenario> testScenarios
                = DataDrivenAnnotations.forClass(testClass).getDataAsInstancesOf(PersonTestScenario.class);

        assertThat(testScenarios.size(), is(2));
        assertThat(testScenarios.get(0).getName(), is("Joe Smith"));
        assertThat(testScenarios.get(0).getAddress(), is("10 Main Street, Smithville"));
        assertThat(testScenarios.get(1).getName(), is("Jack Black"));
        assertThat(testScenarios.get(1).getAddress(), is("1 Main Street, Smithville"));
    }

}
