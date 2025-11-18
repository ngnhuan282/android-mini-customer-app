package com.example.btqt02;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UsePointActivity extends AppCompatActivity {
    Button btnInput, btnUse, btnList, btnSave, btnSaveAndNext;
    EditText edtCurrentPoint, edtUsedPoint, edtPhone, edtNote;
    MyDatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_use_point);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new MyDatabaseHelper(this);

        edtCurrentPoint = findViewById(R.id.edtCurrentUsePoint);
        edtUsedPoint = findViewById(R.id.edtUsedPoint);
        edtNote = findViewById(R.id.edtNoteUse);
        edtPhone = findViewById(R.id.edtUsePhone);

        btnInput = findViewById(R.id.btnInputFromUse);
        btnUse = findViewById(R.id.btnUseFromUse);
        btnList = findViewById(R.id.btnListFromUse);
        btnSave = findViewById(R.id.btnUseSave);
        btnSaveAndNext = findViewById(R.id.btnUseSaveAndNext);

        btnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsePointActivity.this, InputPointActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsePointActivity.this, UsePointActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsePointActivity.this, PointListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usePoint();
            }
        });

        btnSaveAndNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usePoint();
                Intent intent = new Intent(UsePointActivity.this, PointListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        edtPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) loadCurrentPointUse();
        });
    }

    /*
    * HÀM SỬ DỤNG ĐIỂM
    * Kiểm tra khách hàng có tồn tại?
    * Kiểm tra khách hàng có đủ điểm ?
    * Nếu đủ điểm -> trừ
    * Cập nhật note, updatedAt
    * Hiển thị lại Current point mới
    * */
    private void usePoint()
    {
        String phone = edtPhone.getText().toString();
        String usePointStr = edtUsedPoint.getText().toString();
        String note = edtNote.getText().toString();
        int usePoint = Integer.parseInt(usePointStr);

        if(phone.isEmpty() || usePointStr.isEmpty())
        {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT point FROM Customers WHERE phone=?", new String[]{phone});

        if(!cursor.moveToFirst())
        {
            Toast.makeText(this, "Không tìm thấy khách hàng!", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        int currentPoint = cursor.getInt(0);
        if(usePoint > currentPoint)
        {
            Toast.makeText(this, "Điểm của bạn không đủ!", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        // Đủ điểm -> trừ
        int updatedPoint = currentPoint - usePoint;
        SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
        dbWrite.execSQL("UPDATE Customers SET point=?, note=?, updatedAt=? WHERE phone=?",
                        new Object[]{updatedPoint, note, getDateNow(), phone});

        Toast.makeText(this, "Sử dụng điểm thành công!", Toast.LENGTH_SHORT).show();
        edtCurrentPoint.setText(String.valueOf(updatedPoint));
        cursor.close();
    }

    /*
    * Hàm load tự động Current Point khi nhập số điện thoại
    * */
    private void loadCurrentPointUse()
    {
        String phone = edtPhone.getText().toString();
        if(phone.isEmpty()) return;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT point FROM Customers WHERE phone=?", new String[]{phone});

        if(cursor.moveToFirst())
            edtCurrentPoint.setText(String.valueOf(cursor.getInt(0)));
        else
            edtCurrentPoint.setText("0");

        cursor.close();
    }

    // Hàm lấy thời gian hiện tại
    private String getDateNow()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Hàm xóa các field trong form
    private void clearForm()
    {
        edtPhone.setText("");
        edtUsedPoint.setText("");
        edtNote.setText("");
        edtCurrentPoint.setText("");
    }
}