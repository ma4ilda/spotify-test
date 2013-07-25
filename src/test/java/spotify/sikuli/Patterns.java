package spotify.sikuli;
import java.awt.Rectangle;
import java.net.URL;

import org.sikuli.api.DesktopScreenRegion;
import org.sikuli.api.ScreenRegion;
import org.sikuli.api.TextTarget;


public class Patterns {
	
	static final URL Logo = Patterns.class.getResource("./logo.png");
	static final URL LoginFailedImage = Patterns.class.getResource("./login_failed.png");
	static final URL LoginFailedImage_Windows = Patterns.class.getResource("./login_failed_Windows.png");
	static final URL LoginButtonImage = Patterns.class.getResource("./login_button.png");
	static final URL PlayButtonImage = Patterns.class.getResource("./play_button.png");
	static final URL SearchFieldImage = Patterns.class.getResource("./search.png");
	static final URL PauseButtonImage = Patterns.class.getResource("./pause_button.png");
	static final URL EmptySearchImage = Patterns.class.getResource("./empty_search.png");
	static final URL StarIcon = Patterns.class.getResource("./star_and_share.png");
}
