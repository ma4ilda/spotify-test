package myapp.sikuli;

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

import java.awt.image.BufferedImage;
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
	
	//Could be used if text search working
	//private String userName_;
	
	private String[] searchTerms_;
	private String wrongSearchTerm_;
	// TODO:remove
	// private static String macPathToApp_;
	// private static String windowsPathToApp_;

	// path to Spotify Application
	private String pathToApp;

	private ScreenRegion screen;
	private Keyboard keyboard;
	private Mouse mouse;

	// TODO: move to setUp()
	public ClientTest() throws IOException {
		
		Properties prop = new Properties();
		InputStream is = null;

		is = getClass().getResourceAsStream("./test.properties");
		prop.load(is);
		is.close();

		pathToApp = Env.isMac() ? prop.getProperty("macPathToApp") : prop
				.getProperty("windowsPathToApp");

		invalidLogin_ = prop.getProperty(TestProperties.INVALID_LOGIN);
		loginFailedMessage_ = prop.getProperty(TestProperties.LOGIN_FAILED_MESSAGE);
		validLogin_ = prop.getProperty(TestProperties.VALID_LOGIN);
		password_ = prop.getProperty(TestProperties.PASSWORD);
		//userName_ = prop.getProperty(TestProperties.USER_NAME);
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

	@Ignore
	@Test
	public void verifyInvalidLoginScenario() {

		loginUser(invalidLogin_, password_);

		Target imageTarget = new ImageTarget(Patterns.LoginFailedImage);
		ScreenRegion sr = screen.wait(imageTarget, 3000);
		assertNotNull(sr);
		TextTarget text = new TextTarget(loginFailedMessage_);

		sr = screen.find(text);

		assertNotNull(sr);
	}

	@Test
	public void verifyValidLoginScenario() {

		loginUser(validLogin_, password_);

	}

	@Test
	public void verifySearchScenario() {
		System.out.println("verifySearchSacenario");

		loginUser(validLogin_, password_);
		ScreenRegion searchField = getSearchField();

		ImageTarget emptySearchImage = new ImageTarget(
				Patterns.EmptySearchImage);
		ImageTarget starIcon = new ImageTarget(Patterns.StarIcon);

		for (int i = 0; i < searchTerms_.length; i++) {
			String term = searchTerms_[i];
			processSearch(searchField, term);
			ScreenRegion sr = screen.wait(emptySearchImage, 3000);
			assertNull("Not empty search result expected", sr);

			sr = screen.wait(starIcon, 5000);
			assertNull("No songs in the list", starIcon);
		}
	}

	@Test
	public void verifyEmptySearchScenario() {
		System.out.println("verifyEmptySearchSacenario");

		loginUser(validLogin_, password_);
		ScreenRegion searchField = getSearchField();

		ImageTarget emptySearchImage = new ImageTarget(
				Patterns.EmptySearchImage);
		processSearch(searchField, wrongSearchTerm_);
		ScreenRegion sr = screen.wait(emptySearchImage, 3000);
		assertNotNull(sr);
	}

	@Test
	public void verifyPlayingSongsWorks() throws InterruptedException {

		loginUser(validLogin_, password_);

		Target playButton = new ImageTarget(Patterns.PlayButtonImage);
		Target pauseButton = new ImageTarget(Patterns.PauseButtonImage);

		ScreenRegion sr = screen.wait(playButton, 5000);
		mouse.click(sr.getCenter());

		Thread.sleep(3000);
		sr = screen.find(pauseButton);
		mouse.click(sr.getCenter());
	}

	private void loginUser(String login, String password) {

		Target imageTarget = new ImageTarget(Patterns.Logo);
		ScreenRegion logo = screen.wait(imageTarget, 3000);
		assertNotNull(logo);

		keyboardCombination(Key.CMD, "a");
		keyboard.type(Key.BACKSPACE);
		keyboard.type(login);
		keyboard.type(Key.TAB);
		keyboard.type(password);

		imageTarget = new ImageTarget(Patterns.LoginButtonImage);

		ScreenRegion button = screen.find(imageTarget);
		mouse.click(button.getCenter());

		/*
		 * Unfortunately text recognition doesn't work appropriately when there
		 * is a lot of images and video on the screen. Therefore instead of
		 * verifying that user name appeared TextTarget text = new
		 * TextTarget(userName_); ScreenRegion sr = screen.wait(text, 5000);
		 * assertNotNull("User Name was not found on the screen", sr); we will
		 * be looking for search field image
		 */
		// TODO: remove duplicates
		imageTarget = new ImageTarget(Patterns.SearchFieldImage);
		ScreenRegion sr = screen.wait(imageTarget, 3000);

		assertNotNull("Search field was not found", sr);
	}

	private void processSearch(ScreenRegion searchField, String inputText) {
		mouse.click(searchField.getCenter());

		keyboardCombination(Key.CMD, "a");
		keyboard.type(Key.BACKSPACE);

		keyboard.type(inputText);
		keyboard.type(Key.ENTER);
	}

	private void keyboardCombination(String key, String letter) {
		keyboard.keyDown(key); // push "key" button
		keyboard.type(letter);
		keyboard.keyUp(key); // release "key" button
	}
	// TODO Remove. method for regions testing
	private void screenCapture(ScreenRegion sr) {
		BufferedImage bi = sr.capture();
		try {
			javax.imageio.ImageIO.write(bi, "png", new java.io.File(
					"SavedCaptuedImage" + sr.toString().trim() + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ScreenRegion getSearchField() {
		Target imageTarget = new ImageTarget(Patterns.SearchFieldImage);
		ScreenRegion searchField = screen.wait(imageTarget, 3000);
		assertNotNull("Search field was not found", searchField);
		return searchField;
	}

	@After
	public void tearDown() throws InterruptedException {

		keyboardCombination(Key.CMD, "q");
		Thread.sleep(2000);
	}

}