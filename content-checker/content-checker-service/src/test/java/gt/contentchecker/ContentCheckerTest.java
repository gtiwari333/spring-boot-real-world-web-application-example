package gt.contentchecker;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ContentCheckerTest {

    ContentChecker checker = new ContentChecker();

    @Test
    void passesCleanText() {
        assertThat(checker.isOkay("hello world")).isEqualTo(ContentCheckOutcome.PASSED);
        assertThat(checker.isOkay("good morning everyone")).isEqualTo(ContentCheckOutcome.PASSED);
        assertThat(checker.isOkay("")).isEqualTo(ContentCheckOutcome.PASSED);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "fuck", "FUCK", "Fuck",
        "what the fuck is this",
        "fucking", "fucker",
        "suck", "this sucks", "sucked", "sucker",
        "ass", "you are such an ass"
    })
    void failsOnBadWord(String text) {
        assertThat(checker.isOkay(text)).isEqualTo(ContentCheckOutcome.FAILED);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "party", "birthday party tonight",
        "politics", "discussing Politics again",
        "libtard",
        "conspiracy theory",
        "freedom fighter",
        "snowflake generation"
    })
    void needsManualReviewOnControversialWord(String text) {
        assertThat(checker.isOkay(text)).isEqualTo(ContentCheckOutcome.MANUAL_REVIEW_NEEDED);
    }

    @Test
    void badWordsTakePriorityOverControversial() {
        assertThat(checker.isOkay("fuck politics")).isEqualTo(ContentCheckOutcome.FAILED);
    }

    @Test
    void caseInsensitiveMatching() {
        assertThat(checker.isOkay("FUCK")).isEqualTo(ContentCheckOutcome.FAILED);
        assertThat(checker.isOkay("Politics")).isEqualTo(ContentCheckOutcome.MANUAL_REVIEW_NEEDED);
        assertThat(checker.isOkay("Hello World")).isEqualTo(ContentCheckOutcome.PASSED);
    }

    @Test
    void wordBoundaryPreventsFalsePositives() {
        assertThat(checker.isOkay("classic")).isEqualTo(ContentCheckOutcome.PASSED);
        assertThat(checker.isOkay("passage")).isEqualTo(ContentCheckOutcome.PASSED);
        assertThat(checker.isOkay("assembly")).isEqualTo(ContentCheckOutcome.PASSED);
        assertThat(checker.isOkay("departure")).isEqualTo(ContentCheckOutcome.PASSED);
        assertThat(checker.isOkay("impartial")).isEqualTo(ContentCheckOutcome.PASSED);
    }

    @Test
    void paragraphsAreCheckedCorrectly() {
        String paragraph = """
            The ambassador gave a classic speech about freedom and democracy.
            It was a beautiful assembly of people from all parties.
            The discussion covered politics and election topics.
            """;
        assertThat(checker.isOkay(paragraph)).isEqualTo(ContentCheckOutcome.MANUAL_REVIEW_NEEDED);

        String paragraphWithBadWord = """
            The user posted a comment saying 'what the fuck is wrong with you'.
            This kind of language is unacceptable in public discourse.
            """;
        assertThat(checker.isOkay(paragraphWithBadWord)).isEqualTo(ContentCheckOutcome.FAILED);

        String cleanParagraph = """
            The weather today is beautiful with clear skies and gentle breeze.
            Many families are enjoying picnics in the park this afternoon.
            """;
        assertThat(checker.isOkay(cleanParagraph)).isEqualTo(ContentCheckOutcome.PASSED);
    }

    @Test
    void punctuationDoesNotPreventMatching() {
        assertThat(checker.isOkay("what the fuck!")).isEqualTo(ContentCheckOutcome.FAILED);
        assertThat(checker.isOkay("you are an ass.")).isEqualTo(ContentCheckOutcome.FAILED);
        assertThat(checker.isOkay("let's party!")).isEqualTo(ContentCheckOutcome.MANUAL_REVIEW_NEEDED);
        assertThat(checker.isOkay("discussing politics,")).isEqualTo(ContentCheckOutcome.MANUAL_REVIEW_NEEDED);
    }

    @Test
    @Tag("benchmark")
    void throughputBenchmark() {
        int iterations = 1000;
        int reportInterval = 5_000;

        // Build a base  text from a clean lorem-ipsum-style paragraph
        String text = "The quick brown fox jumps over the lazy dog. ".repeat(250);

        // Warmup
        for (int i = 0; i < 100; i++) {
            checker.isOkay(text);
        }

        // Benchmark
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            checker.isOkay(text);
            if ((i + 1) % reportInterval == 0) {
                long elapsed = System.nanoTime() - start;
                double rate = (i + 1) * 1e9 / elapsed;
                System.out.printf("  %,d / %,d — rate: %,.0f msg/s, elapsed: %,d ms%n",
                    i + 1, iterations, rate, elapsed / 1_000_000);
            }
        }
        long totalNanos = System.nanoTime() - start;

        double seconds = totalNanos / 1e9;
        double rate = iterations / seconds;
        double avgMicros = (totalNanos / 1e3) / iterations;

        System.out.printf("%n=== Throughput Benchmark ===%n");
        System.out.printf("Iterations   : %,d%n", iterations);
        System.out.printf("Text length  : %,d%n", text.length());
        System.out.printf("Total time   : %,.2f s%n", seconds);
        System.out.printf("Throughput   : %,.0f msg/s%n", rate);
        System.out.printf("Avg latency  : %,.1f µs/msg%n", avgMicros);
        System.out.printf("===========================%n");

        // Sanity — must complete within reasonable time (30s on modern hardware)
        assertThat(seconds).isLessThan(30);
    }
}
