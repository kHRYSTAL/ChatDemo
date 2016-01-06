package huxiu.com.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import huxiu.com.activities.BaseActivity;


/**
 * Created by yao on 15/7/26.
 */
public class ProgressFragment extends DialogFragment {
    public static ProgressFragment show(BaseActivity activity, String title) {
        ProgressFragment newFragment = ProgressFragment.newInstance(title);
       // newFragment.show(activity.getSupportFragmentManager(), "progressDialog");
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(newFragment, "progressDialog");
        ft.commitAllowingStateLoss();
        return newFragment;

    }

    public static ProgressFragment show(Fragment fragment, String title) {
        ProgressFragment newFragment = ProgressFragment.newInstance(title);
        //newFragment.show(fragment.getChildFragmentManager(), "progressDialog");
        FragmentTransaction ft = fragment.getChildFragmentManager().beginTransaction();
        ft.add(newFragment,  "progressDialog");
        ft.commitAllowingStateLoss();
        return newFragment;
    }

    public static ProgressFragment newInstance(String title) {
        ProgressFragment frag = new ProgressFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");

        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(title);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }
}
