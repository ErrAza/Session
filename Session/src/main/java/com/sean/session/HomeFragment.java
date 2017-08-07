package com.sean.session;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.parse.session.R;

import java.util.ArrayList;

/**
 * Created by Sean on 8/1/2017.
 */

public class HomeFragment extends Fragment {

    GridView gridView;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        gridView = (GridView) getView().findViewById(R.id.gridView);

        ArrayList<Bitmap> list = new ArrayList<>();

        for (int i = 0; i < ProfileManager.getInstance().currentUser.getArtistList().size(); i++)
        {
            Bitmap bitmap = LocalDataManager.getInstance(getActivity()).FetchBitmapFromLocal(ProfileManager.getInstance().currentUser.getArtistList().get(i));
            list.add(bitmap);
        }

        gridView.setAdapter(new ImageAdapter(getActivity(), list));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }
}
