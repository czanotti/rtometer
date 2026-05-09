package com.rtometer;

import com.rtometer.ui.main.MainViewModel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Verifies that the Hilt-wired ViewModel compiles and its LiveData is initialised.
 * Real injection is validated at build time by the Hilt annotation processor.
 */
public class HiltSmokeTest {

    @Test
    public void viewModel_initialMessage_isSet() {
        MainViewModel vm = new MainViewModel();
        assertNotNull(vm.getMessage());
        assertEquals("RTOmeter ready", vm.getMessage().getValue());
    }
}
