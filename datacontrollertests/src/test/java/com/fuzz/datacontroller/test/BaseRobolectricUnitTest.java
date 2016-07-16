package com.fuzz.datacontroller.test;

import android.os.Build;

import com.fuzz.datacontroller.tests.BuildConfig;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Description:
 */
@Ignore
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class BaseRobolectricUnitTest {

    @Rule
    public final DBFlowTestRule dbFlowTestRule = DBFlowTestRule.create();
}
