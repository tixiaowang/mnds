package com.uu.txw.auto.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.uu.txw.auto.AppInstance;

import java.util.ArrayList;
import java.util.List;

public class HelperMainActivity extends AppCompatActivity {
    private ViewPager mVp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_main);
        AppInstance.getInstance().regist(this, null);

        mVp = (ViewPager) findViewById(R.id.vp);
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new ScriptListFragment());

        mVp.setAdapter(new HelperAdapter(getSupportFragmentManager(), this, fragmentList));
    }

    private static class HelperAdapter extends FragmentPagerAdapter {
        private List<Fragment> list;

        public HelperAdapter(FragmentManager fm, Context context, List<Fragment> list) {
            super(fm);
            this.list = new ArrayList<>();
            this.list.addAll(list);
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }

}
