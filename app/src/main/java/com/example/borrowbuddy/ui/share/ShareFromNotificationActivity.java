package com.example.borrowbuddy.ui.share;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.borrowbuddy.R;
import com.example.borrowbuddy.data.local.db.AppDatabase;
import com.example.borrowbuddy.data.model.Loan;
import com.example.borrowbuddy.domain.ShareTextBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShareFromNotificationActivity extends AppCompatActivity {

    public static final String EXTRA_LOAN_ID = "extra_loan_id";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long loanId = getIntent().getLongExtra(EXTRA_LOAN_ID, -1);
        if (loanId == -1) {
            finish();
            return;
        }

        executor.execute(() -> {
            AppDatabase db = AppDatabase.get(getApplicationContext());
            Loan loan = db.loanDao().getByIdSync(loanId);

            if (loan != null) {
                String shareText = ShareTextBuilder.build(loan);
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.send_reminder));
                startActivity(shareIntent);
            }
            // Finish the transparent activity after launching the share sheet
            finish();
        });
    }
}
