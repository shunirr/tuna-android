package jp.s5r.android.tuna;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import com.google.gson.Gson;
import jp.s5r.android.tuna.model.Log;
import jp.s5r.android.tuna.service.TunaClient;
import jp.s5r.android.tuna.util.L;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, TunaClient.OnMessageListener {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private LogsFragment mCurrentFragment;
    private TunaClient mTuna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        initTuna();
    }

    private URI getWebSocketServerUri() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("settings_server_url", null);

        if (!TextUtils.isEmpty(str)) {
            URI uri = null;
            try {
                uri = new URI(str);
            } catch (URISyntaxException e) {
                L.e(e);
            }
            return uri;
        }
        return null;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        mCurrentFragment = LogsFragment.newInstance(position + 1);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mCurrentFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = "#timeline@tm";
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private void initTuna() {
        URI uri = getWebSocketServerUri();
        if (uri != null) {
            mTuna = new TunaClient(uri);
            mTuna.addOnMessageListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_connect:
                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(this);
                String str = preferences.getString("settings_server_password", null);
                if (mTuna != null) {
                    mTuna.connect(str);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMessage(final Log log) {
        if (!"#timeline@tm".equals(log.getChannel().getName())) {
            return;
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mCurrentFragment.addLog(log);
            }
        });
    }

    public void sendMessage(String method, String channel, String message) {
        mTuna.send(new Gson().toJson(new String[] {method, channel, message}));
    }
}
