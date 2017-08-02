package com.parse.session;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseUser;

/**
 * Created by Sean on 8/1/2017.
 */

public class ProfileFragment extends Fragment {

    TextView usernameTextView;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        usernameTextView = (TextView) getView().findViewById(R.id.usernameTextView);
        usernameTextView.setText(ParseUser.getCurrentUser().getUsername());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {

        usernameTextView = null;

        super.onDestroyView();
    }
}
