package com.patrykglow.twitterconnector.domain

fun List<Tweet>.groupByAuthorSortAllAscending(): Map<Author, List<Tweet>> =
    this.groupBy { it.author }
        .toSortedMap(compareBy<Author> { it.createdAt }.thenBy { it.id.raw })
        .mapValues { (_, tweets) ->
            tweets.sortedWith(compareBy<Tweet> { it.createdAt }.thenBy { it.id.raw })
        }