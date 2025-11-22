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
    Button btnInput, btnUse, btnList,btnExport, btnImport;

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
            // Mở trình quản lý file để chọn file XML
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/xml"); // Chỉ lọc file XML hoặc text
            filePickerLauncher.launch(intent);
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
            // 1. Lấy dữ liệu từ DB
            loadData(); // Đảm bảo customerList mới nhất
            if (customerList.isEmpty()) {
                Toast.makeText(this, "Danh sách trống, không có gì để xuất!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Xây dựng chuỗi XML thủ công (StringBuilder)
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

            // 3. Lưu file vào thư mục riêng của App
            String fileName = "customers_export.xml";
            //tao file nam trong thu muc rieng (null tuc thu muc goc cua app) ,null cho don gian va nhanh
            //getExternalFilesDir chi luon luu vao thu muc trong app co the la (DOWNLOADS) ,(DOCUMENTS) ,...  nhung tat ca deu thuoc app
            File file = new File(getExternalFilesDir(null), fileName);
            //chuyen ve byte ( thiet bi chi hieu du lieu bang so)
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(xmlBuilder.toString().getBytes());
            fos.close();

            // 4. Tạo Intent gửi Email (chia se file voi cac app khac)(cap quyen truyen cap den file do , tra ve uri de app khac tim den)
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            // getpackagename de co they lay id app , roi chuoi "" phai trung voi ten trong manifest ,  android:authorities
            //tao intent gui email
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            //cho biet du lieu gui di la file xml
            emailIntent.setType("text/xml");
            //them tieu de email
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Danh sách khách hàng (XML)");
            //noi dung email
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Gửi bạn danh sách khách hàng từ ứng dụng.");
            //dinh kem file vao
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            //cap quyen cho app nhan file doc noi dung nhung k sua , xoa , thay doi
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            //mo app email de gui
            startActivity(Intent.createChooser(emailIntent, "Gửi file qua..."));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importFromXML(Uri uri) {
        try {
            //getContentResolver truy cap du lieu tu uri (lay du lieu tu uri)
            InputStream inputStream = getContentResolver().openInputStream(uri);

            // Sử dụng DOM Parser để đọc XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //parse InputStream chứa XML và trả về Document DOM (doc va xu ly du lieu ve cach co the xu ly)
            Document document = builder.parse(inputStream);
            //giup doc file xml tu cac inputsteam,url , file
            document.getDocumentElement().normalize();// dua ve kieu <...> </...>

            NodeList nodeList = document.getElementsByTagName("customer");
            //lay ra cac the customer (vi document la dom chua nhieu the )
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            //mo database de chuan bi ghi du lieu ( mo dtb va cap quyen ghi)
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
                        // 2. Nếu có -> Cập nhật (Cộng chuỗi trực tiếp vào câu SQL)
                        String sqlUpdate = "UPDATE Customers SET " +
                                "point = " + point + ", " +
                                "note = '" + note + "', " +
                                "updatedAt = '" + updatedAt + "' " +
                                "WHERE phone = '" + phone + "'";

                        db.execSQL(sqlUpdate);
                    } else {
                        // 3. Nếu chưa -> Thêm mới (Cộng chuỗi trực tiếp)
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