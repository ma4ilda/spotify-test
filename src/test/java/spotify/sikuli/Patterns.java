package spotify.sikuli;
import java.net.URL;

public interface Patterns {
	
	public String Logo = "./logo.png";
	public String LoginFailedImage = "./login_failed.png";
	public String LoginFailedImage_Windows = "./login_failed_Windows.png";
	public String LoginButtonImage = "./login_button.png";
	public String PlayButtonImage = "./play_button.png";
	public String SearchFieldImage = "./search.png";
	public String PauseButtonImage = "./pause_button.png";
	public String EmptySearchImage = "./empty_search.png";
	public String StarIcon = "./star_and_share.png";
	
	public URL getImageURL(String imageName);
}
