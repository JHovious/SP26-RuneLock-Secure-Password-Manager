
package test.controller;

import model.User;
import controller.JSONControl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.Rule;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;


/**
 * Unit tests for JSONControl
 * @author Justin Hovious
 */
public class JSONControlTest {
    
    private JSONControl controller;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Before
    public void setUp() {
        controller = new JSONControl();
    }
    
    // Ensures initial user is null 
    @Test
    public void constructor_userIsNullInitially() {
        assertNull(controller.getUser());
    }
    
    // Ensures that authorized device list is null when no login has occurred
    @Test
    public void getAuthorizedDevices_whenUserIsNull_returnsEmptyList() {
        // No login has occurred — user is null
        ArrayList<String> devices = controller.getAuthorizedDevices();
        assertNotNull(devices);
        assertThat(devices.size(), is(0));
    }
    
    
    // Ensures the the authorized device check returns false if a device is
    // not recognized.
    @Test
    public void isAuthorizedDevice_whenUserIsNull_returnsFalse() {
        assertFalse(controller.isAuthorizedDevice("any-device"));
    }
 
    // Ensures that the method does not throw null pointer exception when
    // an incorrect device ID is passed.
    @Test
    public void authorizeDevice_whenUserIsNull_doesNotThrow() {
        controller.authorizeDevice("device-xyz");
    }
 
    @Test
    public void removeAuthorizedDevice_whenUserIsNull_doesNotThrow() {
        controller.removeAuthorizedDevice("device-xyz");
    }

    
    
    /**
     *=====================
     * Branch Coverage for recommendTag() 
     * Ensures each keyword in the Tag generator maps to the correct Tag.
     * =====================
     */
 
    //Social Media
 
    @Test
    public void recommendTag_facebook_returnsSocialMedia() {
        assertThat(controller.recommendTag("https://www.facebook.com/login"), is("Social Media"));
    }
 
    @Test
    public void recommendTag_reddit_returnsSocialMedia() {
        assertThat(controller.recommendTag("https://reddit.com"), is("Social Media"));
    }
 
    @Test
    public void recommendTag_instagram_returnsSocialMedia() {
        assertThat(controller.recommendTag("https://instagram.com"), is("Social Media"));
    }
 
    @Test
    public void recommendTag_tiktok_returnsSocialMedia() {
        assertThat(controller.recommendTag("https://tiktok.com"), is("Social Media"));
    }
 
    
    //Shopping
 
    @Test
    public void recommendTag_amazon_returnsShopping() {
        assertThat(controller.recommendTag("https://amazon.com/cart"), is("Shopping"));
    }
 
    @Test
    public void recommendTag_ebay_returnsShopping() {
        assertThat(controller.recommendTag("https://ebay.com"), is("Shopping"));
    }
 
    @Test
    public void recommendTag_walmart_returnsShopping() {
        assertThat(controller.recommendTag("https://walmart.com"), is("Shopping"));
    }
 
    @Test
    public void recommendTag_target_returnsShopping() {
        assertThat(controller.recommendTag("https://target.com"), is("Shopping"));
    }
 
    
    //Email
 
    @Test
    public void recommendTag_gmail_returnsEmail() {
        assertThat(controller.recommendTag("https://gmail.com"), is("Email"));
    }
 
    @Test
    public void recommendTag_outlook_returnsEmail() {
        assertThat(controller.recommendTag("https://outlook.com"), is("Email"));
    }
 
    
    //Finance
 
    @Test
    public void recommendTag_bank_returnsFinance() {
        assertThat(controller.recommendTag("https://mybank.com"), is("Finance"));
    }
 
    @Test
    public void recommendTag_credit_returnsFinance() {
        assertThat(controller.recommendTag("https://creditkarma.com"), is("Finance"));
    }
 
    
    //Education
 
    @Test
    public void recommendTag_edu_returnsEducation() {
        assertThat(controller.recommendTag("https://university.edu/login"), is("Education"));
    }
 
    @Test
    public void recommendTag_k12_returnsEducation() {
        assertThat(controller.recommendTag("https://portal.k12.nm.us"), is("Education"));
    }
 
    @Test
    public void recommendTag_dotOrg_returnsEducation() {
        assertThat(controller.recommendTag("https://khanacademy.org"), is("Education"));
    }
 
    @Test
    public void recommendTag_dotNet_returnsEducation() {
        assertThat(controller.recommendTag("https://somesite.net"), is("Education"));
    }
 
    
    //Gaming
 
    @Test
    public void recommendTag_steam_returnsGaming() {
        assertThat(controller.recommendTag("https://store.steampowered.com"), is("Gaming"));
    }
 
    @Test
    public void recommendTag_game_returnsGaming() {
        assertThat(controller.recommendTag("https://game.example.com"), is("Gaming"));
    }
 
    @Test
    public void recommendTag_gog_returnsGaming() {
        assertThat(controller.recommendTag("https://gog.com"), is("Gaming"));
    }
 
    @Test
    public void recommendTag_twitch_returnsGaming() {
        assertThat(controller.recommendTag("https://twitch.tv"), is("Gaming"));
    }
 
    
    //General
 
    @Test
    public void recommendTag_unknownUrl_returnsGeneral() {
        assertThat(controller.recommendTag("https://randomsite.com"), is("General"));
    }
 
    @Test
    public void recommendTag_emptyString_returnsGeneral() {
        assertThat(controller.recommendTag(""), is("General"));
    }
 
    @Test
    public void recommendTag_justProtocol_returnsGeneral() {
        assertThat(controller.recommendTag("https://"), is("General"));
    }
 
    
    /**
     * Checking for case insensitivity. Method calls url.toLowerCase()
     * So uppercase input must still match
     */
 
    @Test
    public void recommendTag_uppercaseFacebook_returnsSocialMedia() {
        assertThat(controller.recommendTag("HTTPS://FACEBOOK.COM"), is("Social Media"));
    }
 
    @Test
    public void recommendTag_mixedCaseGmail_returnsEmail() {
        assertThat(controller.recommendTag("https://GMail.Com"), is("Email"));
    }
 
    @Test
    public void recommendTag_mixedCaseAmazon_returnsShopping() {
        assertThat(controller.recommendTag("https://Amazon.COM"), is("Shopping"));
    }
 
    @Test
    public void recommendTag_mixedCaseSteam_returnsGaming() {
        assertThat(controller.recommendTag("HTTPS://STORE.STEAMPOWERED.COM"), is("Gaming"));
    }
 
    /**
     * Checking priority for when a url could match multiple categories.
     * First matching branch should win.
     */
    
    // Ensures that "Social Media" tag is still suggested even though
    // URL ends with ".org"
    @Test
    public void recommendTag_socialMediaBeforeEducation_dotOrgOnSocialSite() {
        assertThat(controller.recommendTag("https://reddit.org"), is("Social Media"));
    }
 
    // Ensures that "Email" tag is still suggested even though URL ends
    // with ".net"
    @Test
    public void recommendTag_emailBeforeEducation_gmailDotNet() {
        assertThat(controller.recommendTag("https://gmail.net"), is("Email"));
    }


    
}
