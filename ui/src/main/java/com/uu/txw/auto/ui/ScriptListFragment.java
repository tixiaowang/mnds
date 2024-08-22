package com.uu.txw.auto.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.uu.txw.auto.TaskHub;
import com.uu.txw.auto.action.ActionClickCenterInterval;
import com.uu.txw.auto.action.ActionScrollForwardInterval;
import com.uu.txw.auto.task.data.CusScriptTask;

import java.util.Arrays;


public class ScriptListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mRootView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_script_list, null);

        mRootView.findViewById(R.id.btn_start).setOnClickListener(v ->
                TaskHub.start(getActivity(), CusScriptTask.TYPE_SINGLE_THREAD,
                        "连续上滑",
                        "",
                        "",
                        "",
                        null,
                        Arrays.asList(ActionScrollForwardInterval.class)
                )
        );
        mRootView.findViewById(R.id.btn_start_click).setOnClickListener(v ->
                TaskHub.start(getActivity(), CusScriptTask.TYPE_SINGLE_THREAD,
                        "连续点击",
                        "",
                        "",
                        "",
                        null,
                        Arrays.asList(ActionClickCenterInterval.class)
                )
        );
        return mRootView;
    }

}
