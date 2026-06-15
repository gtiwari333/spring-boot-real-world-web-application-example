package gt.app.e2e.pageobj;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ArticleReadPage extends BasePage<ArticleReadPage> {

    @Override
    public ArticleReadPage open() {
        return new ArticleReadPage();
    }

    public ArticleReadPage open(Long articleId) {
        Selenide.open("/article/read/" + articleId);
        return this;
    }

    @Override
    public SelenideElement body() {
        return $("body");
    }

    /**
     * Returns the main "Add a comment" input field — the last {@code input[name='content']}
     * on the page, which sits inside the primary comment form after all existing comments.
     */
    public SelenideElement mainCommentInput() {
        return $$("input[name='content']").last();
    }

    /**
     * Returns the main "Add a comment" submit button — the last {@code button[type='submit']}
     * on the page.
     */
    public SelenideElement mainCommentSubmitButton() {
        return $$("button[type='submit']").last();
    }

    /**
     * Returns the reply input field for the n-th existing top-level comment (0-indexed).
     * Each displayed comment carries its own nested reply form.
     */
    public SelenideElement replyInput(int topLevelCommentIndex) {
        return $$("input[name='content']").get(topLevelCommentIndex);
    }

    /**
     * Returns the reply submit button for the n-th existing top-level comment (0-indexed).
     */
    public SelenideElement replySubmitButton(int topLevelCommentIndex) {
        return $$("button[type='submit']").get(topLevelCommentIndex);
    }

    /**
     * Fills the main "Add a comment" form and submits it.
     */
    public ArticleReadPage addTopLevelComment(String commentText) {
        mainCommentInput().setValue(commentText);
        mainCommentSubmitButton().click();
        return new ArticleReadPage();
    }

    /**
     * Fills the reply form nested under the n-th existing comment and submits it.
     */
    public ArticleReadPage addReplyToComment(int topLevelCommentIndex, String replyText) {
        replyInput(topLevelCommentIndex).setValue(replyText);
        replySubmitButton(topLevelCommentIndex).click();
        return new ArticleReadPage();
    }

    /**
     * Asserts that the given text appears somewhere among the displayed comments.
     */
    public ArticleReadPage shouldHaveComment(String commentText) {
        body().shouldHave(text(commentText));
        return this;
    }

    /**
     * Asserts that the "Add a comment" form is visible (always rendered for all users).
     */
    public ArticleReadPage shouldShowAddCommentForm() {
        body().shouldHave(text("Add a comment:"));
        return this;
    }

    /**
     * Asserts that reply forms (nested under existing comments) are NOT visible.
     * Only authenticated users see reply forms — guarded by {@code sec:authorize="isAuthenticated()"}.
     * When no reply forms are rendered, only the main "Add a comment" input exists.
     */
    public ArticleReadPage shouldNotHaveReplyForms() {
        // Each reply form adds an input[name='content']; the main form is the last one.
        // With 0 visible reply forms, there should be exactly 1 content input.
        int contentInputCount = $$("input[name='content']").size();
        if (contentInputCount != 1) {
            throw new AssertionError(
                "Expected exactly 1 input[name='content'] (main form only, no reply forms), but found " + contentInputCount);
        }
        return this;
    }

    /**
     * Returns the success flash message text.
     */
    public String getSuccessMessage() {
        return $(".alert.alert-success").getText();
    }

    /**
     * Asserts that the page body contains the given article title.
     */
    public ArticleReadPage shouldHaveArticleTitle(String title) {
        $(".card-title span").shouldHave(text(title));
        return this;
    }

    /**
     * Returns all currently visible comment content elements.
     */
    public ElementsCollection commentContents() {
        return $$("small span");
    }
}
