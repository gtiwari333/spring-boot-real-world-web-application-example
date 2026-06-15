package gt.app.e2e.pageobj;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class ReviewArticlePage extends BasePage<ReviewArticlePage> {

    @Override
    public ReviewArticlePage open() {
        return new ReviewArticlePage();
    }

    public SelenideElement getTitle() {
        return $(".article-preview-box .card-title");
    }

    public SelenideElement getContent() {
        return $(".article-preview-box .card-text");
    }

    public SelenideElement getAcceptButton() {
        return $("button[value='PUBLISHED']");
    }

    public SelenideElement getRejectButton() {
        return $("button[value='BLOCKED']");
    }

    public AdminPage accept() {
        getAcceptButton().click();
        return new AdminPage();
    }

    public AdminPage reject() {
        getRejectButton().click();
        return new AdminPage();
    }
}
