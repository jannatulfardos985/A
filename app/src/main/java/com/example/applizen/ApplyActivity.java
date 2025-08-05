package com.example.applizen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ApplyActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtResume;
    Button btnSubmit;
    TextView txtStatus;
    String jobTitle;
    DatabaseReference applyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtResume = findViewById(R.id.edtResume);
        btnSubmit = findViewById(R.id.btnSubmit);
        txtStatus = findViewById(R.id.txtStatus); // Ensure this TextView is added to your XML

        jobTitle = getIntent().getStringExtra("jobTitle");
        applyRef = FirebaseDatabase.getInstance().getReference().child("Applications");

        btnSubmit.setOnClickListener(v -> submitApplication());
    }

    private void submitApplication() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String resume = edtResume.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(resume)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        applyRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean alreadyApplied = false;

                        for (DataSnapshot appSnap : snapshot.getChildren()) {
                            String existingJobTitle = appSnap.child("jobTitle").getValue(String.class);
                            if (jobTitle.equals(existingJobTitle)) {
                                alreadyApplied = true;
                                break;
                            }
                        }

                        if (alreadyApplied) {
                            new AlertDialog.Builder(ApplyActivity.this)
                                    .setTitle("Already Applied")
                                    .setMessage("You have already applied for this job.")
                                    .setPositiveButton("OK", null)
                                    .show();

                            btnSubmit.setEnabled(false);
                            btnSubmit.setText("Already Applied");
                            btnSubmit.setBackgroundColor(Color.GRAY);
                            txtStatus.setText("Application already submitted.");
                        } else {
                            HashMap<String, String> application = new HashMap<>();
                            application.put("name", name);
                            application.put("email", email);
                            application.put("resume", resume);
                            application.put("jobTitle", jobTitle);

                            applyRef.push().setValue(application).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ApplyActivity.this, "Application Submitted", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ApplyActivity.this, AllJobActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(ApplyActivity.this, "Failed to submit", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ApplyActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
