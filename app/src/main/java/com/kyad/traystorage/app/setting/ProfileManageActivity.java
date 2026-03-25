package com.kyad.traystorage.app.setting;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;

//import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.auth.LoginHomeActivity;
import com.kyad.traystorage.app.common.PhotoSelectActivity;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelUploadFile;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivityProfileManageBinding;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import base.BaseBindingActivity;
import base.BaseEvent;
import base.BaseViewModel;
import helper.Util;
import helper.Validation;

public class ProfileManageActivity extends BaseBindingActivity<ActivityProfileManageBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_profile_manage;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initViewModel();
        initView();
        setupKeyboard(binding.bg);
    }

    private void initView() {
        binding.setActivity(this);
        binding.setModel(viewModel);

        viewModel.name.observe(this, v->{
            if(v.isEmpty()){
                binding.clearBtn.setVisibility(View.GONE);
            }else
                binding.clearBtn.setVisibility(View.VISIBLE);
        });
    }

    public ObservableBoolean isEditable = new ObservableBoolean(false);
    public ViewModel viewModel;
    public ObservableBoolean isDatePickerShown = new ObservableBoolean(false);
    void initViewModel(){
        viewModel = new ViewModel();
        ModelUser oldUser = DataManager.get().getModel(ModelUser.class);
        viewModel.name.setValue(oldUser.name);
        viewModel.email.setValue(oldUser.email);
        viewModel.birthday.setValue(oldUser.birthday != null ? oldUser.birthday.replace("-", ".") : "");
        viewModel.profile_image.setValue(oldUser.profile_image);
        viewModel.gender.setValue(oldUser.gender);
        // 초기화 시 성별 버튼 상태 설정 (평상시 스타일)
        updateGenderButtons(oldUser.gender);

        Glide.with(binding.imgAvatar).load(oldUser.profile_image).placeholder(R.drawable.icon_c_user_60).into(binding.imgAvatar);

    }

    public class ViewModel extends BaseViewModel {
        public MutableLiveData<String> profile_image = new MutableLiveData<>("");
        public MutableLiveData<String> birthday = new MutableLiveData<>("");
        public MutableLiveData<String> name = new MutableLiveData<>("");
        public MutableLiveData<String> email = new MutableLiveData<>("");
        public MutableLiveData<Integer> gender = new MutableLiveData<>(0);

        public void uploadImage(String fname){
            // 테스트 모드일 경우 로컬 파일 경로 그대로 사용
            if (DataManager.get().isTestMode()) {
                profile_image.setValue(fname);
                updateProfile();
                return;
            }

            addDisposable(DataManager.get().uploadImages(new String[]{fname}).subscribeWith(new ResponseSubscriber<List<ModelUploadFile>>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        profile_image.setValue(getResponse().data.get(0).file_name);
                        updateProfile();
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
        public void updateProfile() {
            // 테스트 모드일 경우 로컬에만 저장
            if (DataManager.get().isTestMode()) {
                ModelUser oldUser = DataManager.get().getModel(ModelUser.class);
                oldUser.name = name.getValue();
                oldUser.email = email.getValue();
                oldUser.birthday = birthday.getValue().replace(".", "-");
                oldUser.gender = gender.getValue();
                oldUser.profile_image = profile_image.getValue();
                DataManager.get().setModel(oldUser);

                Glide.with(binding.imgAvatar).load(oldUser.profile_image).placeholder(R.drawable.icon_c_user_60).into(binding.imgAvatar);
                Utils.showCustomToast(ProfileManageActivity.this, R.string.profile_save_ok);
                isEditable.set(false);
                refreshGenderButtonStyle();
                return;
            }

            addDisposable(DataManager.get().updateProfile(
                    name.getValue(), birthday.getValue().replace(".","-"), gender.getValue(), email.getValue(), profile_image.getValue()).subscribeWith(new ResponseSubscriber<ModelUser>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        ModelUser oldUser = DataManager.get().getModel(ModelUser.class);
                        ModelUser user = getResponse().data;
                        user.password = oldUser.password;
                        user.isAutoLogin = oldUser.isAutoLogin;
                        DataManager.get().setModel(user);

                        viewModel.profile_image.setValue(user.profile_image);

                        Glide.with(binding.imgAvatar).load(user.profile_image).placeholder(R.drawable.icon_c_user_60).into(binding.imgAvatar);

                        Utils.showCustomToast(ProfileManageActivity.this, R.string.profile_save_ok);
                        isEditable.set(false);
                        refreshGenderButtonStyle();
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }

    /************************************************************
     *  Event Bus
     ************************************************************/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void PhotoSelectCompleteEvent(BaseEvent.PhotoSelectCompleteEvent e) {
        viewModel.profile_image.setValue(e.photoUrlList.get(0));
        Glide.with(this).load(viewModel.profile_image.getValue()).into(binding.imgAvatar);
    }

    public void onClickProfile(){
        Intent intent = new Intent(this, PhotoSelectActivity.class);
        intent.putExtra("is_single", 1);
        startActivity(intent);
    }
    public void onClickEdit(){
        isEditable.set(true);
        refreshGenderButtonStyle();
    }
    public void onDeleteName(){
        binding.editName.setText("");
    }

    public void onSex1(){
        if (!isEditable.get()) return;
        updateGenderButtons(0);
        viewModel.gender.setValue(0);
    }
    public void onSex2(){
        if (!isEditable.get()) return;
        updateGenderButtons(1);
        viewModel.gender.setValue(1);
    }
    public void onSex3(){
        if (!isEditable.get()) return;
        updateGenderButtons(2);
        viewModel.gender.setValue(2);
    }

    private void updateGenderButtons(int selected) {
        binding.buttonSex1.setSelected(selected == 0);
        binding.buttonSex2.setSelected(selected == 1);
        binding.buttonSex3.setSelected(selected == 2);

        // 선택된 버튼만 파란색 텍스트, 나머지는 회색
        binding.buttonSex1.setTextColor(getResources().getColor(selected == 0 ? R.color.focus_color : R.color.C777777));
        binding.buttonSex2.setTextColor(getResources().getColor(selected == 1 ? R.color.focus_color : R.color.C777777));
        binding.buttonSex3.setTextColor(getResources().getColor(selected == 2 ? R.color.focus_color : R.color.C777777));
    }

    // 편집 모드 진입 시 버튼 스타일 업데이트
    public void refreshGenderButtonStyle() {
        updateGenderButtons(viewModel.gender.getValue());
    }

    public void onClickBirthDay() {
        if (!isEditable.get()) {
            return;
        }
        if(true){
            com.kyad.traystorage.app.common.dialog.DatePickerDialog.show(this).setDate(viewModel.birthday.getValue())
                    .setListener(new com.kyad.traystorage.app.common.dialog.DatePickerDialog.ActionListener() {
                        @Override
                        public void onSelect(String date) {
                            binding.editBirthday.setText(date);
                            isDatePickerShown.set(false);
                        }
                    });
        }else {
            final Calendar calendar = Calendar.getInstance();
            try {
                if (!viewModel.birthday.getValue().isEmpty()) {
                    calendar.setTime(Util.getDate(viewModel.birthday.getValue()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            DatePickerDialog dlg = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(year, month, dayOfMonth);
                    String currentDate = (String) DateFormat.format("yyyy.MM.dd", calendar.getTime());
                    binding.editBirthday.setText(currentDate);
                    isDatePickerShown.set(false);
                }

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    isDatePickerShown.set(false);
                }
            });
            dlg.show();
            isDatePickerShown.set(true);
        }
    }


    public void onConfirm(){
        if(viewModel.name.getValue().isEmpty()){
            Utils.showCustomToast(this, R.string.input_name);
            return;
        }
        if(viewModel.email.getValue().isEmpty()){
            Utils.showCustomToast(this, R.string.input_email);
            return;
        }
        if (viewModel.name.getValue().length() < 2) {
            Utils.showCustomToast(this, R.string.input_name2, Toast.LENGTH_SHORT);
            return;
        }
        if (!Validation.isEmail(viewModel.email.getValue())) {
            Utils.showCustomToast(this, R.string.incorrect_email, Toast.LENGTH_SHORT);
            return;
        }

        AlertDialog.show(this).setText(getString(R.string.profile_save_confirm), "", getString(R.string.confirm), getString(R.string.cancel))
                .setListener(new AlertDialog.ActionListener() {
                    @Override
                    public void onConfirm() {
                        if(!viewModel.profile_image.getValue().equals("") && !viewModel.profile_image.getValue().startsWith("http")){
                            viewModel.uploadImage(viewModel.profile_image.getValue());
                        }else
                            viewModel.updateProfile();
                    }
                });

    }


    @Override
    public void onBackPressed(){
        boolean isChanged = false;
        ModelUser user = DataManager.get().getModel(ModelUser.class);
        if(user.name.equals(viewModel.name.getValue())==false)
            isChanged = true;
        else if(user.email.equals(viewModel.email.getValue())==false)
            isChanged = true;
        else if(user.birthday.equals(viewModel.birthday.getValue().replace(".","-"))==false)
            isChanged = true;
        else if(user.profile_image.equals(viewModel.profile_image.getValue())==false)
            isChanged = true;
        else if(user.gender.equals(viewModel.gender.getValue())==false)
            isChanged = true;

        if(isChanged){
            AlertDialog.show(this).setText(getString(R.string.profile_save_qa), "", getString(R.string.confirm), getString(R.string.cancel))
                    .setListener(()->{
                        finish();
                    });
        }else
            finish();
    }
}
