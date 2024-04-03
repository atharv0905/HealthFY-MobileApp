package com.atharv.potholehealthdetector;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;


import java.util.List;

public class MenuAdapter extends ArrayAdapter<String> {

    private SharedPreferences userSharedPreferences;
    private  SharedPreferences sharedPreferences;

    private String TOKEN = "Token";
    private String USER_AUTHENTICATION_PREF_NAME = "USER_AUTHENTICATION";
    private String USER = "USER";
    private String USER_PREF_NAME = "USERNAME";
    private Context mContext;
    private List<String> mMenuList;

    public MenuAdapter(Context context, List<String> menuList) {
        super(context, 0, menuList);
        mContext = context;
        mMenuList = menuList;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        String currentItem = mMenuList.get(position);

        // Set the text for the item
        TextView textView = listItem.findViewById(android.R.id.text1);
        textView.setText(currentItem);

        // Set onClickListener for each item
        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle item click here
                // You can use 'position' to identify which item is clicked
                String selectedItem = mMenuList.get(position);
                performAction(selectedItem);
            }
        });

        return listItem;
    }

    private void performAction(String selectedItem) {
        // Perform different tasks based on the selected item
        switch (selectedItem) {
            case "Home":
                // Start the HomeActivity
                Intent homeIntent = new Intent(mContext, CaptureActivity.class);
                mContext.startActivity(homeIntent);
                break;
            case "History":
                // Start the HistoryActivity
                Intent historyIntent = new Intent(mContext, HistoryActivity.class);
                mContext.startActivity(historyIntent);
                break;
            case "Logout":
                // Logout User
                sharedPreferences = mContext.getSharedPreferences(USER_AUTHENTICATION_PREF_NAME, MODE_PRIVATE);
                userSharedPreferences = mContext.getSharedPreferences(USER_PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                SharedPreferences.Editor user = userSharedPreferences.edit();
                user.clear();
                user.apply();
                Intent logoutIntent = new Intent(mContext, LoginActivity.class);
                mContext.startActivity(logoutIntent);
                break;
            default:
                // Default action if none of the above cases match
                break;
        }
    }


}

