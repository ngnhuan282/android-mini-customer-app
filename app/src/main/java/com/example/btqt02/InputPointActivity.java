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

public class InputPointActivity extends AppCompatActivity {
    Button btnInput, btnUse, btnList, btnSave, btnSaveAndNext;
    EditText edtInputPhone, edtInputCurrentPoint, edtInputNewPoint, edtNoteInput;
    MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_point);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new MyDatabaseHelper(this);
        // Ánh xạ id
        edtInputPhone = findViewById(R.id.edtInputPhone);
        edtInputCurrentPoint = findViewById(R.id.edtCurrentInputPoint);
        edtInputNewPoint = findViewById(R.id.edtInputNewPoint);
        edtNoteInput = findViewById(R.id.edtNoteInput);

        btnInput = findViewById(R.id.btnInput);
        btnUse = findViewById(R.id.btnUse);
        btnList = findViewById(R.id.btnList);
        btnSave = findViewById(R.id.btnSave);
        btnSaveAndNext = findViewById(R.id.btnSaveAndNext);

        btnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InputPointActivity.this, InputPointActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InputPointActivity.this, UsePointActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InputPointActivity.this, PointListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInputPoint();
            }
        });

        btnSaveAndNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInputPoint();
                Intent intent = new Intent(InputPointActivity.this, PointListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        edtInputPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus)
                loadCurrentPoint();
        });
    }

    /*
    * Hàm lưu điểm của khách hàng
    * Nếu KH mới -> tạo mới điểm ; Nếu KH cũ -> cập nhật điểm
    * Lưu createdAt lần đầu ; Luôn cập nhật updatedAt
    * Lưu note (nếu có)
    * */
    private void saveInputPoint()
    {
        String phone = edtInputPhone.getText().toString();
        String newPointStr = edtInputNewPoint.getText().toString();
        int newPoint = Integer.parseInt(newPointStr);
        String note = edtNoteInput.getText().toString();

        if(phone.isEmpty() || newPointStr.isEmpty())
        {
            Toast.makeText(InputPointActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT point FROM Customers WHERE phone=?", new String[]{phone});
        if(cursor.moveToFirst())
        {
            // Khách hàng cũ -> cộng điểm
            int currentPoint = cursor.getInt(0);
            int updatedPoint = currentPoint + newPoint;

            db.execSQL("UPDATE Customers SET point=?, note=?, updatedAt=? WHERE phone=?",
                        new Object[]{updatedPoint, note, getDateNow(), phone});

            Toast.makeText(this, "Cập nhật điểm thành công!", Toast.LENGTH_SHORT).show();
            edtInputCurrentPoint.setText(String.valueOf(updatedPoint));
        }
        else
        {
            // Khách hàng mới -> tạo mới
            db.execSQL("INSERT INTO Customers(phone, point, note, createdAt, updatedAt) VALUES(?, ?, ?, ?, ?)",
                    new Object[]{phone, newPoint, note, getDateNow(), getDateNow()});
            Toast.makeText(this, "Thêm khách hàng mới thành công!", Toast.LENGTH_SHORT).show();
            edtInputCurrentPoint.setText(String.valueOf(newPoint));
        }
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
        edtInputPhone.setText("");
        edtInputCurrentPoint.setText("");
        edtInputNewPoint.setText("");
        edtNoteInput.setText("");
    }

    /*
    * HÀM tự động load Current Point khi nhập số điện thoại
    * */
    private void loadCurrentPoint()
    {
        String phone = edtInputPhone.getText().toString();
        if(phone.isEmpty()) return;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT point FROM Customers WHERE phone=?", new String[]{phone});

        if(cursor.moveToFirst())
        {
            int point = cursor.getInt(0);
            edtInputCurrentPoint.setText(String.valueOf(point));
        }
        else {
            edtInputCurrentPoint.setText("0"); // Mặc định khách hàng mới
        }

        cursor.close();
    }
}