import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features",
    plugin = {"pretty", "html:target/site/cucumber-pretty", "json:target/cucumber.json"},
    glue = "steps",
    snippets = SnippetType.UNDERSCORE)
public class GenericTestRunner {

}