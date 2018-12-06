package com.example.wlblu.chatchat;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    //when the user click on the fragment, we get the position on the menu they click
    @Override
    public Fragment getItem(int position) {
       switch (position){
           case 0:
               RequestFragment requestFragment = new RequestFragment();
               return requestFragment;
           case 1:
               ChatsFragment chatsFragment = new ChatsFragment();
               return chatsFragment;
           case 2:
               FriendsFragment friendsFragment = new FriendsFragment();
               return friendsFragment;
           case 3:
               GroupsFragment groupsFragment = new GroupsFragment();
               return groupsFragment;
           default:
               return null;
       }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "Requests";
            case 1:
                return "Chats";
            case 2:
                return "Friends";
            case 3:
                return "Groups";
            default:
                return null;
        }

    }
}
