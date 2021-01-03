package com.example.chatapp_client.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.*;

public class MyAdapter extends BaseAdapter {
  private ArrayList mData;
  public MyAdapter(Map<String, String> map)  {
    mData = new ArrayList();
    mData.clear();
    mData.addAll(map.entrySet());
  }
  @Override
  public int getCount() {
    return mData.size();
  }

  @Override
  public Map.Entry<String, String> getItem(int position) {
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
    Map.Entry<String, String> entry =  this.getItem(position);
    viewHolder.text2.setText(entry.getValue().toString());
    return convertView;
  }
}