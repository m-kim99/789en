package com.us.traystorage.app.setting;

import android.app.Dialog;
import android.content.Intent;

import com.us.traystorage.R;
import com.us.traystorage.app.common.dialog.LoadingDialog;
import com.us.traystorage.databinding.ActivityInviteBinding;

import base.BaseBindingActivity;

public class InviteActivity extends BaseBindingActivity<ActivityInviteBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_invite;
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

        String appInstallUrl = "https://play.google.com/store/apps/details?id=com.us.traystorage";
        String content = "Need to manage documents?\nSolve it with Traystorage!\nInstall Traystorage now and manage your documents safely!";
        content = content + "\n\n" + appInstallUrl;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }
}
