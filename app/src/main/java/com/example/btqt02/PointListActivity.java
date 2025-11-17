package com.example.btqt02;

import android.os.Bundle;
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

        lv = findViewById(R.id.lvCustomers);

        // Tạo list dữ liệu
        customerList = new ArrayList<>();
        customerList.add(new Customer("0934123456", 20, "2025-11-17", "2025-11-18", "abcd"));
        customerList.add(new Customer("0909999999", 50, "2025-11-12", "2025-11-15", "VIP"));

        adapter = new CustomerAdapter(PointListActivity.this, customerList);
        adapter.notifyDataSetChanged();
        lv.setAdapter(adapter);
    }
}