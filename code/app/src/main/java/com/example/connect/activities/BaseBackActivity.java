package com.example.connect.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.connect.R;

public abstract class BaseBackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // subclasses call setContentView(...) first, then call setupBackUi()
        // in case they use a toolbar or a cancel button.
    }

    protected void setupBackUi(String titleIfToolbarExists) {
        // If there’s a MaterialToolbar with id "toolbar", wire its nav icon to finish()
        View tb = findViewById(R.id.toolbar);
        if (tb instanceof MaterialToolbar) {
            MaterialToolbar toolbar = (MaterialToolbar) tb;
            toolbar.setTitle(titleIfToolbarExists);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // If there’s a cancel button with id "btnCancel", wire it to finish()
        View cancel = findViewById(R.id.btnCancel);
        if (cancel != null) cancel.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {  // ensures back always closes the screen
        finish();
    }
}
