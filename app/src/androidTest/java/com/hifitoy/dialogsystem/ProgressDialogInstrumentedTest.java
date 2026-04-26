package com.hifitoy.dialogsystem;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.ProgressBar;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.ProgressDialogTestHostActivity;
import com.hifitoy.hifitoycontrol.HiFiToyControl;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ProgressDialogInstrumentedTest {
    private static final int CONNECTION_READY_STATE = 4;
    private static final int DISCONNECTED_STATE = 0;

    @Rule
    public final ActivityScenarioRule<ProgressDialogTestHostActivity> activityRule =
            new ActivityScenarioRule<>(ProgressDialogTestHostActivity.class);

    @Before
    public void setUp() throws Exception {
        activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<ProgressDialogTestHostActivity>() {
            @Override
            public void perform(ProgressDialogTestHostActivity activity) {
                ApplicationContext.getInstance().setContext(activity);
                DialogSystem.getInstance().closeProgressDialog();
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        forceConnectionState(CONNECTION_READY_STATE);
    }

    @After
    public void tearDown() throws Exception {
        activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<ProgressDialogTestHostActivity>() {
            @Override
            public void perform(ProgressDialogTestHostActivity activity) {
                DialogSystem.getInstance().closeProgressDialog();
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        forceConnectionState(DISCONNECTED_STATE);
    }

    @Test
    public void dialogSystemProgressDialog_updatesAndCloses() {
        activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<ProgressDialogTestHostActivity>() {
            @Override
            public void perform(ProgressDialogTestHostActivity activity) {
                DialogSystem.getInstance().showProgressDialog("Sending Preset...", 100);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        onView(withText("Sending Preset...")).check(matches(isDisplayed()));
        onView(withId(R.id.progress_dialog_bar)).check(matches(isDisplayed()));

        activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<ProgressDialogTestHostActivity>() {
            @Override
            public void perform(ProgressDialogTestHostActivity activity) {
                DialogSystem.getInstance().updateProgressDialog(16);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        final ProgressBar progressBar = getProgressBar();
        assertNotNull(progressBar);
        assertTrue(progressBar.getProgress() > 0);

        activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<ProgressDialogTestHostActivity>() {
            @Override
            public void perform(ProgressDialogTestHostActivity activity) {
                DialogSystem.getInstance().closeProgressDialog();
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertNull(DialogSystem.getInstance().getProgressDialog());
        onView(withText("Sending Preset...")).check(doesNotExist());
    }

    @Test
    public void baseProgressDialog_rendersVisibleProgressBar() {
        final BaseProgressDialog[] holder = new BaseProgressDialog[1];

        activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<ProgressDialogTestHostActivity>() {
            @Override
            public void perform(ProgressDialogTestHostActivity activity) {
                BaseProgressDialog dialog = new BaseProgressDialog(activity);
                dialog.setTitle("Sending Preset...");
                dialog.setMax(100);
                dialog.setProgress(50);
                dialog.show();
                holder[0] = dialog;
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        try {
            final ProgressBar progressBar = holder[0].findViewById(R.id.progress_dialog_bar);
            assertNotNull(progressBar);
            assertTrue("ProgressBar should be visible", progressBar.isShown());

            int nonTransparentPixels = countNonTransparentPixels(progressBar);
            int minExpectedPixels = progressBar.getWidth() * 4;
            assertTrue("Progress bar rendering is too thin to be visible enough",
                    nonTransparentPixels >= minExpectedPixels);
        } finally {
            activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<ProgressDialogTestHostActivity>() {
                @Override
                public void perform(ProgressDialogTestHostActivity activity) {
                    if (holder[0] != null) {
                        holder[0].dismiss();
                    }
                }
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        }
    }

    private ProgressBar getProgressBar() {
        BaseProgressDialog dialog = DialogSystem.getInstance().getProgressDialog();
        assertNotNull(dialog);
        return dialog.findViewById(R.id.progress_dialog_bar);
    }

    private int countNonTransparentPixels(ProgressBar progressBar) {
        Bitmap bitmap = Bitmap.createBitmap(progressBar.getWidth(), progressBar.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        progressBar.draw(canvas);

        int nonTransparentPixels = 0;
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (Color.alpha(bitmap.getPixel(x, y)) > 0) {
                    nonTransparentPixels++;
                }
            }
        }
        return nonTransparentPixels;
    }

    private void forceConnectionState(int stateValue) throws Exception {
        HiFiToyControl control = HiFiToyControl.getInstance();
        Field stateField = HiFiToyControl.class.getDeclaredField("state");
        stateField.setAccessible(true);
        Object connectionState = stateField.get(control);

        Field innerStateField = connectionState.getClass().getDeclaredField("state");
        innerStateField.setAccessible(true);
        innerStateField.setInt(connectionState, stateValue);
    }
}
