package com.rtometer.data.db;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.sqlcipher.database.SupportFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EncryptedDatabaseTest {

    private static final byte[] SQLITE_MAGIC = {
        0x53, 0x51, 0x4c, 0x69, 0x74, 0x65, 0x20, 0x66,
        0x6f, 0x72, 0x6d, 0x61, 0x74, 0x20, 0x33, 0x00
    };

    private static final String DB_NAME = "rtometer_enc_test.db";

    private AppDatabase db;
    private File dbFile;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(DB_NAME);
        byte[] passphrase = new KeystoreKeyProvider(context).getOrCreatePassphrase();
        SupportFactory factory = new SupportFactory(passphrase);
        db = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                .openHelperFactory(factory)
                .allowMainThreadQueries()
                .build();
        db.quarterDao().getAll();
        dbFile = context.getDatabasePath(DB_NAME);
    }

    @After
    public void tearDown() {
        db.close();
        ApplicationProvider.getApplicationContext().deleteDatabase(DB_NAME);
    }

    @Test
    public void databaseFile_doesNotStartWithSqliteMagicBytes() throws Exception {
        assertTrue("DB file must exist on disk", dbFile.exists());
        byte[] header = new byte[16];
        try (FileInputStream fis = new FileInputStream(dbFile)) {
            assertTrue("Could not read 16 bytes from DB file", fis.read(header) == 16);
        }
        assertFalse("DB file must not start with SQLite magic bytes — encryption is absent",
                startsWith(header, SQLITE_MAGIC));
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }
}
