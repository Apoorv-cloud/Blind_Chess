package io.github.wenzla.testapp;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.PositionAssertions.isCompletelyAbove;
import static android.support.test.espresso.assertion.PositionAssertions.isCompletelyBelow;
import static android.support.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;

public class GameActivitiesInstrumentationTest {

    @Rule
    public ActivityTestRule<LocalMultiplayerGame> mActivityTestRule =
            new ActivityTestRule<>(LocalMultiplayerGame.class);


    @Test
    public void GameActivityTextLocationTest() {
        onView(withId(R.id.resetBoard)).check(isCompletelyBelow(withId(R.id.status)));
        onView(withId(R.id.status)).check(isCompletelyAbove(withId(R.id.button2)));
        onView(withId(R.id.button2)).check(isCompletelyLeftOf(withId(R.id.resetBoard)));
    }


}
