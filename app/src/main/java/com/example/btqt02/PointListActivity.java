package com.example.btqt02;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class PointListActivity extends AppCompatActivity {
    ArrayList<Customer> customerList;
    CustomerAdapter adapter;
    ListView lv;
    MyDatabaseHelper dbHelper;
    Button btnInput, btnUse, btnList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_point_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new MyDatabaseHelper(this);
        lv = findViewById(R.id.lvCustomers);

        // Tạo list dữ liệu
        customerList = new ArrayList<>();
//        customerList.add(new Customer("0934123456", 20, "abcd", "2025-11-17", "2025-11-18"));
//        customerList.add(new Customer("0909999999", 50,"VIP", "2025-11-12", "2025-11-15"));

        adapter = new CustomerAdapter(PointListActivity.this, customerList);
        adapter.notifyDataSetChanged();
        lv.setAdapter(adapter);
        loadData();

        // Ánh xạ id của các button
        btnInput = findViewById(R.id.btnInputFromListActivity);
        btnUse = findViewById(R.id.btnUseFromListActivity);
        btnList = findViewById(R.id.btnListFromListActivity);

        btnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PointListActivity.this, InputPointActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PointListActivity.this, UsePointActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PointListActivity.this, PointListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadData()
    {
        customerList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT phone, point, note, createdAt, updatedAt " +
                "FROM Customers ORDER BY updatedAt DESC", null);

        while(cursor.moveToNext())
        {
            customerList.add(new Customer(
                    cursor.getString(0), // phone
                    cursor.getInt(1), // point
                    cursor.getString(2), // note
                    cursor.getString(3), // createdAt
                    cursor.getString(4) // updatedAt
            ));
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    // Tải lại dữ liệu khi chuyển từ InputPointActivity hoặc UsePointActivity
    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}