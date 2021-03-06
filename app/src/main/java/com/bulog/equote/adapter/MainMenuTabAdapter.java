package com.bulog.equote.adapter;


import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.bulog.equote.fragments.MainMenuProductTab;
import com.bulog.equote.model.smallproduct.DataSmallProduct;
import com.bulog.equote.model.smallproduct.SmallProduct;

import java.util.ArrayList;

public class MainMenuTabAdapter extends FragmentStateAdapter {

    ArrayList<DataSmallProduct> products;

    public MainMenuTabAdapter (FragmentManager f, Lifecycle lifecycle, ArrayList<DataSmallProduct> products){
        super(f, lifecycle);
        this.products = products;
    }

    @Override
    public Fragment createFragment(int pos){
        DataSmallProduct product = products.get(pos);
        return MainMenuProductTab.newInstance(product.getProducts());
    }

    @Override
    public int getItemCount(){
        return products.size();
    }
}
