package com.us.traystorage.app.splash;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.us.traystorage.R;
import com.us.traystorage.app.common.dialog.LoadingDialog;
import com.us.traystorage.app.common.util.PrefMgr;
import com.us.traystorage.databinding.ActivityIntroBinding;

import java.util.ArrayList;
import java.util.List;

import base.BaseBindingActivity;
import widgets.viewpager.BasePagerAdapter;

import static android.content.Context.MODE_PRIVATE;

public class IntroActivity extends BaseBindingActivity<ActivityIntroBinding> {

    private static final int RC_PERMISSIONS = 3001;

    @Override
    public int getLayout() {
        return R.layout.activity_intro;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    BasePagerAdapter adapter;
    
    @Override
    public void init() {
        binding.setActivity(this);

        adapter = new BasePagerAdapter(getSupportFragmentManager());
        binding.viewPager.setAdapter(adapter);
        
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new IntroFragment(R.layout.fragment_intro1));
        fragments.add(new IntroFragment(R.layout.fragment_intro2));
        fragments.add(new IntroFragment(R.layout.fragment_intro3));
        fragments.add(new IntroFragment(R.layout.fragment_intro4));
        fragments.add(new IntroFragment(R.layout.fragment_intro5));
        adapter.setFragments(fragments);
        
        // ?�동 ?��? ?�거
        // new Handler(Looper.getMainLooper()).postDelayed(this::nextPage, 2000);
    }

    public void nextPage() {
        int currentItem = binding.viewPager.getCurrentItem();
        if(currentItem < 4) { // 0~4 (5�??�이지)
            binding.viewPager.setCurrentItem(currentItem + 1);
        } else {
            // 마�?�??�이지?�서 "?�인" 버튼 ?�릭 ??
            goLoading();
        }
    }

    public void prevPage() {
        int currentItem = binding.viewPager.getCurrentItem();
        if(currentItem > 0) {
            binding.viewPager.setCurrentItem(currentItem - 1);
        }
    }

    public void goNext() {
        // 건너?�기 (?�기 버튼)
        PrefMgr prefMgr = new PrefMgr(getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.FIRST_START, false);
        prefMgr.put(PrefMgr.INTRO_SHOWN_VERSION, com.us.traystorage.BuildConfig.VERSION_CODE);
        goLoading();
    }

    public void goLoading() {
        if (LoadingActivity.getInstance() != null){
            return;
        }
        PrefMgr prefMgr = new PrefMgr(getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.FIRST_START, false);
        prefMgr.put(PrefMgr.HAS_PERMISSION, true);
        prefMgr.put(PrefMgr.INTRO_SHOWN_VERSION, com.us.traystorage.BuildConfig.VERSION_CODE);
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
        finish();
    }

    // 권한 ?�청 ?�작 (intro5 ?�인 버튼?�서 ?�출)
    public void requestAppPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+
            permissions = new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13
            permissions = new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            };
        } else {
            // Android 12 ?�하
            permissions = new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }

        // ?��? 모든 권한???�는지 체크
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            goLoading();
        } else {
            ActivityCompat.requestPermissions(this, permissions, RC_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSIONS) {
            // 권한 결과?� ?��??�이 ?�음 ?�면?�로 진행 (권한 거�??�도 ???�용 가?�하�?
            goLoading();
        }
    }
}
