package com.example.applizen;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.applizen.model.data;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class InsertJobPostActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText job_title, job_description, job_skills, job_salary;
    private Button btn_post_job;

    private FirebaseAuth mAuth;
    private DatabaseReference mJobPost;
    private DatabaseReference mPublicDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_insert_job_post);

        // Setup toolbar
        toolbar = findViewById(R.id.insert_job_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Post Job");
        }

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        if (mUser != null) {
            String uId = mUser.getUid();
            mJobPost = FirebaseDatabase.getInstance().getReference().child("Job Post").child(uId);
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mPublicDatabase=FirebaseDatabase.getInstance().getReference().child("Public database");

        InsertJob();

        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void InsertJob() {
        job_title = findViewById(R.id.job_title);
        job_description = findViewById(R.id.job_description);
        job_skills = findViewById(R.id.job_skills);
        job_salary = findViewById(R.id.job_salary);
        btn_post_job = findViewById(R.id.btn_job_post);

        btn_post_job.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = job_title.getText().toString().trim();
                String description = job_description.getText().toString().trim();
                String skills = job_skills.getText().toString().trim();
                String salary = job_salary.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    job_title.setError("Required field");
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    job_description.setError("Required field");
                    return;
                }
                if (TextUtils.isEmpty(skills)) {
                    job_skills.setError("Required field");
                    return;
                }
                if (TextUtils.isEmpty(salary)) {
                    job_salary.setError("Required field");
                    return;
                }

                String id = mJobPost.push().getKey();
                if (id == null) {
                    Toast.makeText(getApplicationContext(), "Failed to generate job ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                String date = DateFormat.getDateInstance().format(new Date());
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                data data = new data(title, description, skills, salary, id, date, uid);


                mJobPost.child(id).setValue(data);
                mPublicDatabase.child(id).setValue(data);
                Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), PostJobActivity.class));
            }
        });

    }
}