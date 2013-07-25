package spotify.sikuli;

/**ClientTest is JUnit test case written for Spotify Client GUI-test.
 * It uses sikuli.api library that provides image-based GUI automation functionality. 
 * Unfortunately text and image recognition is still experimental and under development
 * therefore it could result in a strange behavior of the ClientTest functionality.
 * All test methods were implemented to be fully independent and separate. In this way 
 * each method scenario could be easily changed and should not influence others.
 * ClientTest verifies five different scenarios using set of simple sub-scenarios.
 * - user logs in with invalid account
 * - user logs in with valid free account
 * - user processes a search with non-empty result
 * - user processes a search with empty result
 * - user plays the first available song from search result
 * 
 * ClientTest was tested and implemented for two versions of Windows and Mac OS operating systems. 
 *  @author      Pukhyr Anastasiia
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
import org.junit.Test;

public class ClientTest {

	private String invalidLogin_;
	private String loginFailedMessage_;
	private String validLogin_;
	private String password_;
	private String userName_;
	private String[] searchTerms_;
	private String wrongSearchTerm_;

	private String pathToApp;

	private ScreenRegion screen;
	private Keyboard keyboard;
	private Mouse mouse;

	@Before
	public void setUp() throws MalformedURLException, IOException {
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
		userName_ = prop.getProperty(TestProperties.USER_NAME);
		wrongSearchTerm_ = prop.getProperty(TestProperties.WRONG_SEARCH_TEAM);
		searchTerms_ = prop.getProperty(TestProperties.SEARCH_TERMS).split(",");

		screen = new DesktopScreenRegion();
		keyboard = new DesktopKeyboard();
		mouse = new DesktopMouse();
		browse(new URL(pathToApp));
	}

	@Test
	public void verifyInvalidLoginScenario() {

		loginUser(invalidLogin_, password_);
		URL imageURL = Env.isMac() ? Patterns.LoginFailedImage
				: Patterns.LoginFailedImage_Windows;

		assertScreenRegionForImage(imageURL,
				"Expected 'Login Failed' image was not found on the screen",
				true);

		TextTarget text = new TextTarget(loginFailedMessage_);
		ScreenRegion sr = screen.find(text);
		assertNotNull("[" + TestProperties.LOGIN_FAILED_MESSAGE
				+ "] was not found on the screen", sr);
	}

	@Test
	public void verifyValidLoginScenario() {

		loginUser(validLogin_, password_);
		ScreenRegion fieldRegion = assertScreenRegionForImage(
				Patterns.SearchFieldImage,
				"Search field was not found on the screen", true);
		Rectangle r = fieldRegion.getBounds();
		ScreenRegion newRegionForSearch = new DesktopScreenRegion(r.x, r.y,
				screen.getBounds().width, r.height * 2);
		TextTarget text = new TextTarget(userName_);
		ScreenRegion suserNameRegion = newRegionForSearch.find(text);
		assertNotNull("Username was not found", suserNameRegion);
	}

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

	@Test
	public void verifyEmptySearchScenario() {

		loginUser(validLogin_, password_);

		processSearch(wrongSearchTerm_);
		assertScreenRegionForImage(Patterns.EmptySearchImage,
				"Magnifier image is absent", true);
	}

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

	/**
	 * Tries to find logotype on the screen specified in Patterns.Logo URL. If
	 * logotype is found inputs user login and password using global keyboard
	 * object. Clicks login button specified in Patterns.LoginButtonImage URL.
	 * 
	 * @param login
	 *            - used for input in login field
	 * @param password
	 *            - used for input in password field
	 */
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

	/**
	 * Creates ImageTarget for the given image URL and tries to find it on the
	 * screen. If corresponding screen region was not found calls assertion
	 * method. imageURL argument must specify absolute path to the image.
	 * 
	 * @param imageURL
	 *            an absolute URL given the base location of the image
	 * @param assertMessage
	 *            message to be used in assert
	 * @param NotNull
	 *            specifies which assert method should be called (if true then
	 *            assertNotNull, if false then assertNull)
	 * @return screen region for the given image
	 */
	private ScreenRegion assertScreenRegionForImage(URL imageURL,
			String assertMessage, boolean NotNull) {
		ImageTarget imageTarget = new ImageTarget(imageURL);
		ScreenRegion sr = screen.wait(imageTarget, 3000);

		if (NotNull)
			assertNotNull(assertMessage, sr);
		else
			assertNull(assertMessage, sr);
		return sr;
	}

	/**
	 * Tries to find search field on the screen by the image URL specified in
	 * Patterns.SearchFieldImage variable. If field was found types given
	 * inputText and starts the search.
	 * 
	 * @param inputText
	 *            term for the search
	 */
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

	/**
	 * Types double key combination using {@link Keyboard} object. Example: CMD
	 * + C; CTRL + V. If current OS is Windows CMD key will be changed to CTRL
	 * 
	 * @param key1
	 *            first key to be hold
	 * @param key2
	 *            second key to be pushed
	 */
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
		Thread.sleep(2000);
	}
}