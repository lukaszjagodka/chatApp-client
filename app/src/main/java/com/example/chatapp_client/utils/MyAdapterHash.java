package com.example.chatapp_client.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class MyAdapterHash extends BaseAdapter {
  private ArrayList mData;
  public MyAdapterHash(Map<Integer, FindedUser> map)  {
    mData = new ArrayList();
    mData.clear();
    mData.addAll(map.entrySet());
  }
  @Override
  public int getCount() {
    return mData.size();
  }

  @Override
  public Map.Entry<Integer, FindedUser> getItem(int position) {
    return (Map.Entry) mData.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  private class Holder {
    TextView text2;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    Holder viewHolder;
    if (convertView == null) {
      convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
      viewHolder = new Holder();
      viewHolder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
      convertView.setTag(viewHolder);
    } else
      viewHolder = (Holder) convertView.getTag();
    Map.Entry<Integer, FindedUser> entry =  this.getItem(position);
    viewHolder.text2.setText(entry.getValue().getName().toString());
    return convertView;
  }
}