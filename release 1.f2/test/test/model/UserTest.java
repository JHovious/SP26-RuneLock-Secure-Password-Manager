package test.model;

import model.User;
import model.Account;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;

/**
 * Unit tests for User.java
 * @author Justin Hovious
 */
public class UserTest {
    
 
    private User mainUser;
    private User subUser;
 
    @Rule
    public ExpectedException thrown = ExpectedException.none();
 
    @Before
    public void setUp() {
        // Standard main user created before every test
        mainUser = new User("alice", "pass123", "uid-1", "1");
 
        // Standard sub user created before every test
        subUser = new User("bob", "pass456", "uid-2", "2", "uid-1");
    }
 
    /**
     * Constructor for mainUser. Ensures the method correctly assigns the 
     * given values. 
     */
    @Test
    public void mainUserConstructor_setsUsername() {
        assertThat(mainUser.getUsername(), is("alice"));
    }
 
    @Test
    public void mainUserConstructor_setsPassword() {
        assertThat(mainUser.getPassword(), is("pass123"));
    }
 
    @Test
    public void mainUserConstructor_setsUid() {
        assertThat(mainUser.getuid(), is("uid-1"));
    }
 
    @Test
    public void mainUserConstructor_setsMainFileNum() {
        assertThat(mainUser.getMainFileNum(), is("1"));
    }
 
    @Test
    public void mainUserConstructor_isSubIsFalse() {
        assertFalse(mainUser.isSub());
    }
 
    // Ensures that parentUid is not set for a main user
    @Test
    public void mainUserConstructor_parentUidIsNull() {
        assertNull(mainUser.getParentUid());
    }
 
    @Test
    public void mainUserConstructor_vaultVersionIsZero() {
        assertThat(mainUser.getVaultVersion(), is(0));
    }
 
    @Test
    public void mainUserConstructor_subAccountsListIsEmpty() {
        assertThat(mainUser.getSubAccounts().size(), is(0));
    }
 
    @Test
    public void mainUserConstructor_accountsListIsEmpty() {
        assertThat(mainUser.getAccounts().size(), is(0));
    }
 
    // Ensures that pastUsername correctly gets previous username before change
    @Test
    public void mainUserConstructor_pastUsernameMatchesUsername() {
        assertThat(mainUser.getPastUsername(), is("alice"));
    }
 
    @Test
    public void mainUserConstructor_pastPasswordMatchesPassword() {
        assertThat(mainUser.getPastPassword(), is("pass123"));
    }
 
    /**
     * Constructor for subUser. Ensures the method correctly assigns the 
     * given values. 
     */
    @Test
    public void subUserConstructor_isSubIsTrue() {
        assertTrue(subUser.isSub());
    }
 
    @Test
    public void subUserConstructor_setsParentUid() {
        assertThat(subUser.getParentUid(), is("uid-1"));
    }
 
    @Test
    public void subUserConstructor_setsUsername() {
        assertThat(subUser.getUsername(), is("bob"));
    }
 
    @Test
    public void subUserConstructor_setsUid() {
        assertThat(subUser.getuid(), is("uid-2"));
    }
 
    @Test
    public void subUserConstructor_vaultVersionIsZero() {
        assertThat(subUser.getVaultVersion(), is(0));
    }
 
    // Default constructor for new user.
    @Test
    public void defaultConstructor_createsObjectWithoutException() {
        User u = new User();
        assertNotNull(u);
    }
 

    /**
     * Getter and Setter methods to update user values.
     */
    @Test
    public void setUsername_updatesUsername() {
        mainUser.setUsername("charlie");
        assertThat(mainUser.getUsername(), is("charlie"));
    }
 
    @Test
    public void setPassword_updatesPassword() {
        mainUser.setPassword("newpass");
        assertThat(mainUser.getPassword(), is("newpass"));
    }
 
    @Test
    public void setIsSub_changesSubFlag() {
        mainUser.setIsSub(true);
        assertTrue(mainUser.isSub());
    }
 
    @Test
    public void setIsSub_canRevertToFalse() {
        subUser.setIsSub(false);
        assertFalse(subUser.isSub());
    }
 
    @Test
    public void setParentUid_updatesParentUid() {
        mainUser.setParentUid("uid-99");
        assertThat(mainUser.getParentUid(), is("uid-99"));
    }
 
    @Test
    public void setMainFileNum_updatesMainFileNum() {
        mainUser.setMainFileNum("42");
        assertThat(mainUser.getMainFileNum(), is("42"));
    }
 
    @Test
    public void setSubFileNum_updatesSubFileNum() {
        mainUser.setSubFileNum("7");
        assertThat(mainUser.getSubFileNum(), is("7"));
    }
 
    @Test
    public void setAccountFileNum_updatesAccountFileNum() {
        mainUser.setAccountFileNum("5");
        assertThat(mainUser.getAccountFileNum(), is("5"));
    }

 
    /**
     * Ensures past credentials are tracked properly for keystore updates
     */
    @Test
    public void setPastUsername_updatesPastUsername() {
        mainUser.setPastUsername("old_alice");
        assertThat(mainUser.getPastUsername(), is("old_alice"));
    }
 
    @Test
    public void setPastPassword_updatesPastPassword() {
        mainUser.setPastPassword("old_pass");
        assertThat(mainUser.getPastPassword(), is("old_pass"));
    }
 
    @Test
    public void pastCredentials_areIndependentOfCurrentCredentials() {
        mainUser.setPastUsername("old_alice");
        mainUser.setUsername("new_alice");
        // past and current must diverge after the credential change workflow
        assertThat(mainUser.getPastUsername(), is("old_alice"));
        assertThat(mainUser.getUsername(), is("new_alice"));
    }
 
  
    /**
     * Ensures vault version is updating appropriately
     */
    @Test
    public void setVaultVersion_updatesVersion() {
        mainUser.setVaultVersion(5);
        assertThat(mainUser.getVaultVersion(), is(5));
    }
 
    @Test
    public void incrementVaultVersion_incrementsByOne() {
        int before = mainUser.getVaultVersion();
        mainUser.incrementVaultVersion();
        assertThat(mainUser.getVaultVersion(), is(before + 1));
    }
 
    @Test
    public void incrementVaultVersion_calledMultipleTimes_accumulatesCorrectly() {
        mainUser.incrementVaultVersion();
        mainUser.incrementVaultVersion();
        mainUser.incrementVaultVersion();
        assertThat(mainUser.getVaultVersion(), is(3));
    }
 

    /**
     * Ensures authorizedDevices are behaving properly.
     */
    @Test
    public void authorizedDevices_initiallyEmpty() {
        assertThat(mainUser.getAuthorizedDevices().size(), is(0));
    }
 
    @Test
    public void authorizeDevice_addsDeviceToList() {
        mainUser.authorizeDevice("device-abc");
        assertTrue(mainUser.isDeviceAuthorized("device-abc"));
    }
 
    @Test
    public void authorizeDevice_duplicate_doesNotAddTwice() {
        mainUser.authorizeDevice("device-abc");
        mainUser.authorizeDevice("device-abc");
        assertThat(mainUser.getAuthorizedDevices().size(), is(1));
    }
 
    @Test
    public void isDeviceAuthorized_returnsFalseForUnknownDevice() {
        assertFalse(mainUser.isDeviceAuthorized("device-xyz"));
    }
 
    @Test
    public void isDeviceAuthorized_returnsTrueAfterAuthorize() {
        mainUser.authorizeDevice("device-123");
        assertTrue(mainUser.isDeviceAuthorized("device-123"));
    }
 
    @Test
    public void removeAuthorizedDevice_removesDevice() {
        mainUser.authorizeDevice("device-abc");
        mainUser.removeAuthorizedDevice("device-abc");
        assertFalse(mainUser.isDeviceAuthorized("device-abc"));
    }
 
    @Test
    public void removeAuthorizedDevice_nonExistent_doesNotThrow() {
        // Removing a device that was never added should be a no-op
        mainUser.removeAuthorizedDevice("ghost-device");
        assertThat(mainUser.getAuthorizedDevices().size(), is(0));
    }
 
    @Test
    public void authorizeMultipleDevices_allTrackedCorrectly() {
        mainUser.authorizeDevice("device-1");
        mainUser.authorizeDevice("device-2");
        mainUser.authorizeDevice("device-3");
        assertThat(mainUser.getAuthorizedDevices().size(), is(3));
        assertTrue(mainUser.isDeviceAuthorized("device-1"));
        assertTrue(mainUser.isDeviceAuthorized("device-2"));
        assertTrue(mainUser.isDeviceAuthorized("device-3"));
    }
 
    @Test
    public void removeOneDevice_othersRemainAuthorized() {
        mainUser.authorizeDevice("device-1");
        mainUser.authorizeDevice("device-2");
        mainUser.removeAuthorizedDevice("device-1");
        assertFalse(mainUser.isDeviceAuthorized("device-1"));
        assertTrue(mainUser.isDeviceAuthorized("device-2"));
    }
 
    /**
     * Ensures subUser metadata is assigning properly
     */
    @Test
    public void subUserMeta_fullConstructor_setsAllFields() {
        User.SubUserMeta meta = new User.SubUserMeta("uid-99", "dave", "Dave Smith", "encpwd==");
        assertThat(meta.uid, is("uid-99"));
        assertThat(meta.username, is("dave"));
        assertThat(meta.name, is("Dave Smith"));
        assertThat(meta.encPassword, is("encpwd=="));
    }
 
    @Test
    public void subUserMeta_defaultConstructor_doesNotThrow() {
        User.SubUserMeta meta = new User.SubUserMeta();
        assertNotNull(meta);
    }
 
    @Test
    public void getSubAccounts_returnsLiveList() {
        User.SubUserMeta meta = new User.SubUserMeta("uid-5", "eve", "Eve", "xyz");
        mainUser.getSubAccounts().add(meta);
        assertThat(mainUser.getSubAccounts().size(), is(1));
        assertThat(mainUser.getSubAccounts().get(0).username, is("eve"));
    }
 
    // Ensures getAccounts is correctly returning the list info
    @Test
    public void getAccounts_returnsLiveList_canAddAccount() {
        Account acc = new Account("http://example.com", "user", "pass",
                new ArrayList<>(), null, null, "0");
        mainUser.getAccounts().add(acc);
        assertThat(mainUser.getAccounts().size(), is(1));
    }

}
