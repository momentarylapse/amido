package com.example.michi.amido;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddToListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddToListFragment extends DialogFragment {
    private String type;
    private int char_id;

    public AddToListFragment() {
        // Required empty public constructor
    }

    public static AddToListFragment newInstance(Character c) {
        AddToListFragment fragment = new AddToListFragment();
        Bundle args = new Bundle();
        args.putString("type", c.type);
        args.putInt("id", c.id);
        fragment.setArguments(args);
        return fragment;
        }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            char_id = getArguments().getInt("id");
            type = getArguments().getString("type");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_add_to_list, container, false);

        final ListManager lm = ListManager.getInstance(view.getContext());

        Button b = (Button)view.findViewById(R.id.button_ok);
        final TextView t = (TextView)view.findViewById(R.id.edit_name);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = t.getText().toString();
                if (key.length() > 0)
                    lm.addToUserList(type, key, char_id);
                AddToListFragment.this.dismiss();
            }
        });

        ArrayAdapter<String> aa = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1);
        ListView lv = (ListView)view.findViewById(R.id.list_list);
        lv.setAdapter(aa);
        final ArrayList<ListManager.List> lists = lm.getUserLists(type);
        for (ListManager.List l : lists)
            aa.add(l.key);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                t.setText(lists.get(position).key);
                Toast.makeText(view.getContext(), lists.get(position).key, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //dialog.requestWindowFeature()
        return dialog;
    }

}
