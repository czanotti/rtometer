package com.rtometer.ui.office;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rtometer.data.db.AppDatabase;
import com.rtometer.data.db.Office;
import com.rtometer.data.db.OfficeDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class OfficeSetupViewModelTest {

    @Rule
    public InstantTaskExecutorRule taskRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private OfficeDao officeDao;
    private OfficeSetupViewModel vm;

    @Before
    public void setup() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        officeDao = db.officeDao();
        vm = new OfficeSetupViewModel(officeDao);
    }

    @After
    public void tearDown() {
        vm.onCleared();
        db.close();
    }

    @Test
    public void initialLoad_empty() {
        List<Office> offices = vm.offices.getValue();
        assertNotNull(offices);
        assertTrue(offices.isEmpty());
    }

    @Test
    public void addOffice_appearsInList() {
        vm.addOfficeSync("HQ", 51.5, -0.1, 200);

        List<Office> offices = vm.offices.getValue();
        assertEquals(1, offices.size());
        assertEquals("HQ", offices.get(0).name);
        assertEquals(51.5, offices.get(0).latitude, 0.001);
        assertEquals(-0.1, offices.get(0).longitude, 0.001);
        assertEquals(200, offices.get(0).radiusMeters);
    }

    @Test
    public void addOffice_firstIsPrimary() {
        vm.addOfficeSync("HQ", 51.5, -0.1, 200);

        assertTrue(vm.offices.getValue().get(0).isPrimary);
    }

    @Test
    public void addOffice_secondIsNotPrimary() {
        vm.addOfficeSync("HQ", 51.5, -0.1, 200);
        vm.addOfficeSync("Branch", 51.6, -0.2, 150);

        List<Office> offices = vm.offices.getValue();
        assertEquals(2, offices.size());
        assertFalse(offices.get(1).isPrimary);
    }

    @Test
    public void updateOffice_reflectedInList() {
        vm.addOfficeSync("HQ", 51.5, -0.1, 200);
        Office o = vm.offices.getValue().get(0);
        o.name = "Updated HQ";
        o.radiusMeters = 350;
        vm.updateOfficeSync(o);

        Office updated = vm.offices.getValue().get(0);
        assertEquals("Updated HQ", updated.name);
        assertEquals(350, updated.radiusMeters);
    }

    @Test
    public void updateOffice_coordinatesUpdated() {
        vm.addOfficeSync("HQ", 51.5, -0.1, 200);
        Office o = vm.offices.getValue().get(0);
        o.latitude = 52.0;
        o.longitude = 0.5;
        vm.updateOfficeSync(o);

        Office updated = vm.offices.getValue().get(0);
        assertEquals(52.0, updated.latitude, 0.001);
        assertEquals(0.5, updated.longitude, 0.001);
    }

    @Test
    public void deleteOffice_withMultiple_succeeds() {
        vm.addOfficeSync("HQ", 51.5, -0.1, 200);
        vm.addOfficeSync("Branch", 51.6, -0.2, 150);
        Office toDelete = vm.offices.getValue().get(0);

        boolean result = vm.deleteOfficeSync(toDelete);

        assertTrue(result);
        assertEquals(1, vm.offices.getValue().size());
    }

    @Test
    public void deleteOffice_lastOne_blocked() {
        vm.addOfficeSync("HQ", 51.5, -0.1, 200);
        Office toDelete = vm.offices.getValue().get(0);

        boolean result = vm.deleteOfficeSync(toDelete);

        assertFalse(result);
        assertEquals(1, vm.offices.getValue().size());
    }

    @Test
    public void addMultipleOffices_allAppear() {
        vm.addOfficeSync("HQ", 51.5, -0.1, 200);
        vm.addOfficeSync("Branch A", 51.6, -0.2, 150);
        vm.addOfficeSync("Branch B", 51.7, -0.3, 100);

        assertEquals(3, vm.offices.getValue().size());
    }
}
