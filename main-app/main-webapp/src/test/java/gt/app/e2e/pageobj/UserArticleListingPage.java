package gt.app.e2e.pageobj;

import com.codeborne.selenide.SelenideElement;

import java.io.File;
import java.util.List;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class UserArticleListingPage extends BaseLoggedInPage<UserArticleListingPage> {

    @Override
    public UserArticleListingPage open() {
        return new UserArticleListingPage();
    }

    public List<File> downloadFiles(int row) {
        return $$x(".//table/tbody/tr[" + row + "]/td[4]/a").asFixedIterable()
            .stream()
            .map(SelenideElement::download)
            .toList();
    }


    public ArticleEditPage editArticle(int row) {
        ///html/body/div[2]/div[1]/
        $x(".//table/tbody/tr[" + row + "]/td[6]/a").click();
        return new ArticleEditPage();
    }

    /**
     * Click "Edit" on the row whose Title cell (td[2]) matches the given text.
     * Prefer this over {@link #editArticle(int)} when the test cares about a specific
     * article — row index is flaky when multiple articles share the same created_date
     * second (the listing sorts by createdDate desc and the column is MySQL `datetime`
     * with 1s precision, so ties have undefined order).
     */
    public ArticleEditPage editArticleByTitle(String title) {
        $x(".//table/tbody/tr[td[2][normalize-space(.)='" + title + "']]/td[6]/a").click();
        return new ArticleEditPage();
    }

    public UserArticleListingPage deletePage(int row) {
        $x(".//table/tbody/tr[" + row + "]/td[7]/a").click();
        return new UserArticleListingPage();
    }

    /**
     * Click "Delete" on the row whose Title cell (td[2]) matches the given text.
     * See {@link #editArticleByTitle(String)} for why title-based selection is
     * preferred over row index in tests.
     */
    public UserArticleListingPage deleteByTitle(String title) {
        $x(".//table/tbody/tr[td[2][normalize-space(.)='" + title + "']]/td[7]/a").click();
        return new UserArticleListingPage();
    }

}
