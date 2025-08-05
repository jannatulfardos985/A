package com.example.applizen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applizen.model.data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AllJobActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private DatabaseReference mAllJobPost;
    private FirebaseRecyclerAdapter<data, AllJobPostViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_job);

        toolbar = findViewById(R.id.all_job_post);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Job Post");

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAllJobPost = FirebaseDatabase.getInstance().getReference().child("Public database");
        mAllJobPost.keepSynced(true);

        recyclerView = findViewById(R.id.recycler_all_job);
        searchView = findViewById(R.id.search_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchJobsBySkill(query.toLowerCase().trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchJobsBySkill(newText.toLowerCase().trim());
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Load all jobs initially
        searchJobsBySkill("");
    }

    private void searchJobsBySkill(String skillQuery) {
        FirebaseRecyclerOptions<data> options =
                new FirebaseRecyclerOptions.Builder<data>()
                        .setQuery(mAllJobPost, data.class)
                        .build();

        // Stop old adapter listening before creating new one
        if (adapter != null) {
            adapter.stopListening();
        }

        adapter = new FirebaseRecyclerAdapter<data, AllJobPostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AllJobPostViewHolder viewHolder, int position, @NonNull data model) {
                String modelSkill = model.getSkills() != null ? model.getSkills().toLowerCase() : "";
                boolean matches = skillQuery.isEmpty() || modelSkill.contains(skillQuery);

                if (matches) {
                    viewHolder.myview.setVisibility(View.VISIBLE);
                    viewHolder.myview.setLayoutParams(new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    viewHolder.setJobTitle(model.getTitle());
                    viewHolder.setJobDate(model.getDate());
                    viewHolder.setJobDescription(model.getDescription());
                    viewHolder.setJobSkills(model.getSkills());
                    viewHolder.setJobSalary(model.getSalary());

                    // Get current user ID safely
                    String currentUid = null;
                    if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
                        currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                    }

                    if (model.getUid() != null && model.getUid().equals(currentUid)) {
                        viewHolder.btnDelete.setVisibility(View.VISIBLE);
                        viewHolder.btnDelete.setOnClickListener(v -> {
                            String key = getRef(position).getKey();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Public database");
                            ref.child(key).removeValue()
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(AllJobActivity.this, "Job deleted", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(AllJobActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        });
                    } else {
                        viewHolder.btnDelete.setVisibility(View.GONE);
                        viewHolder.btnDelete.setOnClickListener(null);
                    }

                    viewHolder.btnApply.setOnClickListener(v -> {
                        Intent applyIntent = new Intent(AllJobActivity.this, ApplyActivity.class);
                        applyIntent.putExtra("jobTitle", model.getTitle());
                        applyIntent.putExtra("jobDate", model.getDate());
                        applyIntent.putExtra("jobDescription", model.getDescription());
                        applyIntent.putExtra("jobSkills", model.getSkills());
                        applyIntent.putExtra("jobSalary", model.getSalary());
                        startActivity(applyIntent);
                    });

                    viewHolder.myview.setOnClickListener(v -> {
                        Intent intent = new Intent(AllJobActivity.this, JobDetailsActivity.class);
                        intent.putExtra("title", model.getTitle());
                        intent.putExtra("date", model.getDate());
                        intent.putExtra("description", model.getDescription());
                        intent.putExtra("skills", model.getSkills());
                        intent.putExtra("salary", model.getSalary());
                        startActivity(intent);
                    });

                } else {
                    // Hide non-matching items
                    viewHolder.myview.setVisibility(View.GONE);
                    viewHolder.myview.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    viewHolder.btnDelete.setOnClickListener(null);
                    viewHolder.btnApply.setOnClickListener(null);
                }
            }

            @NonNull
            @Override
            public AllJobPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.alljobpost, parent, false);
                return new AllJobPostViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    public static class AllJobPostViewHolder extends RecyclerView.ViewHolder {

        View myview;
        Button btnDelete, btnApply;

        public AllJobPostViewHolder(@NonNull View itemView) {
            super(itemView);
            myview = itemView;
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnApply = itemView.findViewById(R.id.btnApply);
        }

        public void setJobTitle(String title) {
            TextView mTitle = myview.findViewById(R.id.job_title);
            mTitle.setText(title);
        }

        public void setJobDate(String date) {
            TextView mDate = myview.findViewById(R.id.job_date);
            mDate.setText(date);
        }

        public void setJobDescription(String description) {
            TextView mDescription = myview.findViewById(R.id.job_description);
            mDescription.setText(description);
        }

        public void setJobSkills(String skills) {
            TextView mSkills = myview.findViewById(R.id.job_skills);
            mSkills.setText(skills);
        }

        public void setJobSalary(String salary) {
            TextView mSalary = myview.findViewById(R.id.job_salary);
            mSalary.setText(salary);
        }
    }
}
