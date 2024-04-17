package com.example.motiontrackapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button_login = findViewById(R.id.button_login);
        Button button_register = findViewById(R.id.button_register);
        EditText editText_name = findViewById(R.id.editText_name);
        EditText editText_password = findViewById(R.id.editText_password);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText_name.getText().toString();
                String password = editText_password.getText().toString();

                SharedPreferences SP = getSharedPreferences(getString(R.string.passwordsTable), MODE_PRIVATE);
                if(!SP.contains(name)){
                    // no user
                    Toast.makeText(getApplicationContext(),
                            "user does not exist",Toast.LENGTH_SHORT).show();
                }else if(!password.equals(SP.getString(name, ""))){
                    // wrong password
                    Toast.makeText(getApplicationContext(),
                            "Wrong password or username",Toast.LENGTH_SHORT).show();
                }else{
                    // valid user and password
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(),
                            "login successful",Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences SP = getSharedPreferences(getString(R.string.passwordsTable), MODE_PRIVATE);

                View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.activity_register, null);
                AlertDialog.Builder alerDiaglogBuilder = new AlertDialog.Builder(LoginActivity.this);
                alerDiaglogBuilder.setView(view);

                TextView contactTitle = view.findViewById(R.id.new_title);
                EditText newName = view.findViewById(R.id.new_name);
                EditText newPassword = view.findViewById(R.id.new_password);
                alerDiaglogBuilder.setCancelable(true)
                        .setPositiveButton("Save",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i){
                                String name = newName.getText().toString();
                                String password = newPassword.getText().toString();
                                if(SP.contains(name)){
                                    Toast.makeText(getApplicationContext(),"user already exists", Toast.LENGTH_SHORT).show();
                                }else if(name.equals("")){
                                    Toast.makeText(getApplicationContext(),"please enter user", Toast.LENGTH_SHORT).show();
                                }else if(password.equals("")){
                                    Toast.makeText(getApplicationContext(),"please enter password", Toast.LENGTH_SHORT).show();
                                }else{
                                    SharedPreferences.Editor editor = SP.edit();
                                    editor.putString(name,password);
                                    editor.commit();
                                }
                                Toast.makeText(getApplicationContext(),"user:"+name+" password:"+password, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        {
                                            dialogInterface.cancel();
                                        }
                                    }
                                });
                final AlertDialog alertDialog = alerDiaglogBuilder.create();
                alertDialog.show();
            }
        });
    }
}
