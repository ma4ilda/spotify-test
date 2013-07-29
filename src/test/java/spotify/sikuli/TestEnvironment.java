package spotify.sikuli;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.net.URL;

import org.sikuli.api.DesktopScreenRegion;
import org.sikuli.api.ScreenRegion;
import org.sikuli.api.robot.Env;
import org.sikuli.api.robot.Keyboard;
import org.sikuli.api.robot.Mouse;
import org.sikuli.api.robot.desktop.DesktopKeyboard;
import org.sikuli.api.robot.desktop.DesktopMouse;

public class TestEnvironment implements Patterns {

    private ScreenRegion screen;
    private Keyboard keyboard;
    private Mouse mouse;
    String[] searchTerms_;
    private String propertiesFile = "test.properties";

    private static volatile TestEnvironment instance = null;

    private TestEnvironment() {
	screen = new DesktopScreenRegion();
	keyboard = new DesktopKeyboard();
	mouse = new DesktopMouse();
	initTestPeroperties();
    }

    private void initTestPeroperties() {
	Properties prop = new Properties();
	InputStream is = null;

	is = getClass().getResourceAsStream(propertiesFile);

	try {
	    prop.load(is);
	    is.close();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
	TestProperties props = new TestProperties();
	String suffix = Env.isMac() ? ".mac" : ".win";
	Field[] fields = TestProperties.class.getDeclaredFields();
	try {
	    for (int i = 0; i < fields.length; i++) {
		Field field = fields[i];

		Object propValue = prop.get(field.getName() + suffix);
		if (propValue == null)
		    propValue = prop.get(field.getName());
		if(propValue == null){
		    throw new IllegalStateException( field.getName() + " variable is not set");
		}
		field.set(props, propValue);
	    }
	} catch (IllegalArgumentException e) {
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	}
	searchTerms_ = TestProperties.searchTerms.split(",");
    }

    public URL getImageURL(String imageName) {
	return TestEnvironment.class.getResource(imageName);
    }

    public static TestEnvironment getInstance() {
	if (instance == null) {
	    synchronized (TestEnvironment.class) {
		if (instance == null) {
		    instance = new TestEnvironment();
		}
	    }
	}
	return instance;
    }

    public String[] getSearchTerms() {
	return searchTerms_;
    }

    public Mouse getMouse() {
	return mouse;
    }

    public Keyboard getKeyboard() {
	return keyboard;
    }

    public ScreenRegion getScreen() {
	return screen;
    }
}
