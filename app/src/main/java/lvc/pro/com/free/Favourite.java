package lvc.pro.com.free;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.callrecorder.free.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;

import lvc.pro.com.free.SqliteDatabase.PhoneContactsDatabase;
import lvc.pro.com.free.SqliteDatabase.DatabaseHelper;
import lvc.pro.com.free.adapter.FavouriteAdapter;
import lvc.pro.com.free.constants.Constants;
import lvc.pro.com.free.pojo_classes.Contacts;
import lvc.pro.com.free.utility.SharedPreferenceUtility;
import lvc.pro.com.free.utils.StringUtils;

/**
 * Created by LVC on 29-Aug-17.
 */

public class Favourite extends AppCompatActivity {
    FavouriteAdapter recyclerAdapter;
    RecyclerView recyclerView;
    ArrayList<Contacts> recordedContacts = new ArrayList<>();
    ArrayList<Contacts> realContacts = new ArrayList<>();
    private AdView mAdView;
    LinearLayout message;
    public static boolean mIsDestroying = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourite_layout);
        Toolbar toolbar = findViewById(R.id.action_bar);
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        toolbar.setTitle("Favourites");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        recordedContacts.clear();
        recordedContacts = db.getAllFavouriteContacts();
        message = findViewById(R.id.hidemessage);
        for (Contacts contacts : recordedContacts) {
            boolean hascontact = false;
            PhoneContactsDatabase database = new PhoneContactsDatabase(this);
            ArrayList<Contacts> goContacts = database.AllContacts();
            for (Contacts contacts1 : goContacts) {
                if (StringUtils.prepareContacts(this, contacts.getNumber()).equals(StringUtils.prepareContacts(this, contacts1.getNumber()))) {
                    realContacts.add(contacts1);
                    hascontact = true;
                    break;
                }
            }
            if (!hascontact) {
                realContacts.add(contacts);
            }
        }
        if (realContacts.isEmpty()) {
            message.setVisibility(View.VISIBLE);
        } else {
            message.setVisibility(View.GONE);
        }
        recyclerAdapter.setContacts(realContacts);
        recyclerAdapter.notifyDataSetChanged();
    }

    private void init() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getApplicationContext())
                        .color(Color.parseColor("#dadde2"))
                        .sizeResId(R.dimen.divider)
                        .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                        .build());
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new FavouriteAdapter();
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setListener(new FavouriteAdapter.OnitemClickListener() {

            @Override
            public void onClick(View v, int position) {
                Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
                Constants.sFROM_FAV_TO_LISTEN = true;
                Intent intent = new Intent(v.getContext(), ListenActivity.class);
                intent.putExtra("NUMBER", StringUtils.prepareContacts(getApplicationContext(), realContacts.get(position).getNumber()));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(" 22222222222222" + Favourite.class.getSimpleName(), " on pause Favourite");
        mIsDestroying = true;
        SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), true);
        if (ListenActivity.mIsDestroying) {
            Constants.sIS_FROM_ANOTHER_ACTIVITY = false;
            ListenActivity.mIsDestroying = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Favourite.class.getSimpleName(), SharedPreferenceUtility.getBackgroundStatus(getApplicationContext()) + " " + Constants.sIS_FROM_ANOTHER_ACTIVITY);
        if (!ListenActivity.mIsDestroying) {

            if (SharedPreferenceUtility.getLockActivatedStatus(getApplicationContext())) {
                if ((SharedPreferenceUtility.getBackgroundStatus(getApplicationContext()))
                        && (!(Constants.sIS_FROM_ANOTHER_ACTIVITY))) {
                    Constants.sIS_FROM_BACKGROUND = true;
                    Intent intent = new Intent(Favourite.this, NewPinLock.class);
                    startActivity(intent);
                }
            }
        }
        Constants.sIS_FROM_ANOTHER_ACTIVITY = false;
        mIsDestroying = false;
        Log.d(Favourite.class.getSimpleName(), SharedPreferenceUtility.getBackgroundStatus(getApplicationContext()) + " " + Constants.sIS_FROM_ANOTHER_ACTIVITY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // mIsDestroying=false;
        Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
        SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), false);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
        SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
                SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), false);
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
