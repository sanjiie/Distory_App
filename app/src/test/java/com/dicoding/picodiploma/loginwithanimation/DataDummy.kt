package com.dicoding.picodiploma.loginwithanimation

import com.dicoding.picodiploma.loginwithanimation.story.ListStoryItem

object DataDummy {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                id = "id_$i",
                name = "Story $i",
                description = "Description $i",
                photoUrl = "https://example.com/photo$i.jpg",
                createdAt = "2024-11-25T00:00:00Z",
                lat = -6.2,
                lon = 106.8
            )
            items.add(story)
        }
        return items
    }
}
