package com.riis.androidarduino.bt.test;

import com.jayway.android.robotium.solo.Solo;
import android.test.ActivityInstrumentationTestCase2;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MainTest extends ActivityInstrumentationTestCase2 {
	private static final String TARGET_PACKAGE_ID="com.riis.androidarduino.bt";
	private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "com.riis.androidarduino.bt.MainActivity";
	private static Class launcherActivityClass;
	
	private Solo solo;
	
	static {
		try {
			launcherActivityClass = Class.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
		} catch (ClassNotFoundException e){
			throw new RuntimeException(e);
		}
	}
	
	public MainTest()throws ClassNotFoundException {
		super(TARGET_PACKAGE_ID, launcherActivityClass);
	}
	
	@Override
	protected void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	public void testUIElementsExist() {
		assertTrue(solo.searchText("Connect"));
		assertTrue(solo.searchText("Disconnect"));
		
		assertTrue(solo.searchText("Turn Red On"));
		assertTrue(solo.searchText("Turn Yellow On"));
		assertTrue(solo.searchText("Turn Green On"));
	}
	
	public void testRedButtonToggles() {
		solo.clickOnButton("Turn Red On");
		assertTrue(solo.searchButton("Turn Red Off"));
		
		solo.clickOnButton("Turn Red Off");
		assertTrue(solo.searchButton("Turn Red On"));
	}
	
	public void testYellowButtonToggles() {
		solo.clickOnButton("Turn Yellow On");
		assertTrue(solo.searchButton("Turn Yellow Off"));
		

		solo.clickOnButton("Turn Yellow Off");
		assertTrue(solo.searchButton("Turn Yellow On"));
	}
	
	public void testGreenButtonToggles() {
		solo.clickOnButton("Turn Green On");
		assertTrue(solo.searchButton("Turn Green Off"));
		

		solo.clickOnButton("Turn Green Off");
		assertTrue(solo.searchButton("Turn Green On"));
	}
	
	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}
}