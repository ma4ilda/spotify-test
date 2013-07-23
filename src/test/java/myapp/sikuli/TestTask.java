package myapp.sikuli;

/**/

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

public class TestTask {

	// testing data is set in /test/resources/test.properties file
	private String invalidLogin_;
	private String loginFailedMessage_;
	private String validLogin_;
	private String password_;
	private String userName_;
	private String[] searchTerms_;
	private String wrongSearchTerm_;
	private static String macPathToApp_;
	private static String windowsPathToApp_;

	// path to Spotify Application
	private String pathToApp;

	private ScreenRegion screen;
	private Keyboard keyboard;
	private Mouse mouse;

	// Constructor
	public TestTask() throws IOException {
		InputStream is = getClass().getResourceAsStream("./test.properties");
		Properties prop = new Properties();
		try {
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}

		pathToApp = Env.isMac() ? prop.getProperty("macPathToApp") : prop
				.getProperty("windowsPathToApp");

		invalidLogin_ = prop.getProperty("invalidLogin");
		loginFailedMessage_ = prop.getProperty("loginFailedMessage");
		validLogin_ = prop.getProperty("validLogin");
		password_ = prop.getProperty("password");
		userName_ = prop.getProperty("userName");
		wrongSearchTerm_ = prop.getProperty("wrongSearchTerm");
		searchTerms_ = prop.getProperty("searchTerms").split(",");

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
		Target imageTarget = new ImageTarget(Patterns.Logo);
		// wait for Login window to open
		ScreenRegion logo = screen.wait(imageTarget, 3000);

		// verify that Login window is opened by check of Logo image presence
		assertNotNull(logo);
		loginUser(invalidLogin_, password_);

		// verify both image ways "Login Failed" image and message appeared on
		// the screen
		imageTarget = new ImageTarget(Patterns.LoginFailedImage);
		ScreenRegion sr = screen.wait(imageTarget, 3000);
		assertNotNull(sr);
		TextTarget text = new TextTarget(loginFailedMessage_);

		sr = screen.find(text);

		assertNotNull(sr);
		// TODO:remove
		mouse.click(sr.getCenter());

	}

	@Test
	public void verifyValidLoginSacenario() {
		Target imageTarget = new ImageTarget(Patterns.Logo);
		// wait for Login window to open
		ScreenRegion logo = screen.wait(imageTarget, 3000);

		// verify that Login window is opened by check of Logo image presence
		assertNotNull(logo);
		loginUser(validLogin_, password_);
		BufferedImage bi = screen.capture();
		try {
			javax.imageio.ImageIO.write(bi, "png", new java.io.File(
					"SavedCaptuedImage.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * looks like text recognition doesn't work in sikuli so instead of
		 * verifying user was logged in and user name appeared TextTarget text =
		 * new TextTarget(userName_); ScreenRegion sr = screen.wait(text, 5000);
		 * assertNotNull("User Name was not found on the screen", sr); will be
		 * looking for search field image
		 */
		imageTarget = new ImageTarget(Patterns.SearchFieldImage);
		ScreenRegion sr = screen.wait(imageTarget, 3000);
		assertNotNull("Search field was not found", sr);

		// TODO:remove
		// mouse.click(sr.getCenter());

	}

	@Test
	public void verifySearchSacenario() {
		System.out.println("verifySearchSacenario");
		Target imageTarget = new ImageTarget(Patterns.Logo);
		// wait for Login window to open
		ScreenRegion logo = screen.wait(imageTarget, 3000);

		// verify that Login window is opened by check of Logo image presence
		assertNotNull(logo);
		loginUser(validLogin_, password_);
		imageTarget = new ImageTarget(Patterns.SearchFieldImage);
		ScreenRegion searchField = screen.wait(imageTarget, 3000);
		assertNotNull("Search field was not found", searchField);

		ImageTarget emptySearchImage = new ImageTarget(
				Patterns.EmptySearchImage);
		ImageTarget starIcon = new ImageTarget(Patterns.StarIcon);

		for (int i = 0; i < searchTerms_.length; i++) {
			String term = searchTerms_[i];
			processSearch(searchField, term);
			ScreenRegion sr = screen.wait(emptySearchImage, 3000);
			assertNull("Not empty search result expected", sr);
			// verify at least one song precense in the result list
			sr = screen.wait(starIcon, 5000);
			assertNull("No songs in the list", starIcon);

			// TODO:Remove
			mouse.click(sr.getCenter());
		}
	}

	@Test
	public void verifyEmptySearchSacenario() {
		System.out.println("verifyEmptySearchSacenario");
		Target imageTarget = new ImageTarget(Patterns.Logo);
		// wait for Login window to open
		ScreenRegion logo = screen.wait(imageTarget, 3000);

		// verify that Login window is opened by check of Logo image presence
		assertNotNull(logo);
		loginUser(validLogin_, password_);
		imageTarget = new ImageTarget(Patterns.SearchFieldImage);
		ScreenRegion searchField = screen.wait(imageTarget, 3000);
		assertNotNull("Search field was not found", searchField);

		ImageTarget emptySearchImage = new ImageTarget(
				Patterns.EmptySearchImage);
		processSearch(searchField, wrongSearchTerm_);
		ScreenRegion sr = screen.wait(emptySearchImage, 3000);
		assertNotNull(sr);
	}

	@Test
	public void verifyPlayingSongsWorks() throws InterruptedException {

		Target imageTarget = new ImageTarget(Patterns.Logo);
		// wait for Login window to open
		ScreenRegion logo = screen.wait(imageTarget, 3000);

		// verify that Login window is opened by check of Logo image presence
		assertNotNull(logo);
		loginUser(validLogin_, password_);

		imageTarget = new ImageTarget(Patterns.SearchFieldImage);
		ScreenRegion searchField = screen.wait(imageTarget, 3000);
		assertNotNull("Search field was not found", searchField);

		Target playButton = new ImageTarget(Patterns.PlayButtonImage);
		Target pauseButton = new ImageTarget(Patterns.PauseButtonImage);

		ScreenRegion sr = screen.wait(playButton, 5000);
		mouse.click(sr.getCenter());
		
		//let song to play 3 seconds
		Thread.sleep(3000);
		sr = screen.find(pauseButton);
		mouse.click(sr.getCenter());

	}

	private void loginUser(String login, String password) {
		// for any case select all text(CMD+A) in the Login input field and
		// remove it

		keyboardCombimation(Key.CMD, "a");
		keyboard.type(Key.BACKSPACE);

		// input login and password
		keyboard.type(login);
		keyboard.type(Key.TAB);
		keyboard.type(password);

		// find "Log In" button on the screen and click it
		ImageTarget imageTarget = new ImageTarget(Patterns.LoginButtonImage);

		ScreenRegion button = screen.find(imageTarget);
		mouse.click(button.getCenter());
	}

	private void processSearch(ScreenRegion searchField, String inputText) {
		mouse.click(searchField.getCenter());
		// select all text in the field and remove it
		keyboardCombimation(Key.CMD, "a");
		keyboard.type(Key.BACKSPACE);

		// input new text for search and click enter
		keyboard.type(inputText);
		keyboard.type(Key.ENTER);
	}

	private void keyboardCombimation(String key, String letter) {
		keyboard.keyDown(key); // push button "key"
		keyboard.type(letter);
		keyboard.keyUp(key); // release button "key"
	}

	@After
	public void tearDown() throws InterruptedException {
		// close application
		keyboardCombimation(Key.CMD, "q");
		//give time for application to be closed
		Thread.sleep(2000);

	}

}