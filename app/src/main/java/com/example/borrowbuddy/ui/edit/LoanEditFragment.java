package com.example.borrowbuddy.ui.edit;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.borrowbuddy.R;
import com.example.borrowbuddy.data.model.Enums;
import com.example.borrowbuddy.data.model.Loan;
import com.example.borrowbuddy.domain.ShareTextBuilder;
import java.time.LocalDate;
import java.util.Calendar;

public class LoanEditFragment extends Fragment {

    private static final String ARG_EDIT_ID = "edit_id";
    public static LoanEditFragment editing(long id){
        Bundle b = new Bundle(); b.putLong(ARG_EDIT_ID, id);
        LoanEditFragment f = new LoanEditFragment(); f.setArguments(b); return f;
    }

    private LoanEditViewModel vm;
    private EditText etTitle, etPerson, etPhone, etAmount, etNotes;
    private TextView tvDate, tvType;
    private ImageView ivPhoto;
    private Uri pendingPhotoUri;

    private final ActivityResultLauncher<Void> pickContact =
            registerForActivityResult(new ActivityResultContracts.PickContact(), uri -> {
                if (uri != null) {
                    queryContactData(uri);
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImage =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    Loan l = vm.loan.getValue(); if (l!=null) { l.photoUri = uri.toString(); vm.loan.setValue(l); }
                }
            });

    private final ActivityResultLauncher<String> reqCamera =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openCamera();
                else showPermissionDeniedDialog();
            });

    private void showPermissionDeniedDialog() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.permission_camera_rationale)
                .setMessage(R.string.permission_settings_guide)
                .setPositiveButton(R.string.go_to_settings, (d, w) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private final ActivityResultLauncher<Intent> takePicture =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode() == android.app.Activity.RESULT_OK && pendingPhotoUri != null) {
                    Loan l = vm.loan.getValue(); if (l!=null) { l.photoUri = pendingPhotoUri.toString(); vm.loan.setValue(l); }
                }
            });

    private void applyFormToLoan() {
        Loan l = vm.loan.getValue();
        if (l == null) return;

        l.title = etTitle.getText().toString().trim();
        l.personName = etPerson.getText().toString().trim();
        l.phone = etPhone.getText().toString().trim();
        l.notes = etNotes.getText().toString().trim();

        String amt = etAmount.getText().toString().trim();
        if (!amt.isEmpty()) {
            try {
                l.amountCents = Math.round(Double.parseDouble(amt) * 100);
            } catch (Exception e) {
                l.amountCents = null;
            }
            l.currency = "CNY";
        } else {
            l.amountCents = null;
            l.currency = null;
        }

        vm.loan.setValue(l);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loan_edit, container, false);
    }

    @Override public void onViewCreated(@NonNull View v,@Nullable Bundle s) {
        vm = new ViewModelProvider(this).get(LoanEditViewModel.class);

        etTitle=v.findViewById(R.id.et_title);
        etPerson=v.findViewById(R.id.et_person);
        etPhone=v.findViewById(R.id.et_phone);
        etAmount=v.findViewById(R.id.et_amount);
        etNotes=v.findViewById(R.id.et_notes);
        tvDate=v.findViewById(R.id.tv_date);
        tvType=v.findViewById(R.id.tv_type);
        ivPhoto=v.findViewById(R.id.iv_photo);
        Button btnPick=v.findViewById(R.id.btn_pick);
        Button btnCamera=v.findViewById(R.id.btn_camera);
        Button btnSave=v.findViewById(R.id.btn_save);
        Button btnSendReminder = v.findViewById(R.id.btn_send_reminder);
        ImageButton btnPickContact = v.findViewById(R.id.btn_pick_contact);

        long editId = getArguments()!=null? getArguments().getLong(ARG_EDIT_ID,0):0;
        if (editId > 0) {
            vm.load(editId);
        } else {
            vm.newLoan(Enums.LoanType.LOANED);
            btnSendReminder.setVisibility(View.GONE); // Hide for new loans
        }

        vm.loan.observe(getViewLifecycleOwner(), l -> {
            if (l==null) return;

            boolean isLoaned = l.type==Enums.LoanType.LOANED;
            tvType.setText(isLoaned?getString(R.string.loan_type_loaned):getString(R.string.loan_type_borrowed));

            int bgColor = ContextCompat.getColor(requireContext(),
                isLoaned ? R.color.color_due_chip_soon_bg : R.color.color_due_chip_normal_bg);
            int textColor = ContextCompat.getColor(requireContext(),
                isLoaned ? R.color.color_primary_dark : R.color.color_due_default);
            tvType.setBackgroundColor(bgColor);
            tvType.setTextColor(textColor);

            etTitle.setText(l.title);
            etPerson.setText(l.personName);
            etPhone.setText(l.phone);
            etNotes.setText(l.notes);
            if (l.dueDate != null) {
                tvDate.setText(l.dueDate.toString());
            } else {
                tvDate.setText(R.string.due_date_not_set);
            }
            etAmount.setText(l.amountCents==null?"":String.valueOf(l.amountCents/100.0));
            if (!TextUtils.isEmpty(l.photoUri)) Glide.with(ivPhoto).load(Uri.parse(l.photoUri)).into(ivPhoto);
            else ivPhoto.setImageResource(android.R.color.transparent);
        });

        btnPickContact.setOnClickListener(v1 -> {
            applyFormToLoan();
            pickContact.launch(null);
        });

        tvType.setOnClickListener(v1 -> {
            Loan current = vm.loan.getValue();
            if (current == null) return;

            // 创建一个新的 Loan 实例，避免同一个引用导致界面刷新不明显
            Loan updated = new Loan();
            updated.id = current.id;
            updated.type = current.type==Enums.LoanType.LOANED
                    ? Enums.LoanType.BORROWED
                    : Enums.LoanType.LOANED;
            updated.title = current.title;
            updated.personName = current.personName;
            updated.phone = current.phone;
            updated.amountCents = current.amountCents;
            updated.dueDate = current.dueDate;
            updated.notes = current.notes;
            updated.photoUri = current.photoUri;
            updated.status = current.status;
            updated.createdAt = current.createdAt;
            updated.updatedAt = current.updatedAt;
            updated.remindDaysBefore = current.remindDaysBefore;
            updated.nextNotifyAt = current.nextNotifyAt;

            vm.loan.setValue(updated);
        });

        tvDate.setOnClickListener(v12 -> {
            applyFormToLoan();
            Loan l = vm.loan.getValue(); if (l==null) return;
            LocalDate d = l.dueDate != null ? l.dueDate : LocalDate.now();
            Calendar c = Calendar.getInstance();
            c.set(d.getYear(), d.getMonthValue()-1, d.getDayOfMonth());
            new DatePickerDialog(requireContext(), (view, y, m, day) -> {
                Loan l2 = vm.loan.getValue(); if (l2==null) return;
                l2.dueDate = LocalDate.of(y, m+1, day); vm.loan.setValue(l2);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnPick.setOnClickListener(v13 -> {
            applyFormToLoan();
            pickImage.launch(
                new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()
            );
        });

        btnCamera.setOnClickListener(v14 -> {
            applyFormToLoan();
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) openCamera();
            else reqCamera.launch(Manifest.permission.CAMERA);
        });

        btnSendReminder.setOnClickListener(v16 -> {
            Loan l = vm.loan.getValue();
            if (l == null) return;

            String shareText = ShareTextBuilder.build(l);
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.send_reminder));
            startActivity(shareIntent);
        });

        btnSave.setOnClickListener(v15 -> {
            applyFormToLoan();
            vm.save();
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private void queryContactData(Uri contactUri) {
        try (Cursor cursor = requireContext().getContentResolver().query(contactUri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                if (nameIndex == -1) return;
                String name = cursor.getString(nameIndex);

                Loan l = vm.loan.getValue();
                if (l == null) return;
                l.personName = name;
                l.contactUri = contactUri.toString();

                int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                if (idIndex == -1) return;
                String contactId = cursor.getString(idIndex);

                int hasPhoneNumberIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                if (hasPhoneNumberIndex == -1) return;

                if (cursor.getInt(hasPhoneNumberIndex) > 0) {
                    try(Cursor pCur = requireContext().getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null)) {

                        if (pCur != null && pCur.moveToFirst()) {
                            int phoneIndex = pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            if (phoneIndex > -1) {
                                l.phone = pCur.getString(phoneIndex);
                            }
                        }
                    }
                } else {
                    l.phone = "";
                }
                vm.loan.setValue(l);
            }
        }
    }


    private void openCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "bb_"+System.currentTimeMillis()+".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BorrowBuddy");
        }
        pendingPhotoUri = requireContext().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pendingPhotoUri);
        takePicture.launch(intent);
    }
}
