package com.example.borrowbuddy;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.fragment.app.Fragment;
import com.example.borrowbuddy.ui.list.LoanListFragment;
import com.example.borrowbuddy.ui.edit.LoanEditFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // 隐藏调试：连续长按标题栏 3 次触发一次性提醒 Worker
    private int debugTitleClickCount = 0;
    private long lastDebugClickTime = 0L;
    private static final int REQ_POST_NOTIFICATIONS = 1001;
    private static final int REQ_READ_CONTACTS = 1002;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 启动时主动请求必要权限（通知 + 联系人）
        requestNotificationPermissionIfNeeded();
        requestContactsPermissionIfNeeded();

        // 调试入口改为：快速点击标题栏 3 次，更容易触发
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                if (now - lastDebugClickTime > 1500) {
                    // 超过 1.5 秒重新计数
                    debugTitleClickCount = 0;
                }
                lastDebugClickTime = now;
                debugTitleClickCount++;

                if (debugTitleClickCount == 1) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.debug_toast_reminder_hint_first), Toast.LENGTH_SHORT).show();
                } else if (debugTitleClickCount < 3) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.debug_toast_reminder_hint_more, 3 - debugTitleClickCount), Toast.LENGTH_SHORT).show();
                } else {
                    debugTitleClickCount = 0;
                    triggerReminderWorkerOnce();
                }
            }
        });

        if (savedInstanceState == null) {
            // Always add the list fragment as the base screen.
            replace(new LoanListFragment(), false);

            // Check if the activity was launched from a widget item click.
            long editId = getIntent().getLongExtra("edit_id", 0);
            if (editId > 0) {
                replace(LoanEditFragment.editing(editId), true);
            } else if (getIntent() != null && getIntent().getData() != null) {
                // 支持深度链接 https://borrowbuddy/loan/{id}
                String path = getIntent().getData().getPath();
                if (path != null && path.startsWith("/loan/")) {
                    try {
                        long id = Long.parseLong(path.substring("/loan/".length()));
                        replace(LoanEditFragment.editing(id), true);
                    } catch (Exception ignore) {}
                }
            }
        }

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> replace(new LoanEditFragment(), true));
    }

    /**
     * 手动触发一次 ReminderWorker，方便真机测试通知，不影响正式的每日周期任务。
     */
    private void triggerReminderWorkerOnce() {
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(
                com.example.borrowbuddy.worker.ReminderWorker.class
        ).build();
        WorkManager.getInstance(this).enqueue(req);
        Toast.makeText(this, "已触发一次性提醒扫描，请稍等几秒查看通知", Toast.LENGTH_SHORT).show();
    }

    /**
     * 在启动时主动请求 Android 13+ 的通知权限，避免系统静默拦截通知。
     */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIFICATIONS);
            }
        }
    }

    private void requestContactsPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQ_READ_CONTACTS);
        }
    }

    public void replace(Fragment f, boolean addToBackStack){
        var tx = getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f);
        if (addToBackStack) tx.addToBackStack(null);
        tx.commit();
    }
}
