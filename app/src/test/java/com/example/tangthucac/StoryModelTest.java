package com.jian.tangthucac;

import com.jian.tangthucac.model.Story;
import com.jian.tangthucac.model.Chapter;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class StoryModelTest {
    private Story testStory;
    private Map<String, Chapter> chapters;

    @Before
    public void setUp() {
        // Create a test Story object with sample data
        testStory = new Story();
        testStory.setId("story123");
        testStory.setTitle("Test Story");
        testStory.setAuthor("Test Author");

        // Add some chapters
        chapters = new HashMap<>();
        Chapter chapter1 = new Chapter();
        chapters.put("chapter1", chapter1);
    }

    @Test
    public void testStoryPropertiesAreCorrect() {
        assertEquals("story123", testStory.getId());
        assertEquals("Test Story", testStory.getTitle());
        assertEquals("Test Author", testStory.getAuthor());
    }

    @Test
    public void testChaptersMap() {
        testStory.setChapters(chapters);
        assertEquals(1, testStory.getChapters().size());
        assertTrue(testStory.getChapters().containsKey("chapter1"));
    }
}