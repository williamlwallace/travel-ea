import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;
import cucumber.api.junit.Cucumber;
import cucumber.api.SnippetType;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features",
        plugin = {"pretty", "html:target/site/cucumber-pretty", "json:target/cucumber.json"},
        glue = "steps",
        snippets = SnippetType.UNDERSCORE)
public class GenericTestRunner {

}