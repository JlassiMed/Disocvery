package com.example.discovery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.discovery.DB.SQLiteOperations;
import com.example.disocvery.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity implements CountryAdapter.CountryItemClicked {
    private static final String TAG = "MainActivity";

    public static final String DB_ACTION = "DB_ACTION";
    public static final String COUNTRY_ITEM_ID_EXTRA = "COUNTRY_ITEM_ID";

    public static final int REQUEST_CODE_ADD_COUNTRY = 201;
    public static final int REQUEST_CODE_UPDATE_COUNTRY = 202;

    private RecyclerView recyclerCountries;
    private FloatingActionButton btnAddCountry, btnDeleteAll;
    private CountryAdapter countryAdapter;
    private LinearLayoutManager linearLayoutManager;
    private List<Country> countryList;
    private SQLiteOperations sqliteOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerCountries = findViewById(R.id.recycler_countries);
        btnAddCountry = findViewById(R.id.btn_add_country);
        btnDeleteAll = findViewById(R.id.btn_delete_all_countries);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        countryList = new ArrayList<>();
        sqliteOperations = new SQLiteOperations(this);
        countryList = sqliteOperations.getAllCountries();
        countryAdapter = new CountryAdapter(countryList, this, this);
        recyclerCountries.setLayoutManager(linearLayoutManager);
        recyclerCountries.setAdapter(countryAdapter);
        recyclerCountries.setHasFixedSize(true);


        btnAddCountry.setOnClickListener(view -> addCountryItem());

        btnDeleteAll.setOnClickListener(view -> dialogActionMessage(this, "", "Delete all items?", "YES", "CANCEL", () -> deleteAll(), null, false));
        swipeToDelete();

    }

    private void addCountryItem() {
        Intent intent = new Intent(MainActivity.this, AddCountryActivity.class);
        intent.putExtra(DB_ACTION, CrudMethod.ADD);
        startActivityForResult(intent, REQUEST_CODE_ADD_COUNTRY);
    }

    private Void deleteAll() {
        sqliteOperations.deleteAllCountries();
        countryList.clear();
        countryAdapter.notifyDataSetChanged();
        return null;
    }


    private void updateCountryItem(int position) {
        Country country = countryList.get(position);
        Intent intent = new Intent(MainActivity.this, AddCountryActivity.class);
        intent.putExtra(DB_ACTION, CrudMethod.UPDATE);
        intent.putExtra(COUNTRY_ITEM_ID_EXTRA, country.getId());
        startActivityForResult(intent, REQUEST_CODE_UPDATE_COUNTRY);
    }

    private void swipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                sqliteOperations.deleteCountry(countryList.get(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Country at position " + viewHolder.getAdapterPosition() + " deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerCountries);
    }

    public void dialogActionMessage(Activity activity, String title, String message, String positiveActionWord, String negativeActionWord, Callable<Void> positiveAction, Callable<Void> negativeAction, boolean cancelableDialog) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveActionWord, (dialog, which) -> {
                    try {
                        positiveAction.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton(negativeActionWord, (dialog, which) -> {
                    try {
                        negativeAction.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setCancelable(cancelableDialog)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        countryList.clear();
        countryList.addAll(sqliteOperations.getAllCountries());
        countryAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == Activity.RESULT_OK) {
            Country country = (Country) data.getSerializableExtra(AddCountryActivity.COUNTRY_INTENT_EXTRA);
            if (country != null) {
                if (requestCode == REQUEST_CODE_ADD_COUNTRY) {
                    sqliteOperations.addCountry(country);
                    countryList.add(country);
                    countryAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_UPDATE_COUNTRY) {
                    sqliteOperations.updateCountry(country);
                    if (null != sqliteOperations.getAllCountries()) {
                        countryList.clear();
                        countryList.addAll(sqliteOperations.getAllCountries());
                        countryAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCountryItemClicked(int position) {
        updateCountryItem(position);
    }
}