package com.riis.androidarduino.rs232.test;

import com.jayway.android.robotium.solo.Solo;
import android.test.ActivityInstrumentationTestCase2;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MainTest extends ActivityInstrumentationTestCase2 {
	private static final String TARGET_PACKAGE_ID="com.riis.androidarduino.rs232";
	private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "com.riis.androidarduino.rs232.MainActivity";
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
		assertTrue(solo.getCurrentEditTexts().size() == 1);
		assertTrue(solo.getCurrentButtons().size() == 1);
		assertTrue(solo.searchText("Enter a message to send to the Arduino"));
	}
	
	public void testMessageBoxClearedAfterSend() {
		solo.enterText(0, "Test String");
		solo.clickOnButton("Send");
		
		assertTrue(solo.getCurrentEditTexts().get(0).getText().toString().equals(""));
	}
	
	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}
}