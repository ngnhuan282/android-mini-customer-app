package com.example.btqt02;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class PointListActivity extends AppCompatActivity {
    ArrayList<Customer> customerList;
    CustomerAdapter adapter;
    ListView lv;
    MyDatabaseHelper dbHelper;
    Button btnInput, btnUse, btnList,btnExport, btnImport,btnChangePass;

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
        btnExport = findViewById(R.id.btnExport);
        btnImport = findViewById(R.id.btnImport);
        btnChangePass = findViewById(R.id.btnChangePass);

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

        btnExport.setOnClickListener(v -> exportToXMLAndEmail());

        btnImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/xml");
            filePickerLauncher.launch(intent);
        });

        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PointListActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //Kiem tra nguoi dung chon file thanh cong ko , xem intent lay ve co != null k
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    //lay uri (1 .getdata -> lay dc intent //  2 .getdata -> lay dc uri
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importFromXML(uri);
                    }
                }
            });

    private void exportToXMLAndEmail() {
        try {
            loadData();
            if (customerList.isEmpty()) {
                Toast.makeText(this, "Danh sách trống, không có gì để xuất!", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder xmlBuilder = new StringBuilder();
            xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xmlBuilder.append("<customers>\n");

            for (Customer c : customerList) {
                xmlBuilder.append("\t<customer>\n");
                xmlBuilder.append("\t\t<phone>").append(c.getPhone()).append("</phone>\n");
                xmlBuilder.append("\t\t<point>").append(c.getPoint()).append("</point>\n");
                xmlBuilder.append("\t\t<note>").append(c.getNote() == null ? "" : c.getNote()).append("</note>\n");
                xmlBuilder.append("\t\t<createdAt>").append(c.getCreatedAt()).append("</createdAt>\n");
                xmlBuilder.append("\t\t<updatedAt>").append(c.getUpdatedAt()).append("</updatedAt>\n");
                xmlBuilder.append("\t</customer>\n");
            }
            xmlBuilder.append("</customers>");

            String fileName = "customers_export.xml";
            File file = new File(getExternalFilesDir(null), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(xmlBuilder.toString().getBytes());
            fos.close();

            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/xml");
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Danh sách khách hàng (XML)");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Gửi bạn danh sách khách hàng từ ứng dụng.");
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(emailIntent, "Gửi file qua..."));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importFromXML(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();// dua ve kieu <...> </...>

            NodeList nodeList = document.getElementsByTagName("customer");
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int count = 0;

            // Duyệt qua từng thẻ <customer>
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    //ep sang element de su dung cac ham cua element
                    Element element = (Element) node;

                    String phone = getTagValue("phone", element);
                    String pointStr = getTagValue("point", element);
                    String note = getTagValue("note", element);
                    String createdAt = getTagValue("createdAt", element);
                    String updatedAt = getTagValue("updatedAt", element);

                    int point = Integer.parseInt(pointStr);

                    Cursor cursor = db.rawQuery("SELECT * FROM Customers WHERE phone = '" + phone + "'", null);
                    if (cursor.moveToFirst()) {
                        String sqlUpdate = "UPDATE Customers SET " +
                                "point = " + point + ", " +
                                "note = '" + note + "', " +
                                "updatedAt = '" + updatedAt + "' " +
                                "WHERE phone = '" + phone + "'";

                        db.execSQL(sqlUpdate);
                    } else {
                        String sqlInsert = "INSERT INTO Customers(phone, point, note, createdAt, updatedAt) VALUES (" +
                                "'" + phone + "', " +
                                point + ", " +
                                "'" + note + "', " +
                                "'" + createdAt + "', " +
                                "'" + updatedAt + "')";

                        db.execSQL(sqlInsert);
                    }
                    cursor.close();
                    count++;
                }
            }
            Toast.makeText(this, "Đã nhập thành công " + count + " khách hàng!", Toast.LENGTH_SHORT).show();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi nhập file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getTagValue(String tag, Element element) {
        //luon item(0) cho getElementsByTagName,getChildNodes,... vi no luon tra ve nodelist tuc VD index 0 -> <phone>0901</phone>
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node != null ? node.getNodeValue() : "";
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