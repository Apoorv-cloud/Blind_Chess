package io.github.wenzla.testapp;

import android.graphics.Color;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class LocationTest {
    @Test
    public void locationTest() throws Exception {
        Location test = new Location(0, 6);
        assertEquals(0, test.rank());
        assertEquals(6, test.file());

    }
}