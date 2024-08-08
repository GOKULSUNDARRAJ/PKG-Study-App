package com.PKG.rs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuestionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<QuestionPaper> questionPapers = new ArrayList<>();
    private QuestionPaperAdapter adapter;
    private GoogleSignInClient mGoogleSignInClient;
    ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_qusetion);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        back=findViewById(R.id.imageView);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signOut();
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new QuestionPaperAdapter(questionPapers,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrofit initialization
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://akgsacademy.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        String userEmail = getIntent().getStringExtra("user_email");

        // Create an instance of the ApiService
        ApiService apiService = retrofit.create(ApiService.class);

        // Create an instance of the EmailRequest class with the email address
        EmailRequest emailRequest = new EmailRequest(userEmail);

        // Make the network request with email data
        Call<List<QuestionPaper>> call = apiService.getList(emailRequest);
        call.enqueue(new Callback<List<QuestionPaper>>() {
            @Override
            public void onResponse(Call<List<QuestionPaper>> call, Response<List<QuestionPaper>> response) {
                if (response.isSuccessful()) {
                    questionPapers.addAll(response.body());
                    adapter.notifyDataSetChanged();



                } else {
                    // Handle error
                    String errorMessage = "Failed to fetch data. Error: ";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        errorMessage += response.message();
                    }
                    Toast.makeText(QuestionActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<List<QuestionPaper>> call, Throwable t) {
                // Handle failure
                String errorMessage = "Failed to fetch data: ";
                if (t instanceof IOException) {
                    errorMessage += "Network error";
                } else {
                    errorMessage += t.getMessage();
                }
                Toast.makeText(QuestionActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(QuestionActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();


                            // Redirect to login screen or handle sign-out success accordingly
                            Intent intent = new Intent(QuestionActivity.this, Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(QuestionActivity.this, "Sign out failed", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}
