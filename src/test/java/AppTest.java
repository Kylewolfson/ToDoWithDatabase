import org.fluentlenium.adapter.FluentTest;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.sql2o.*; // for DB support
import org.junit.*; // for @Before and @After
import static org.fluentlenium.core.filter.FilterConstructor.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest extends FluentTest {
  public WebDriver webDriver = new HtmlUnitDriver();

  @Before
public void setUp() {
  DB.sql2o = new Sql2o("jdbc:postgresql://localhost:5432/to_do_test", null, null);
}

@After
public void tearDown() {
  try(Connection con = DB.sql2o.open()) {
    String deleteTasksQuery = "DELETE FROM tasks *;";
    String deleteCategoriesQuery = "DELETE FROM categories *;";
    con.createQuery(deleteTasksQuery).executeUpdate();
    con.createQuery(deleteCategoriesQuery).executeUpdate();
  }
}

  @Override
  public WebDriver getDefaultDriver() {
    return webDriver;
  }

  @ClassRule
  public static ServerRule server = new ServerRule();

  @Test
  public void rootTest() {
    goTo("http://localhost:4567/");
    assertThat(pageSource()).contains("Do all the things!");
  }

  @Test
  public void categoryIsCreatedTest() {
    goTo("http://localhost:4567/");
    fill("#name").with("Household chores");
    submit(".btn");
    assertThat(pageSource()).contains("Your category hath been saved");
  }

  @Test
  public void categoryIsDisplayedTest() {
    Category newCategory = new Category("Household chores");
    newCategory.save();
    String categoryPath = String.format("http://localhost:4567/%d", newCategory.getId());
    goTo(categoryPath);
    assertThat(pageSource()).contains("Household chores");
  }

  @Test
  public void allTasksDisplayDescriptionOnCategoryPage() {
    Category myCategory = new Category("Household chores");
    myCategory.save();
    Task firstTask = new Task("Mow the lawn", myCategory.getId());
    firstTask.save();
    Task secondTask = new Task("Do the dishes", myCategory.getId());
    secondTask.save();
    String categoryPath = String.format("http://localhost:4567/%d", myCategory.getId());
    goTo(categoryPath);
    assertThat(pageSource()).contains("Mow the lawn");
    assertThat(pageSource()).contains("Do the dishes");
  }

}
