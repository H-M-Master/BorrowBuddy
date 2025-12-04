package com.example.borrowbuddy.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.example.borrowbuddy.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.borrowbuddy.util.GsonJavaTimeAdapters;

public class SettingsFragment extends Fragment {
	// private ActivityResultLauncher<Intent> importCsvLauncher; // 已移除CSV导入功能
	private ActivityResultLauncher<Intent> exportBackupLauncher;
	private ActivityResultLauncher<Intent> importBackupLauncher;
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_settings, container, false);
		Spinner spinner = v.findViewById(R.id.spinner_theme);
		String[] items = new String[]{getString(R.string.theme_system), getString(R.string.theme_light), getString(R.string.theme_dark)};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		// 设置当前模式
		int mode = AppCompatDelegate.getDefaultNightMode();
		spinner.setSelection(mode == AppCompatDelegate.MODE_NIGHT_YES ? 2 : (mode == AppCompatDelegate.MODE_NIGHT_NO ? 1 : 0));
		spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
				int selMode = position == 2 ? AppCompatDelegate.MODE_NIGHT_YES : (position == 1 ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
				if (AppCompatDelegate.getDefaultNightMode() != selMode) {
					AppCompatDelegate.setDefaultNightMode(selMode);
				}
			}
			@Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
		});

		// ...已移除CSV导入相关代码...

		// 本地备份导出
		Button btnExportBackup = v.findViewById(R.id.btn_export_backup);
		btnExportBackup.setOnClickListener(view -> {
			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("application/json");
			intent.putExtra(Intent.EXTRA_TITLE, "borrowbuddy-backup.json");
			exportBackupLauncher.launch(intent);
		});

		// 本地备份恢复
		Button btnImportBackup = v.findViewById(R.id.btn_import_backup);
		btnImportBackup.setOnClickListener(view -> {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("application/json");
			importBackupLauncher.launch(intent);
		});

		// ...已移除CSV导入相关注册...
		exportBackupLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				result -> {
					if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
						Uri uri = result.getData().getData();
						handleExportBackup(uri);
					}
				});
		importBackupLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				result -> {
					if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
						Uri uri = result.getData().getData();
						handleImportBackup(uri);
					}
				});

		return v;
	}

	// ...已移除CSV导入相关方法...

	// 处理备份导出
	private void handleExportBackup(Uri uri) {
		if (uri == null || getContext() == null) {
			Toast.makeText(getContext(), "文件无效", Toast.LENGTH_SHORT).show();
			return;
		}
		new Thread(() -> {
			try {
				com.example.borrowbuddy.data.local.db.AppDatabase db = com.example.borrowbuddy.data.local.db.AppDatabase.get(requireContext());
				java.util.List<com.example.borrowbuddy.data.model.Loan> loans = db.loanDao().getAllSync();
				Gson gson = new GsonBuilder()
						.registerTypeAdapter(java.time.Instant.class, new GsonJavaTimeAdapters.InstantTypeAdapter())
						.registerTypeAdapter(java.time.LocalDate.class, new GsonJavaTimeAdapters.LocalDateTypeAdapter())
						.create();
				String json = gson.toJson(loans);
				try (java.io.OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
					os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
				}
				requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "备份导出成功", Toast.LENGTH_SHORT).show());
			} catch (Exception e) {
				requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "备份导出失败: " + e.getMessage(), Toast.LENGTH_LONG).show());
			}
		}).start();
	}

	// 处理备份恢复
	private void handleImportBackup(Uri uri) {
		if (uri == null || getContext() == null) {
			Toast.makeText(getContext(), "文件无效", Toast.LENGTH_SHORT).show();
			return;
		}
		new Thread(() -> {
			try (java.io.InputStream is = requireContext().getContentResolver().openInputStream(uri);
				 java.io.InputStreamReader isr = new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8)) {
				Gson gson = new GsonBuilder()
						.registerTypeAdapter(java.time.Instant.class, new GsonJavaTimeAdapters.InstantTypeAdapter())
						.registerTypeAdapter(java.time.LocalDate.class, new GsonJavaTimeAdapters.LocalDateTypeAdapter())
						.create();
				com.example.borrowbuddy.data.model.Loan[] loans = gson.fromJson(isr, com.example.borrowbuddy.data.model.Loan[].class);
				if (loans != null && loans.length > 0) {
					com.example.borrowbuddy.data.local.db.AppDatabase db = com.example.borrowbuddy.data.local.db.AppDatabase.get(requireContext());
					db.loanDao().deleteAll();
					for (com.example.borrowbuddy.data.model.Loan l : loans) {
						db.loanDao().insert(l);
					}
				}
				requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "备份恢复成功：" + (loans == null ? 0 : loans.length) + "条", Toast.LENGTH_SHORT).show());
			} catch (Exception e) {
				requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "备份恢复失败: " + e.getMessage(), Toast.LENGTH_LONG).show());
			}
		}).start();
	}
}
