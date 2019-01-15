package com.ninesward.nymph.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ninesward.nymph.JoyStickManager;
import com.ninesward.nymph.NymphApp;
import com.ninesward.nymph.R;
import com.ninesward.nymph.event.BroadcastEvent;
import com.ninesward.nymph.model.LocBookmark;
import com.ninesward.nymph.model.LocPoint;
import com.ninesward.nymph.util.DbUtils;
import com.ninesward.nymph.util.FakeGpsUtils;
import com.ninesward.nymph.util.LocationUtil;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import java.util.ArrayList;

public class MainActivity
        extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener {

    public static final int DELETE_ID = 1001;
    public static final int MY_PERMISSIONS_REQUEST = 1;
    public static final int REQUEST_CODE_PERMISSIONS = 2;
    private final double LAT_DEFAULT = 23.151637;
    private final double LON_DEFAULT = 113.344721;
    private EditText mLocEditText;
    private EditText mMoveStepEditText;
    private ListView mListView;
    private Button mBtnStart;
    private Button mBtnSetNew;
    private BookmarkAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BroadcastEvent.BookMark.ACTION_BOOK_MARK_UPDATE.equals(action)) {
                ArrayList<LocBookmark> allBookmark = DbUtils.getAllBookmark();
                mAdapter.setLocBookmarkList(allBookmark);
            }
        }
    };

    public static void startPage(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_main);

        //location input
        mLocEditText = findViewById(R.id.inputLoc);
        LocPoint currentLocPoint = JoyStickManager.get().getCurrentLocPoint();
        if (currentLocPoint != null) {
            mLocEditText.setText(currentLocPoint.toString());
        } else {
            String lastLocPoint = DbUtils.getLastLocPoint(this);
            if (!TextUtils.isEmpty(lastLocPoint)) {
                mLocEditText.setText(lastLocPoint);
            } else {
                mLocEditText.setText(new LocPoint(LAT_DEFAULT, LON_DEFAULT).toString());
            }
        }

        if (!LocationUtil.isLocServiceEnable(this)) {
            AlertDialog.Builder builder  = new AlertDialog.Builder(this);
            builder.setMessage("尚未开启位置定位服务");
            builder.setPositiveButton("开启", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //启动定位Activity
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_CODE_PERMISSIONS);
                }


            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }

        mLocEditText.setSelection(mLocEditText.getText().length());

        //each move step delta
        mMoveStepEditText = findViewById(R.id.inputStep);
        double currentMoveStep = JoyStickManager.get().getMoveStep();
        mMoveStepEditText.setText(String.valueOf(currentMoveStep));

        mListView = findViewById(R.id.list_bookmark);

        mBtnStart = findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(this);
        updateBtnStart();

        mBtnSetNew = findViewById(R.id.btn_set_loc);
        mBtnSetNew.setOnClickListener(this);
        updateBtnSetNew();

        initListView();

        registerBroadcastReceiver();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed(); 	不要调用父类的方法
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        double step = FakeGpsUtils.getMoveStepFromInput(this, mMoveStepEditText);
        LocPoint point = FakeGpsUtils.getLocPointFromInput(this, mLocEditText);

        ArrayList<String> arrPerm = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INSTALL_LOCATION_PROVIDER) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.INSTALL_LOCATION_PROVIDER);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.WRITE_SECURE_SETTINGS);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.WRITE_SETTINGS);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CONTROL_LOCATION_UPDATES) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.CONTROL_LOCATION_UPDATES);
        }
        if (!arrPerm.isEmpty()) {
            String[] permissions = new String[arrPerm.size()];
            permissions = arrPerm.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
        }
        switch (view.getId()) {
            case R.id.btn_start:
                if (!JoyStickManager.get().isStarted()) {
                    JoyStickManager.get().setMoveStep(step);
                    if (point != null) {
                        JoyStickManager.get().start(point);
                        finish();
                    } else {
                        Toast.makeText(this, "Input is not valid!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    LocPoint currentLocPoint = JoyStickManager.get().getCurrentLocPoint();
                    if (currentLocPoint != null) {
                        DbUtils.saveLastLocPoint(this, currentLocPoint);
                    }
                    JoyStickManager.get().stop();
                    finish();
                }
                updateBtnStart();
                updateBtnSetNew();
                break;

            case R.id.btn_set_loc:
                if (step > 0 && point != null) {
                    JoyStickManager.get().setMoveStep(step);
                    JoyStickManager.get().jumpToLocation(point);
                } else {
                    Toast.makeText(this, "Input is not valid!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void updateBtnStart() {
        if (JoyStickManager.get().isStarted()) {
            mBtnStart.setText(R.string.btn_stop);
        } else {
            mBtnStart.setText(R.string.btn_start);
        }
    }

    private void updateBtnSetNew() {
        if (JoyStickManager.get().isStarted()) {
            mBtnSetNew.setEnabled(true);
        } else {
            mBtnSetNew.setEnabled(false);
        }
    }

    private void initListView() {
        mAdapter = new BookmarkAdapter(this);
        ArrayList<LocBookmark> allBookmark = DbUtils.getAllBookmark();
        mAdapter.setLocBookmarkList(allBookmark);
        mListView.setAdapter(mAdapter);

        View emptyView = findViewById(R.id.empty_view);
        mListView.setEmptyView(emptyView);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocPoint locPoint = mAdapter.getItem(position).getLocPoint();
                mLocEditText.setText(locPoint != null ? locPoint.toString() : "");
            }
        });

        registerForContextMenu(mListView);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.menu_delete);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                delete(info.position);
                return true;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void delete(final int position) {
        if (position < 0) return;
        final LocBookmark bookmark = mAdapter.getItem(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete " + bookmark.toString())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DbUtils.deleteBookmark(bookmark);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(BroadcastEvent.BookMark.ACTION_BOOK_MARK_UPDATE);
        LocalBroadcastManager.getInstance(NymphApp.get()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(NymphApp.get()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        unregisterBroadcastReceiver();
        super.onDestroy();
    }
}
