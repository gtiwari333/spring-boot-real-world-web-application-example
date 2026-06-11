package gt.app.web.mvc;

import gt.app.domain.Article;
import gt.app.domain.ArticleStatus;
import gt.app.modules.article.ArticleReviewResultDto;
import gt.app.modules.article.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
class ReviewController {

    final ArticleService articleService;

    @GetMapping("/review/{id}")
    public String startReview(Model model, @PathVariable Long id) {
        model.addAttribute("article", articleService.readForReview(id));
        return "admin/review-article";
    }

    @PostMapping("/review")
    public String finishReviewArticle(ArticleReviewResultDto reviewResult, RedirectAttributes redirectAttrs) {

        Optional<Article> articleOpt = articleService.handleReview(reviewResult);

        String action = reviewResult.getVerdict() == ArticleStatus.PUBLISHED ? "Approved" : "Rejected";

        articleOpt.ifPresentOrElse(
            n -> redirectAttrs.addFlashAttribute("success", "Article with id " + reviewResult.getId() + " is " + action),
            () -> redirectAttrs.addFlashAttribute("success", "Article with id " + reviewResult.getId() + " is already reviewed or doesn't exists")
        );

        return "redirect:/admin";
    }
}
