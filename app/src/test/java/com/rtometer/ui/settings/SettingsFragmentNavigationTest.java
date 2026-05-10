package com.rtometer.ui.settings;

import android.content.Context;
import android.view.Menu;
import android.widget.PopupMenu;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import com.rtometer.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SettingsFragmentNavigationTest {

    @Test
    public void bottomNavMenu_hasFourItems_inCorrectOrder() {
        Context ctx = ApplicationProvider.getApplicationContext();
        PopupMenu popup = new PopupMenu(ctx, new View(ctx));
        popup.inflate(R.menu.bottom_nav_menu);
        Menu menu = popup.getMenu();

        assertEquals(4, menu.size());
        assertEquals(R.id.nav_dashboard, menu.getItem(0).getItemId());
        assertEquals(R.id.nav_calendar,  menu.getItem(1).getItemId());
        assertEquals(R.id.nav_history,   menu.getItem(2).getItemId());
        assertEquals(R.id.nav_settings,  menu.getItem(3).getItemId());
    }

    @Test
    public void bottomNavMenu_settingsItemHasCorrectTitle() {
        Context ctx = ApplicationProvider.getApplicationContext();
        PopupMenu popup = new PopupMenu(ctx, new View(ctx));
        popup.inflate(R.menu.bottom_nav_menu);
        Menu menu = popup.getMenu();

        assertNotNull(menu.findItem(R.id.nav_settings));
        assertEquals(ctx.getString(R.string.nav_settings),
                menu.findItem(R.id.nav_settings).getTitle().toString());
    }
}
