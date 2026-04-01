package com.us.traystorage.app.setting;

import android.app.Dialog;

import androidx.databinding.ObservableArrayList;

import com.us.traystorage.App;
import com.us.traystorage.R;
import com.us.traystorage.app.common.dialog.LoadingDialog;
import com.us.traystorage.data.DataManager;
import com.us.traystorage.data.model.ModelBase;
import com.us.traystorage.data.model.ModelNotice;
import com.us.traystorage.data.model.ModelNoticeDetail;
import com.us.traystorage.data.remote.ResponseSubscriber;
import com.us.traystorage.databinding.ActivityNoticeDetailBinding;

import base.BaseBindingActivity;
import base.BaseViewModel;

public class NoticeDetailActivity extends BaseBindingActivity<ActivityNoticeDetailBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_notice_detail;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initViewModel();
        initView();
    }

    private void initViewModel() {
        int id = getIntent().getIntExtra("id", -1);
        String code=null;
        if(getIntent().hasExtra("code")){
            code = getIntent().getStringExtra("code");
        }
        viewModel = new ViewModel();
        if(code==null)
            viewModel.getNoticeDetail(id+"", 0);
        else
            viewModel.getNoticeDetail(code, 1);
    }

    public ViewModel viewModel;
    public class ViewModel extends BaseViewModel {
        public void getNoticeDetail(String id, int is_code) {
            addDisposable(DataManager.get().getNoticeDetail(id, is_code).subscribeWith(new ResponseSubscriber<ModelNoticeDetail>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        ModelNoticeDetail dat = getResponse().data;
                        binding.setData(dat);
                        binding.webView.loadData(dat.content, "text/html", "UTF8");
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }

    }

    private void initView() {
        binding.setActivity(this);
    }

}
