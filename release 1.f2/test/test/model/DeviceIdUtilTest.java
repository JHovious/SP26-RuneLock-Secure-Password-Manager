package test.model;

import model.DeviceIdUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;

/**
 * Unit tests for DeviceIdUtil
 * @author Justin Hovious
 */
public class DeviceIdUtilTest {
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
 
    // Absolute path to a device.id file inside the temp folder
    private String tempFilePath;
 
    /**
     * Creates a temporary directory for testing instead of using the real
     * working directory. Clears the cache so every test is clean.
     * @throws Exception 
     */
    @Before
    public void setUp() throws Exception {
        tempFilePath = new File(tempFolder.getRoot(), "device.id").getAbsolutePath();
        setStaticField("FILE", tempFilePath);
        setStaticField("cached", null);
    }
 
    @After
    public void tearDown() throws Exception {
        // Restore the original filename and clear cache after every test
        setStaticField("FILE", "device.id");
        setStaticField("cached", null);
    }
 
    private void setStaticField(String fieldName, Object value) throws Exception {
        Field field = DeviceIdUtil.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
 
    /**
     * Ensures getDeviceId() is behaving properly for edge cases and 
     * real IDs.
     */
    @Test
    public void getDeviceId_returnsNonNull() {
        assertNotNull(DeviceIdUtil.getDeviceId());
    }
 
    @Test
    public void getDeviceId_returnsNonEmptyString() {
        assertFalse(DeviceIdUtil.getDeviceId().isEmpty());
    }
 
    @Test
    public void getDeviceId_returnsValidUuidFormat() {
        String id = DeviceIdUtil.getDeviceId();
        assertTrue(
            "Device ID must be a valid UUID: " + id,
            id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
        );
    }
 
    /**
     * Ensures that getDeviceId() is retrieving the same device ID instead
     * generating a new one when called multiple times.
     */
    @Test
    public void getDeviceId_calledTwice_returnsSameValue() {
        String first  = DeviceIdUtil.getDeviceId();
        String second = DeviceIdUtil.getDeviceId();
        assertThat(second, is(first));
    }
 
    @Test
    public void getDeviceId_calledManyTimes_alwaysReturnsSameValue() {
        String expected = DeviceIdUtil.getDeviceId();
        for (int i = 0; i < 10; i++) {
            assertThat(DeviceIdUtil.getDeviceId(), is(expected));
        }
    }
 
    /**
     * Ensures that created files are persistent and match when recalled.
     */
    @Test
    public void getDeviceId_createsDeviceIdFile() {
        DeviceIdUtil.getDeviceId();
        File idFile = new File(tempFilePath);
        assertTrue("device.id file should be created on first call", idFile.exists());
    }
 
    @Test
    public void getDeviceId_fileContentsMatchReturnedId() throws Exception {
        String id = DeviceIdUtil.getDeviceId();
        String stored = Files.readString(new File(tempFilePath).toPath()).trim();
        assertThat(stored, is(id));
    }
 
    /**
     * Ensures that persistent files can be recalled properly and that the 
     * correct information is retrieved from the files.
     * @throws Exception 
     */
    @Test
    public void getDeviceId_existingFile_returnsStoredId() throws Exception {
        String knownId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        Files.writeString(new File(tempFilePath).toPath(), knownId);
 
        String returned = DeviceIdUtil.getDeviceId();
        assertThat(returned, is(knownId));
    }
 
    @Test
    public void getDeviceId_existingFile_isNotOverwritten() throws Exception {
        String knownId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        Files.writeString(new File(tempFilePath).toPath(), knownId);
 
        DeviceIdUtil.getDeviceId();
 
        String stored = Files.readString(new File(tempFilePath).toPath()).trim();
        assertThat(stored, is(knownId));
    }
 
    /**
     * Simulates program restarting to ensure correct files and device info
     * is retrieved after. 
     * @throws Exception 
     */
    @Test
    public void getDeviceId_afterCacheCleared_readsFromFileAndReturnsSameId() throws Exception {
        String original = DeviceIdUtil.getDeviceId();

        setStaticField("cached", null);
 
        String reloaded = DeviceIdUtil.getDeviceId();
        assertThat(reloaded, is(original));
    }
 
    @Test
    public void getDeviceId_afterCacheCleared_doesNotGenerateNewId() throws Exception {
        String original = DeviceIdUtil.getDeviceId();
        setStaticField("cached", null);
        String reloaded = DeviceIdUtil.getDeviceId();
        assertThat(reloaded, is(original));
    }


}
