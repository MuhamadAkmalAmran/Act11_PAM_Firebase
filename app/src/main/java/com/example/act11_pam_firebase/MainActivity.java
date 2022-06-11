package com.example.act11_pam_firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.act11_pam_firebase.Adapter.UserAdapter;
import com.example.act11_pam_firebase.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * Mendefinisikan variable yang akan dipakai
     */
    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;
    /*
     *inisialisasi object firebase firestore
     * untuk menghubungkan dengan firestore
     */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<User> list = new ArrayList<>();
    private UserAdapter userAdapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recylerview);
        btnAdd = findViewById(R.id.btnadd);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Mengambil Data...");
        userAdapter = new UserAdapter(getApplicationContext(), list);
        userAdapter.setDialog(new UserAdapter.Dialog() {
            @Override
            public void onClick(int pos) {
                final CharSequence[] dialogItem = {"Edit , Hapus"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setItems(dialogItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i)
                        {
                            /*
                            *Melemparkan data ke class berikutnya
                             */
                            case 0:
                                Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
                                intent.putExtras("id", list.get(pos).getId());
                                intent.putExtras("name", list.get(pos).getName());
                                intent.putExtras("email", list.get(pos).getEmail());
                                startActivity(intent);
                                break;
                            case 1:
                                /*
                                *memanggil class delete data
                                 */
                                deleteData(list.get(pos).getId());
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        RecyclerView.ItemDecoration decoration = new DividerItemDecoration(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(userAdapter);

        btnAdd.setOnClickListener(v ->{
            startActivity(new Intent(getApplicationContext(), EditorActivity.class));
        });
    }
    /*
    *method untuk menampilkan data agar ditampilkan
    * pada saat aplikasi pertama kali dirunning
     */

    @Override
    protected void onStart() {
        super.onStart();
        getData();
    }

    /*
    *Method untuk mengambil data cari firebase firestore
     */
    private void getData()
    {
        progressDialog.show();
        /*
        *Mengambil data dari firestore
         */
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list.clear();
                        if (task.isSuccessful());
                        {
                            /*
                            *code ini mengambil data dari collector
                             */
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                /*
                                *Data apa saja yang ingin diambil dari collection
                                 */
                                User user = new User(document.getString("name"),document.getString("email"));
                                user.setId(document.getId());
                                list.add(user);
                            }
                            userAdapter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(getApplicationContext(), " Data gagal diambil", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
        /*
        *method untuk menghapus data
         */
        private void deleteData(String id)
        {
            progressDialog.show();
            db.collection("users").document(id)
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful())
                            {
                                Toast.makeText(getApplicationContext(), "Data gagal di hapus", Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                            getData();
                        }
                    })
        }
    }
}