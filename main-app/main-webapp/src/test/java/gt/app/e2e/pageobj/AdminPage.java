package gt.app.e2e.pageobj;

import com.codeborne.selenide.Selenide;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class AdminPage extends BaseLoggedInPage<AdminPage> {

    @Override
    public AdminPage open() {
        Selenide.open("/admin");
        return this;
    }

    /**
     * Click "Review" on the row whose Title cell (td[1]) matches the given text.
     * Mirrors the XPath pattern used by {@link UserArticleListingPage#editArticleByTitle(String)}.
     */
    public ReviewArticlePage clickReviewByTitle(String articleTitle) {
        $x(".//table/tbody/tr[td[1][normalize-space(.)='" + articleTitle + "']]/td[6]/a").click();
        return new ReviewArticlePage();
    }

    public String getSuccessMessage() {
        return $(".alert.alert-success").getText();
    }
}
