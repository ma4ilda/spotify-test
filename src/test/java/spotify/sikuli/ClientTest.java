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

import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {

    TestEnvironment testEnv;

    @Before
    public void setUp() {
	testEnv = TestEnvironment.getInstance();
	try {
	    browse(new URL(TestProperties.pathToApp));
	} catch (MalformedURLException e) {
	   new MalformedURLException("Wrong path to application. Check properties file.");
	}
    }

    @Test
    public void verifyInvalidLoginScenario() {

	loginUser(TestProperties.invalidLogin, TestProperties.password);
	URL imageURL = Env.isMac() ? testEnv
		.getImageURL(Patterns.LoginFailedImage) : testEnv
		.getImageURL(Patterns.LoginFailedImage_Windows);

	validateScreenRegionForImage(imageURL,
		"Expected 'Login Failed' image was not found on the screen",
		true);

	TextTarget text = new TextTarget(TestProperties.loginFailedMessage);
	ScreenRegion sr = testEnv.getScreen().find(text);
	assertNotNull("[" + TestProperties.loginFailedMessage
		+ "] was not found on the screen", sr);
    }

    @Test
    public void verifyValidLoginScenario() {

	loginUser(TestProperties.validLogin, TestProperties.password);
	ScreenRegion fieldRegion = validateScreenRegionForImage(
		testEnv.getImageURL(Patterns.SearchFieldImage),
		"Search field was not found on the screen", true);
	Rectangle r = fieldRegion.getBounds();
	ScreenRegion newRegionForSearch = new DesktopScreenRegion(r.x, r.y,
		testEnv.getScreen().getBounds().width, r.height * 2);
	TextTarget text = new TextTarget(TestProperties.userName);
	ScreenRegion suserNameRegion = newRegionForSearch.find(text);
	assertNotNull("Username was not found", suserNameRegion);
    }


    @Test
    public void verifySearchScenario() {

	loginUser(TestProperties.validLogin, TestProperties.password);
	String[] searchTerms = testEnv.getSearchTerms();
	for (int i = 0; i < searchTerms.length; i++) {
	    String term = searchTerms[i];
	    processSearch(term);
	    validateScreenRegionForImage(
		    testEnv.getImageURL(Patterns.EmptySearchImage),
		    "Not empty search result expected. Please verify search terms for your test.",
		    false);

	    validateScreenRegionForImage(
		    testEnv.getImageURL(Patterns.StarIcon),
		    "There is no songs in the list", true);
	}
    }

  
    @Test
    public void verifyEmptySearchScenario() {

	loginUser(TestProperties.validLogin, TestProperties.password);

	processSearch(TestProperties.wrongSearchTerm);
	validateScreenRegionForImage(
		testEnv.getImageURL(Patterns.EmptySearchImage),
		"Magnifier image is absent", true);
    }

   
    @Test
    public void verifyPlayingSongsWorks() throws InterruptedException {
	loginUser(TestProperties.validLogin, TestProperties.password);

	processSearch(testEnv.getSearchTerms()[0]);
	ScreenRegion sr = validateScreenRegionForImage(
		testEnv.getImageURL(Patterns.StarIcon),
		"There is no songs in the list to play", true);
	Rectangle r = sr.getBounds();
	ScreenRegion newRegion = new DesktopScreenRegion(r.x + r.width, r.y,
		r.width, r.height);
	Mouse mouse = testEnv.getMouse();
	mouse.doubleClick(newRegion.getCenter());

	Thread.sleep(3000);

	sr = validateScreenRegionForImage(
		testEnv.getImageURL(Patterns.PauseButtonImage),
		"'Pause' button was not found on the screen", true);
	mouse.click(sr.getCenter());
    }

    /**
     * Tries to find logotype on the screen specified in
     * testEnv.getImageURL(Patterns.Logo URL. If logotype is found inputs user
     * login and password using global keyboard object. Clicks login button
     * specified in testEnv.getImageURL(Patterns.LoginButtonImage URL.
     * 
     * @param login  used for input in login field
     * @param password used for input in password field
     */
    private void loginUser(String login, String password) {

	validateScreenRegionForImage(testEnv.getImageURL(Patterns.Logo),
		"Logo was not found on the screen", true);
	Keyboard keyboard = testEnv.getKeyboard();
	keyboardCombination(Key.CMD, "a");
	keyboard.type(Key.BACKSPACE);
	keyboard.type(login);
	keyboard.type(Key.TAB);
	keyboard.type(password);

	ScreenRegion button = validateScreenRegionForImage(
		testEnv.getImageURL(Patterns.LoginButtonImage),
		"Login button was not found on the screen", true);
	testEnv.getMouse().click(button.getCenter());
    }

    /**
     * Creates ImageTarget for the given image URL and tries to find it on the
     * screen. If corresponding screen region was not found calls assertion
     * method. imageURL argument must specify absolute path to the image.
     * 
     * @param imageURL an absolute URL given the base location of the image
     * @param assertMessage  message to be used in assert
     * @param notNull specifies which assert method should be called (if true then
     *            assertNotNull, if false then assertNull)
     * @return screen region for the given image
     */
    private ScreenRegion validateScreenRegionForImage(URL imageURL,
	    String assertMessage, boolean notNull) {
	ImageTarget imageTarget = new ImageTarget(imageURL);
	ScreenRegion sr = testEnv.getScreen().wait(imageTarget, 3000);

	if (notNull)
	    assertNotNull(assertMessage, sr);
	else
	    assertNull(assertMessage, sr);
	return sr;
    }

    /**
     * Tries to find search field on the screen by the image specified in
     * Patterns.SearchFieldImage variable. If field was
     * found types given inputText and starts the search.
     * 
     * @param inputText term for the search
     */
    private void processSearch(String inputText) {
	ScreenRegion searchField = validateScreenRegionForImage(
		testEnv.getImageURL(Patterns.SearchFieldImage),
		"Search field was not found on the screen", true);
	testEnv.getMouse().click(searchField.getCenter());
	Keyboard keyboard = testEnv.getKeyboard();
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
	Keyboard keyboard = testEnv.getKeyboard();
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
	    Keyboard keyboard = testEnv.getKeyboard();
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