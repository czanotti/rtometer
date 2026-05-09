package com.rtometer.ui.office;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rtometer.R;
import com.rtometer.data.db.Office;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OfficeSetupActivity extends AppCompatActivity {

    private OfficeSetupViewModel viewModel;
    private OfficeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_office_setup);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(OfficeSetupViewModel.class);
        adapter = new OfficeAdapter();
        adapter.setListener(new OfficeAdapter.Listener() {
            @Override
            public void onEdit(Office office) {
                OfficeEditFragment.newInstance(office.id)
                        .show(getSupportFragmentManager(), "office_edit");
            }

            @Override
            public void onDelete(Office office) {
                confirmDelete(office);
            }
        });

        RecyclerView recycler = findViewById(R.id.officeRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddOffice);
        fab.setOnClickListener(v ->
                OfficeEditFragment.newInstance().show(getSupportFragmentManager(), "office_add"));

        viewModel.offices.observe(this, offices -> adapter.setOffices(offices));
    }

    private void confirmDelete(Office office) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.office_delete_title)
                .setMessage(getString(R.string.office_delete_message, office.name))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    boolean deleted = viewModel.deleteOffice(office);
                    if (!deleted) {
                        Toast.makeText(this, R.string.office_delete_last_blocked, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
