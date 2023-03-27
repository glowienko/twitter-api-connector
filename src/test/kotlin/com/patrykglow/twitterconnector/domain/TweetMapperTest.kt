package com.patrykglow.twitterconnector.domain

import com.patrykglow.twitterconnector.TweetTestData
import com.patrykglow.twitterconnector.TweetTestData.createTweetWith
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class TweetMapperTest {

    @ParameterizedTest
    @MethodSource("generateTweetListMappingCases")
    fun `should map list of tweets to sorted map by author `(givenList: List<Tweet>, result: Map<Author, List<Tweet>>) {
        givenList.groupByAuthorSortAllAscending().size shouldBe result.size

//        assertThat(givenList.groupByAuthorSortAllAscending()).isEqualTo(result).usingRecursiveComparison() // without kotest
        givenList.groupByAuthorSortAllAscending() shouldBe result
    }

    companion object {
        @JvmStatic
        fun generateTweetListMappingCases(): Stream<Arguments> {
            val author1 = TweetTestData.createAuthor(id = 1, createdAt = 1)
            val author2 = TweetTestData.createAuthor(id = 2, createdAt = 2)
            val author3CreatedAtSameAsAuthor1 = TweetTestData.createAuthor(id = 3, createdAt = author1.createdAt)
            val author1Tweets = listOf(
                createTweetWith(author1, createdAt = 1),
                createTweetWith(author1, createdAt = 2)
            )
            val author2Tweets = listOf(createTweetWith(author2, createdAt = 2))
            val author3Tweets = listOf(createTweetWith(author3CreatedAtSameAsAuthor1, createdAt = 3))

            return Stream.of(
                Arguments.of(
                    (author1Tweets + author2Tweets).shuffled(),
                    mapOf(
                        author1 to author1Tweets,
                        author2 to author2Tweets
                    )
                ),
                generateDifferentAuthorIdSameCreatedAtCase(author1Tweets, author3Tweets, author1, author3CreatedAtSameAsAuthor1),
                Arguments.of(listOf<Tweet>(), emptyMap<Author, List<Tweet>>()),
            )
        }

        private fun generateDifferentAuthorIdSameCreatedAtCase(
            author1Tweets: List<Tweet>,
            author3Tweets: List<Tweet>,
            author1: Author,
            author3CreatedAtSameAsAuthor1: Author
        ): Arguments? = Arguments.of(
            author1Tweets + author3Tweets,
            mapOf(
                author1 to author1Tweets,
                author3CreatedAtSameAsAuthor1 to author3Tweets
            )
        )
    }
}