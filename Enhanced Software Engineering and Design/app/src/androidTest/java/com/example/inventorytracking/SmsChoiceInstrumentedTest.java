package com.example.inventorytracking;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.junit.Assert.*;

/** Verifies saved choices only. These tests never call the SMS sending method. */
@RunWith(AndroidJUnit4.class)
public class SmsChoiceInstrumentedTest {
    private Context context;
    private String preferencesName;
    private SharedPreferences preferences;
    private SmsHelper helper;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertTrue(context.getPackageName().endsWith(".milestone4database"));
        preferencesName = "simple_sms_test_" + UUID.randomUUID();
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        helper = new SmsHelper(context, preferences);
    }

    @After
    public void tearDown() {
        if (preferencesName != null) context.deleteSharedPreferences(preferencesName);
    }

    @Test
    public void deniedChoiceSurvivesCreatingAnotherHelper() {
        assertFalse(helper.hasChoice("first_user"));
        assertFalse(helper.isEnabled("first_user"));
        helper.saveChoice("first_user", false);

        SmsHelper reopened = new SmsHelper(context, preferences);
        assertTrue(reopened.hasChoice("first_user"));
        assertFalse(reopened.isEnabled("first_user"));
    }

    @Test
    public void choicesAreKeptSeparateForEachUsername() {
        helper.saveChoice("first_user", true);
        assertTrue(helper.isEnabled("first_user"));
        assertFalse(helper.hasChoice("second_user"));

        helper.saveChoice("second_user", false);
        assertTrue(helper.isEnabled("first_user"));
        assertTrue(helper.hasChoice("second_user"));
        assertFalse(helper.isEnabled("second_user"));

        helper.saveChoice("first_user", false);
        assertFalse(helper.isEnabled("first_user"));
    }
}
