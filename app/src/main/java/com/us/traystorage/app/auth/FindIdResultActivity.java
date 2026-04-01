package com.us.traystorage.app.auth;

import android.app.Dialog;
import android.content.Intent;

import com.us.traystorage.R;
import com.us.traystorage.app.common.dialog.LoadingDialog;
import com.us.traystorage.databinding.ActivityFindidResultBinding;

import base.BaseBindingActivity;

public class FindIdResultActivity extends BaseBindingActivity<ActivityFindidResultBinding> {

    @Override
    public int getLayout() {
        return R.layout.activity_findid_result;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        binding.setActivity(this);
        Intent i = getIntent();
        String loginId = i.getStringExtra("login_id");
        String type = i.getStringExtra("type");
        String showId = loginId;/*.substring(0, Math.min(loginId.length(), 4));
        for (int pad = 0; pad < loginId.length() - 4; pad++)
            showId += "*";*/
        if (type.equals("0")) {
            binding.textResult.setText(showId);
        } else {
            String temp = "";
            if (type.equals("1")) {
                temp = "Signed up with Google";
            } else if (type.equals("2")) {
                temp = "Signed up with Naver";
            } else if (type.equals("5")) {
                temp = "Signed up with Kakao";
            }
            binding.textResult.setText(temp);
        }

    }

    public void onPassword(){
        Intent intent = new Intent(this, FindAuthActivity.class);
        intent.putExtra("type", "password_reset");
        startActivity(intent);
        finish();
    }
    public void onLogin(){

        finish();
    }
}
