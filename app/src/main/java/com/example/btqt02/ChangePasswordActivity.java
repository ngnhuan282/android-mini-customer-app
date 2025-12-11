package com.example.btqt02;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChangePasswordActivity extends AppCompatActivity {
    EditText edtOldPassword, edtNewPassword, edtConfirmNewPassword;
    Button btnSave;

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnSave = findViewById(R.id.btnChangePasswordSave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    private void changePassword() {
        String oldPass = edtOldPassword.getText().toString();
        String newPass = edtNewPassword.getText().toString();
        String confirmPass = edtConfirmNewPassword.getText().toString();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu mới và Xác nhận mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedPassword = sharedPref.getString(KEY_PASSWORD, "123"); // Mật khẩu mặc định

        if (!oldPass.equals(savedPassword)) {
            Toast.makeText(this, "Mật khẩu cũ không đúng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu mật khẩu mới
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_PASSWORD, newPass);
        editor.apply();

        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình đăng nhập sau khi đổi mật khẩu
        Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}