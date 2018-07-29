package lvc.pro.com.free;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.callrecorder.free.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lvc.pro.com.free.SqliteDatabase.PhoneContactsDatabase;
import lvc.pro.com.free.Transformer.ZoomOutPageTransformer;
import lvc.pro.com.free.adapter.ScreenSlidePagerAdapter;
import lvc.pro.com.free.constants.Constants;
import lvc.pro.com.free.contacts.ContactProvider;
import lvc.pro.com.free.fragments.AllFragment;
import lvc.pro.com.free.fragments.Incomming;
import lvc.pro.com.free.fragments.Outgoing;
import lvc.pro.com.free.pojo_classes.Contacts;
import lvc.pro.com.free.service.CallDetectionService;
import lvc.pro.com.free.utility.SharedPreferenceUtility;
import lvc.pro.com.free.utils.StringUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "MainActivity";
    private ViewPager viewPager;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ScreenSlidePagerAdapter adapter;
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;
    static querySearch queylistener;
    static querySearch2 queylistener2;
    static querySearch3 queylistener3;
    static refreshstener refreshlistenerobj;
    ArrayList<String> recordinglist = new ArrayList<>();
    private InterstitialAd mInterstitialAd;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 2001;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int NUMBER_OF_CONTACTS_TO_DELETE = 300;
    SharedPreferences prefofsync;
    private AdView mAdView;
    ProgressDialog bar;
    public ActionMode mActionMode;
    private SearchManager mSearchManager;
    private SearchView mSearchView;
    private AllFragment allFragment;
    private Incomming fr;
    private Outgoing outgoing;
    private static String mTextToBeSearched = "";
    private Switch toggleRecord;
    public static MainActivity mMainActivityInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        mMainActivityInstance = this;
        //bar = new ProgressDialog(this);
        //  bar.setMessage("Fetching Contacts..");
        prefofsync = getSharedPreferences("SYNC", MODE_PRIVATE);
        MobileAds.initialize(this, "post_your_ad_here");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("post_your_ad_here");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        boolean Auth = getIntent().getBooleanExtra("AUTH", false);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        SharedPreferences SP1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean b1 = SP1.getBoolean("LOCK", false);
        if (b1 && !Auth) {
            Intent intent = new Intent(getApplicationContext(), NewPinLock.class);
            finish();
            startActivity(intent);
        }
//        initAdmin();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = findViewById(R.id.viewpager);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        toolbar.setTitle("Call Recorder");
        Bundle bundles = new Bundle();
        bundles.putStringArrayList("RECORDING", recordinglist);
        //final AllFragment allFragment = new AllFragment();
        allFragment = new AllFragment();
        allFragment.setArguments(bundles);
        fr = new Incomming();
        fr.setArguments(bundles);
        outgoing = new Outgoing();
        outgoing.setArguments(bundles);
        adapter.addFrag(allFragment, "All");
        adapter.addFrag(fr, "Received");
        adapter.addFrag(outgoing, "Outgoing");
        adapter.notifyDataSetChanged();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            //   if (prefofsync.getBoolean("RED", true)) {
            //     new AsyncAdapter1().execute(); //logic here
            //  }
        }
        //navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeColorOfStatusAndActionBar();
                setSearchQuery();
                if (mActionMode != null) {
                    mActionMode.finish();
                }

                switch (position) {

                    case 0: {
                        MainActivity.setrefreshlistener(allFragment);
                        allFragment.refresh(true);
                        break;
                    }
                    case 1: {
                        MainActivity.setrefreshlistener(fr);
                        fr.refresh(true);
                        break;
                    }
                    case 2: {
                        MainActivity.setrefreshlistener(outgoing);
                        outgoing.refresh(true);
                        break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        final SharedPreferences pref = getSharedPreferences("TOGGLE", MODE_PRIVATE);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //  toggleRecord = navigationView.getHeaderView(0).findViewById(R.id.switch1);
        View myLayout = findViewById(R.id.header_layout_nav); // root View id from that link
        toggleRecord = myLayout.findViewById(R.id.switch1);
        boolean sie = pref.getBoolean("STATE", true);
        if (sie) {
            toggleRecord.setChecked(true);
            toggleRecord.setText("Call Recording On");
        } else {
            toggleRecord.setChecked(false);
            toggleRecord.setText("Call Recording Off");
        }

        toggleRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                Toast.makeText(MainActivity.this, "status"+b, Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = pref.edit();

                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor ed = SP.edit();

                if (b) {
                    editor.putBoolean("STATE", b);
                    editor.apply();
                    toggleRecord.setText("Call Recording On");
                    ed.putString(getString(R.string.shared_pref_saving_pref_key), "0");
                    ed.apply();
                } else {
                    editor.putBoolean("STATE", b);
                    editor.apply();
                    toggleRecord.setText("Call Recording Off");
                    ed.putString(getString(R.string.shared_pref_saving_pref_key), "1");
                    ed.apply();
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        ContactProvider.deletelistener(new ContactProvider.deleterefresh() {
            @Override
            public void deleterefreshList(boolean var) {
                adapter.notifyDataSetChanged();
            }
        });

        deleteRecordingsGreaterThan300();

        Intent intent = new Intent(MainActivity.this, CallDetectionService.class);
        startService(intent);
        MainActivity.setrefreshlistener(allFragment);
    }

    private boolean isUnlimitedRecordings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        return sharedPreferences.getBoolean("UNLIMITED", true);
    }

    private ArrayList<String> sorts(ArrayList<String> recordingList) {
        Collections.sort(recordingList);
        return recordingList;
    }

    public void deleteRecordingsGreaterThan300() {
//        ArrayList<String> recordingNameList = new ArrayList<>();
        ArrayList<Contacts> recordedContacts = new ArrayList<>();
        ArrayList<String> arrangedRecordingFileNameList = new ArrayList<>();

//        recordingNameList = ContactProvider.showAllRecordedlistfiles(MainActivity.this);
//        Log.d(TAG, "deleteRecordingsGreaterThan300: unarrangRecordingFileNameList: " + recordingNameList.toString());
//
//        // arrange recording list from new to old. (3222220013__1515703409__IN__2-1223928264.3gpp, 3222220013__1515589108__IN__2-1223928264.3gpp, __1515589904__OUT__2-1223928264.3gpp)
//
////        for (String oneRecordingFileName : sorts(recordingNameList)) {
////            arrangedRecordingFileNameList.add(oneRecordingFileName);
////        }

        if (!isUnlimitedRecordings()) {


            arrangedRecordingFileNameList = ContactProvider.showAllRecordedlistfilesInNewToOldOrder(MainActivity.this);

            if (arrangedRecordingFileNameList != null && arrangedRecordingFileNameList.size() > 0) {


                Log.d(TAG, "deleteRecordingsGreaterThan300: arrangedRecordingFileNameList: " + arrangedRecordingFileNameList.toString());

                recordedContacts = ContactProvider.getCallList(MainActivity.this, arrangedRecordingFileNameList, "");

                Log.d(TAG, "deleteRecordingsGreaterThan300: recordedContacts: size(): " + recordedContacts.size());
                Log.d(TAG, "deleteRecordingsGreaterThan300: recordedContacts: " + recordedContacts.toString());


                Log.d(TAG, "deleteRecordingsGreaterThan300: Recordings Limit: " + NUMBER_OF_CONTACTS_TO_DELETE);

                if (recordedContacts.size() > NUMBER_OF_CONTACTS_TO_DELETE) {

                    Log.d(TAG, "deleteRecordingsGreaterThan300: recordedContacts.size() IS GREATER");

                    int numberOfRecordingsToDelete = recordedContacts.size() - NUMBER_OF_CONTACTS_TO_DELETE;

                    for (int i = 0; i < numberOfRecordingsToDelete; i++) {

                        Log.d(TAG, "deleteRecordingsGreaterThan300: oneRecordingName: " + arrangedRecordingFileNameList.get(i));

                        String recordedfilearray[] = arrangedRecordingFileNameList.get(i).split("__");

                        Log.d(TAG, "deleteRecordingsGreaterThan300: recordedfilearray[0]: " + recordedfilearray[0]);

                        String number = StringUtils.prepareContacts(MainActivity.this, recordedfilearray[0]);

                        if (ContactProvider.checkFav(MainActivity.this, number)) {

                            Log.d(TAG, "deleteRecordingsGreaterThan300: contact is not favourite contact");

                            File file = new File(ContactProvider.getFolderPath(MainActivity.this) + "/" + arrangedRecordingFileNameList.get(i));

                            if (file.delete()) {

                                Log.d(TAG, "deleteRecordingsGreaterThan300: DELETING RECORDING " + file.getName());

//                                Toast.makeText(MainActivity.this, "Recordings exceeded 300 deleting older ones", Toast.LENGTH_SHORT).show();

                            }

                        } else {
                            Log.d(TAG, "deleteRecordingsGreaterThan300: contact is favourite contact");
                        }
                    }
                } else {
                    Log.d(TAG, "deleteRecordingsGreaterThan300: LESS THAN " + NUMBER_OF_CONTACTS_TO_DELETE);
                }
            } else {
                Log.d(TAG, "deleteRecordingsGreaterThan300: These is no call");
            }
        } else {
            Log.d(TAG, "Recordings Limit: Unlimited");
        }

    }

    private boolean storeToDatabase(ArrayList<Contacts> phoneContacts) {
        PhoneContactsDatabase datbaseObj = new PhoneContactsDatabase(this);
        for (Contacts con : phoneContacts) {
            //photo uri got here
            if (datbaseObj.isContact(con.getNumber()).getNumber() != null) {
                datbaseObj.updateContact(con);
            } else {
                datbaseObj.addContact(con);
            }
        }
        return true;
    }

    private void changeColorOfStatusAndActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            switch (viewPager.getCurrentItem()) {
                case 0:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    break;
                case 1:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.cyan));
                    window.setStatusBarColor(getResources().getColor(R.color.cyan_dark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.cyan));
                    break;
                case 2:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.smooth_red));
                    window.setStatusBarColor(getResources().getColor(R.color.smooth_red_dark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.smooth_red));
                    break;
                default:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_resourse_file, menu);
        mSearchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(
                mSearchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mTextToBeSearched = newText;
                queylistener.Search_name(newText + "");
                queylistener2.Search_name2(newText + "");
                try {
                    queylistener3.Search_name3(newText + "");
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                if (!newText.isEmpty()) {
                    tabLayout.setVisibility(View.GONE);
                } else {
                    tabLayout.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.setting) {
            Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
            Constants.sFROM_MAIN_TO_ACTIVITY = true;
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.setting) {
            Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
            Constants.sFROM_MAIN_TO_ACTIVITY = true;
            // Handle the setting action
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_faq) {
            Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
            Constants.sFROM_MAIN_TO_ACTIVITY = true;
            Intent intent = new Intent(MainActivity.this, FAQActivity.class);
            startActivity(intent);
        } else if (id == R.id.pin_lock) {
            Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
//                    Intent intent=new Intent(MainActivity.this,PinLock.class);
//                    intent.putExtra("SET",true);
//                    startActivity(intent);
            Intent intent = new Intent(MainActivity.this, NewPinLock.class);
            intent.putExtra("SET", true);
            startActivity(intent);
        } else if (id == R.id.fav) {
            Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
            Constants.sFROM_MAIN_TO_ACTIVITY = true;
            //open favourite activity
            Intent intent = new Intent(MainActivity.this, Favourite.class);
            intent.putStringArrayListExtra("RECORD", recordinglist);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Automatic Call Recorder 2018 app download now.https://play.google.com/store/apps/details?id=com.callrecorder.freecallrecorder&hl=en";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share App");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else if (id == R.id.nav_upgrade) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.callrecorder.procallrecorder")));
            } catch (Exception e) {
                Toast.makeText(this, "Play store not found.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.rate_us) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.callrecorder.freecallrecorder")));
            } catch (Exception e) {
                Toast.makeText(this, "Play store not found.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.recording_issue) {
            Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
            Constants.sFROM_MAIN_TO_ACTIVITY = true;
            Intent intent = new Intent(MainActivity.this, Recording_issue.class);
            startActivity(intent);
        } else if (id == R.id.contact) {
            new AsyncAdapter1().execute();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static void setQueylistener(querySearch quey) {
        queylistener = quey;
    }

    public interface querySearch {
        public void Search_name(String name1);
    }

    public static void setQueylistener2(querySearch2 quey1) {
        queylistener2 = quey1;
    }

    public interface querySearch2 {
        public void Search_name2(String name1);
    }

    public static void setQueylistener3(querySearch3 quey3) {
        queylistener3 = quey3;
    }

    public interface querySearch3 {
        public void Search_name3(String name1);
    }

    public static void setrefreshlistener(refreshstener quey3) {
        refreshlistenerobj = quey3;
    }

    public interface refreshstener {
        public void refresh(boolean b);
    }

    private boolean checkAndRequestPermissions() {

        List<String> listPermissionsNeeded = new ArrayList<>();
        listPermissionsNeeded.clear();
        int recordaudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);//
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);//
        int call = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);//
        int read_phonestate = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);//
        //int Capture_audio_output = ContextCompat.checkSelfPermission(this, Manifest.permission.CAPTURE_AUDIO_OUTPUT);
        int process_outgoing_call = ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS);//
        int modify_audio_setting = ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS);//
        int read_contacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);//

        if (read_contacts != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (modify_audio_setting != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
        }
        if (process_outgoing_call != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        }
        if (read_phonestate != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (call != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (recordaudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
       /* if (Capture_audio_output != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAPTURE_AUDIO_OUTPUT);
        }*/
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Please Allow All Permission To Continue..", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            break;
            case PERMISSIONS_REQUEST_READ_CONTACTS:
                // Permission is granted
                if (prefofsync.getBoolean("RED", true)) {
                    new AsyncAdapter1().execute();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!(Favourite.mIsDestroying
                || Recording_issue.mIsDestroying
                || SettingsActivity.mIsDestroying
                || FAQActivity.mIsDestroying ||
                Main2Activity.mIsDestroying)) {
            if (SharedPreferenceUtility.getLockActivatedStatus(getApplicationContext())) {
                if ((SharedPreferenceUtility.getBackgroundStatus(getApplicationContext())) && (!(Constants.sIS_FROM_ANOTHER_ACTIVITY))) {
                    Constants.sIS_FROM_BACKGROUND = true;
                    Intent intent = new Intent(MainActivity.this, NewPinLock.class);
                    startActivity(intent);
                }
            }
        }
        Constants.sIS_FROM_ANOTHER_ACTIVITY = false;
        Constants.sFROM_MAIN_TO_ACTIVITY = false;

        if (prefofsync.getBoolean("RED", true)) {
            bar = new ProgressDialog(this);
            bar.setMessage("Fetching Contacts..");
            bar.setCancelable(false);
            new AsyncAdapter1().execute();
        } else {
            new AsyncAdapter1().execute();
        }
        // new AsyncAdapter1().execute();


/*

        if (SharedPreferenceUtility.isLocked(getApplicationContext())) {

        }
        if (!Constants.sIs_FROM_SPLASH) {
            if (!mSyncExecOnce) {
                mSyncExecOnce = true;
            } else {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSIONS_REQUEST_READ_CONTACTS);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant

                    return;
                } else {
                    new AsyncAdapter1().execute();
                }

            }
        }*/
        //placed interterstial ads here


        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }

    private class AsyncAdapter1 extends AsyncTask<Void, Integer, ArrayList<Contacts>> {

        @Override
        protected void onPostExecute(ArrayList<Contacts> contactses) {
            refreshlistenerobj.refresh(true);
            if (allFragment.isAdded()) {
                allFragment.refresh(true);
            }

            if (prefofsync.getBoolean("RED", true)) {
                bar.dismiss();
                SharedPreferences.Editor editor = prefofsync.edit();
                editor.putBoolean("RED", false);
                editor.apply();
                addAppToProtectedMode();
            }

           /* if (prefofsync.getBoolean("RED", true)) {
                SharedPreferences.Editor editor = prefofsync.edit();
                editor.putBoolean("RED", false);
                editor.apply();
            }*/
        }

        @Override
        protected ArrayList<Contacts> doInBackground(Void... voids) {
            ArrayList<Contacts> backphone = ContactProvider.getContacts(getApplicationContext());
            if (backphone != null) {
                storeToDatabase(backphone);
            }
            return backphone;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (prefofsync.getBoolean("RED", true)) {
                bar.show();
            }

        }
    }

    public boolean setSearchQuery() {
        if (!(mSearchView.getQuery().toString().trim().equalsIgnoreCase(""))) {
            if (queylistener != null) {
                queylistener.Search_name(mSearchView.getQuery() + "");
            }
            if (queylistener2 != null) {
                queylistener2.Search_name2(mSearchView.getQuery() + "");
            }
            if (queylistener3 != null) {
                queylistener3.Search_name3(mSearchView.getQuery() + "");
            }
            return true;
        }
        return false;
    }

    private void addAppToProtectedMode() {
        if ("huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER) && !/*sp.getBoolean("protected",false)*/
                SharedPreferenceUtility.getProtectedModeStatus(getApplicationContext())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.protected_mode_title).setMessage(R.string.add_to_protected_mode)
                    .setPositiveButton(R.string.add_to_protected_mode_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.huawei.systemmanager",
                                    "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                            startActivity(intent);
                            // sp.edit().putBoolean("protected",true).commit();
                            SharedPreferenceUtility.putProtectedModeStatus(getApplicationContext());
                        }
                    }).setNegativeButton(R.string.add_to_protected_mode_negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferenceUtility.putProtectedModeStatus(getApplicationContext());
                }
            }).setCancelable(false).create().show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Testing " + MainActivity.class.getSimpleName(), " on Pause");
        if (!Constants.sFROM_MAIN_TO_ACTIVITY) {
            if (Favourite.mIsDestroying) {
                Favourite.mIsDestroying = false;
                Constants.sIS_FROM_ANOTHER_ACTIVITY = false;
            }
            if (Recording_issue.mIsDestroying) {
                Recording_issue.mIsDestroying = false;
                Constants.sIS_FROM_ANOTHER_ACTIVITY = false;
            }
            if (Main2Activity.mIsDestroying) {
                Main2Activity.mIsDestroying = false;
                Constants.sIS_FROM_ANOTHER_ACTIVITY = false;
            }
            if (SettingsActivity.mIsDestroying) {
                SettingsActivity.mIsDestroying = false;
                Constants.sIS_FROM_ANOTHER_ACTIVITY = false;
            }
            if (FAQActivity.mIsDestroying) {
                FAQActivity.mIsDestroying = false;
                Constants.sIS_FROM_ANOTHER_ACTIVITY = false;
            }
        }
        SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void fetchSearchRecords() {
        if (queylistener != null) {
            queylistener.Search_name(mTextToBeSearched);
        }
        if (queylistener2 != null) {
            queylistener2.Search_name2(mTextToBeSearched);
        }
        if (queylistener3 != null) {
            queylistener3.Search_name3(mTextToBeSearched);
        }
    }

    public void setRecorderState(boolean pStatus) {
        if (toggleRecord != null) {
            toggleRecord.setChecked(pStatus);
        }
    }

}
