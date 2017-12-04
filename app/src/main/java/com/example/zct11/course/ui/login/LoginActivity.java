package com.example.zct11.course.ui.login;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.zct11.course.R;
import com.example.zct11.course.widget.statusbar.StatusBarUtil;


/**
 * Created by Administrator on 2017/10/24.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{


    private EditText inputEmail;
    private EditText inputPassword;
    private AppCompatButton btnSign;
    private ImageView back;
    private TextView tvRegister;
    private ScrollView svRoot;
    private int radius = 25;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StatusBarUtil.setTranslucent(this);
        init();


    }

    private void init() {
        inputEmail= (EditText) findViewById(R.id.input_email);
        inputPassword= (EditText) findViewById(R.id.input_password);
        btnSign= (AppCompatButton) findViewById(R.id.btn_sign);
        tvRegister= (TextView) findViewById(R.id.tv_register);
        svRoot= (ScrollView) findViewById(R.id.sv_root);
        back= (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        btnSign.setOnClickListener(this);
        //高斯模糊背景
        applyBlur();
    }


    @Override
    public void onClick(View view) {
       switch (view.getId()){
           case R.id.back:
               LoginActivity.this.finish();
               break;
           case R.id.btn_sign:
               if (!validate()) {
                   return;
               }

               btnSign.setEnabled(false);

               mProgressDialog = new ProgressDialog(this);
               mProgressDialog.setIndeterminate(true);
               mProgressDialog.setMessage("正在验证...");
               mProgressDialog.show();

               String userword = inputEmail.getText().toString();
               String password = inputPassword.getText().toString();

               //presenter.login(userword,password);
               break;
           case R.id.tv_register:

               break;
       }
    }

    /**
     * 邮箱，密码是否格式正确
     *
     * @return
     */
    public boolean validate() {
        boolean valid = true;

        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if (email.isEmpty() || !Patterns.PHONE.matcher(email).matches()) {
            inputEmail.setError("请输入有效的手机号码");
            valid = false;
        } else {
            inputEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            inputPassword.setError("密码长度在4-10位之间");
            valid = false;
        } else {
            inputPassword.setError(null);
        }

        return valid;
    }

    private void applyBlur() {
        Drawable db = getResources().getDrawable(R.drawable.bg);
        BitmapDrawable drawable = (BitmapDrawable) db;
        Bitmap bgBitmap = drawable.getBitmap();
        //处理得到模糊效果的图
        RenderScript renderScript = RenderScript.create(this);
        // Allocate memory for Renderscript to work with
        final Allocation input = Allocation.createFromBitmap(renderScript, bgBitmap);
        final Allocation output = Allocation.createTyped(renderScript, input.getType());
        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        scriptIntrinsicBlur.setInput(input);
        // Set the blur radius
        scriptIntrinsicBlur.setRadius(radius);
        // Start the ScriptIntrinisicBlur
        scriptIntrinsicBlur.forEach(output);
        // Copy the output to the blurred bitmap
        output.copyTo(bgBitmap);
        renderScript.destroy();
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bgBitmap);
        svRoot.setBackground(bitmapDrawable);
    }
}
