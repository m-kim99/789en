package com.us.traystorage.app.setting;

import android.app.Dialog;
import android.content.Intent;

import com.us.traystorage.R;
import com.us.traystorage.app.auth.FindAuthActivity;
import com.us.traystorage.app.common.dialog.LoadingDialog;
import com.us.traystorage.data.DataManager;
import com.us.traystorage.data.model.ModelUser;
import com.us.traystorage.databinding.ActivityWithdrawBinding;

import base.BaseBindingActivity;

public class WithdrawActivity extends BaseBindingActivity<ActivityWithdrawBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_withdraw;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initView();
    }

    private void initView() {
        binding.setActivity(this);
    }

    public void onConfirm(){
        Intent intent = new Intent(this, FindAuthActivity.class);
        intent.putExtra("type", "exit_request");
        intent.putExtra("user_id", DataManager.get().getModel(ModelUser.class).login_id);
        startActivity(intent);
        finish();
    }
}
