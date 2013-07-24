package spotify.sikuli;

/* 
 */

import static org.junit.Assert.*;
import static org.sikuli.api.API.browse;

import org.sikuli.api.*;
import org.sikuli.api.robot.Key;
import org.sikuli.api.robot.Env;
import org.sikuli.api.robot.Keyboard;
import org.sikuli.api.robot.Mouse;
import org.sikuli.api.robot.desktop.DesktopKeyboard;
import org.sikuli.api.robot.desktop.DesktopMouse;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ClientTest {

	private String invalidLogin_;
	private String loginFailedMessage_;
	private String validLogin_;
	private String password_;

	// Could be used if text search works
	// private String userName_;

	private String[] searchTerms_;
	private String wrongSearchTerm_;

	// path to Spotify Application
	private String pathToApp;

	private ScreenRegion screen;
	private Keyboard keyboard;
	private Mouse mouse;

	// TODO: move to setUp()
	public ClientTest() throws IOException {

		Properties prop = new Properties();
		InputStream is = null;

		is = getClass()
				.getResourceAsStream(TestProperties.TEST_PROPERTIES_PATH);
		prop.load(is);
		is.close();

		pathToApp = Env.isMac() ? prop.getProperty("macPathToApp") : prop
				.getProperty("windowsPathToApp");

		invalidLogin_ = prop.getProperty(TestProperties.INVALID_LOGIN);
		loginFailedMessage_ = prop
				.getProperty(TestProperties.LOGIN_FAILED_MESSAGE);
		validLogin_ = prop.getProperty(TestProperties.VALID_LOGIN);
		password_ = prop.getProperty(TestProperties.PASSWORD);
		// userName_ = prop.getProperty(TestProperties.USER_NAME);
		wrongSearchTerm_ = prop.getProperty(TestProperties.WRONG_SEARCH_TEAM);
		searchTerms_ = prop.getProperty(TestProperties.SEARCH_TERMS).split(",");

		screen = new DesktopScreenRegion();
		keyboard = new DesktopKeyboard();
		mouse = new DesktopMouse();
	}

	@Before
	public void setUp() throws MalformedURLException {
		browse(new URL(pathToApp));
	}

	@Test
	public void verifyInvalidLoginScenario() {

		loginUser(invalidLogin_, password_);
		URL imageURL = Env.isMac() ? Patterns.LoginFailedImage : Patterns.LoginFailedImage_Windows;

		assertScreenRegionForImage(imageURL,
				"Expected 'Login Failed' image was not found on the screen",
				true);

		TextTarget text = new TextTarget(loginFailedMessage_);
		ScreenRegion sr = screen.find(text);
		assertNotNull("[" + TestProperties.LOGIN_FAILED_MESSAGE
				+ "] was not found on the screen", sr);
	}

	@Ignore
	@Test
	public void verifyValidLoginScenario() {

		loginUser(validLogin_, password_);
		/*
		 * Unfortunately text recognition doesn't work appropriately when there
		 * is a lot of images and video on the screen. Therefore instead of
		 * verifying that user name appeared TextTarget text = new
		 * TextTarget(userName_); ScreenRegion sr = screen.wait(text, 5000);
		 * assertNotNull("User Name was not found on the screen", sr); we will
		 * be looking for search field image
		 */
	}

	@Ignore
	@Test
	public void verifySearchScenario() {

		loginUser(validLogin_, password_);

		for (int i = 0; i < searchTerms_.length; i++) {
			String term = searchTerms_[i];
			processSearch(term);
			assertScreenRegionForImage(Patterns.EmptySearchImage,
					"Not empty search result expected. Please verify ["
							+ TestProperties.WRONG_SEARCH_TEAM
							+ "] for your test.", false);

			assertScreenRegionForImage(Patterns.StarIcon,
					"There is no songs in the list", true);
		}
	}

	@Ignore
	@Test
	public void verifyEmptySearchScenario() {

		loginUser(validLogin_, password_);

		processSearch(wrongSearchTerm_);
		assertScreenRegionForImage(Patterns.EmptySearchImage,
				"Magnifier image is absent", true);
	}

	@Ignore
	@Test
	public void verifyPlayingSongsWorks() throws InterruptedException {

		loginUser(validLogin_, password_);

		processSearch(searchTerms_[0]);
		ScreenRegion sr = assertScreenRegionForImage(Patterns.StarIcon,
				"There is no songs in the list to play", true);
		Rectangle r = sr.getBounds();
		ScreenRegion newRegion = new DesktopScreenRegion(r.x + r.width, r.y,
				r.width, r.height);
		mouse.doubleClick(newRegion.getCenter());

		Thread.sleep(3000);

		sr = assertScreenRegionForImage(Patterns.PauseButtonImage,
				"'Pause' button was not found on the screen", true);
		mouse.click(sr.getCenter());
	}

	private void loginUser(String login, String password) {

		assertScreenRegionForImage(Patterns.Logo,
				"Logo was not found on the screen", true);

		keyboardCombination(Key.CMD, "a");
		keyboard.type(Key.BACKSPACE);
		keyboard.type(login);
		keyboard.type(Key.TAB);
		keyboard.type(password);

		ScreenRegion button = assertScreenRegionForImage(
				Patterns.LoginButtonImage,
				"Login button was not found on the screen", true);
		mouse.click(button.getCenter());
	}

	private ScreenRegion assertScreenRegionForImage(URL targetURL,
			String assertMessage, boolean NotNull) {
		ImageTarget imageTarget = new ImageTarget(targetURL);
		ScreenRegion sr = screen.wait(imageTarget, 3000);

		if (NotNull)
			assertNotNull(assertMessage, sr);
		else
			assertNull(assertMessage, sr);
		return sr;
	}

	private void processSearch(String inputText) {
		ScreenRegion searchField = assertScreenRegionForImage(
				Patterns.SearchFieldImage,
				"Search field was not found on the screen", true);
		mouse.click(searchField.getCenter());

		keyboardCombination(Key.CMD, "a");
		keyboard.type(Key.BACKSPACE);

		keyboard.type(inputText);
		keyboard.type(Key.ENTER);
	}

	private void keyboardCombination(String key1, String key2) {

		if (key1.equals(Key.CMD) && !Env.isMac())
			key1 = Key.CTRL;
		keyboard.keyDown(key1); // push "key" button
		keyboard.type(key2);
		keyboard.keyUp(key1); // release "key" button
	}

	@After
	public void tearDown() throws InterruptedException {

		if (Env.isMac())
			keyboardCombination(Key.CMD, "q");
		else {
			// logout
			keyboard.keyDown(Key.CTRL);
			keyboard.keyDown(Key.SHIFT);
			keyboard.type("w");
			keyboard.keyUp(Key.CTRL);
			keyboard.keyUp(Key.SHIFT);
			// quit application
			Thread.sleep(1000);
			keyboardCombination(Key.ALT, Key.F4);

		}
		Thread.sleep(1000);
	}
}