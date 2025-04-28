package com.jian.tangthucac;

import androidx.annotation.NonNull;

import com.jian.tangthucac.model.Story;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Helper utilities for testing Firebase operations
 */
public class FirebaseTestUtils {

    /**
     * Helps synchronize async Firebase tasks for testing
     */
    public static <T> T waitForTaskCompletion(Task<T> task) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final T[] result = (T[]) new Object[1];
        final Exception[] exception = new Exception[1];

        task.addOnCompleteListener(new OnCompleteListener<T>() {
            @Override
            public void onComplete(@NonNull Task<T> task) {
                if (task.isSuccessful()) {
                    result[0] = task.getResult();
                } else {
                    exception[0] = task.getException();
                }
                latch.countDown();
            }
        });

        // Wait up to 5 seconds for the task to complete
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Task timed out");
        }

        if (exception[0] != null) {
            throw exception[0];
        }

        return result[0];
    }

    /**
     * Creates sample story data for tests
     */
    public static Story createSampleStory() {
        Story story = new Story();
        story.setId("test_story_id");
        story.setTitle("Test Story");
        story.setAuthor("Test Author");
        story.setViews(100);
        story.setGenre("Fantasy");
        story.setHot(true);
        return story;
    }
}