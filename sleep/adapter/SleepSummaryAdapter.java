package com.desay.iwan2.module.sleep.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.desay.fitband.R;

import java.util.ArrayList;
import java.util.List;

public class SleepSummaryAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<Entity> datas;

    public SleepSummaryAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        datas = new ArrayList<Entity>();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        if (datas.size() > 0)
            return datas.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Entity tmp = datas.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sleep_summary_list_item, null);
            ViewHolder holder = new ViewHolder();
            holder.textView_duration = (TextView) convertView
                    .findViewById(R.id.textView_duration);
            holder.textView_range = (TextView) convertView
                    .findViewById(R.id.textView_range);
            holder.textView_shallow = (TextView) convertView
                    .findViewById(R.id.textView_shallow);
            holder.textView_deep = (TextView) convertView
                    .findViewById(R.id.textView_deep);
            holder.textView_dream = (TextView) convertView
                    .findViewById(R.id.textView_dream);
            holder.textView_wakeup = (TextView) convertView
                    .findViewById(R.id.textView_wakeup);
            convertView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.textView_duration.setText(tmp.duration);
        holder.textView_range.setText(tmp.range);
        holder.textView_shallow.setText(context.getString(R.string.SleepLight) + ":" + (tmp.shallow == null ? "0'" : tmp.shallow));
        holder.textView_deep.setText(context.getString(R.string.SleepDeep) + ":" + (tmp.deep == null ? "0'" : tmp.deep));
        holder.textView_dream.setText(context.getString(R.string.SleepDream) + ":" + (tmp.dream == null ? "0'" : tmp.dream));
        holder.textView_wakeup.setText(context.getString(R.string.SleepWake) + ":" + (tmp.wakeup == null ? "0'" : tmp.wakeup));

        return convertView;
    }

    public List<Entity> getDatas() {
        return datas;
    }

    public void setDatas(List<Entity> datas) {
        this.datas = datas;
        this.notifyDataSetChanged();
    }

    public void addDatas(List<Entity> datas) {
        this.datas.addAll(datas);
        this.notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView textView_duration;
        TextView textView_range;
        TextView textView_shallow;
        TextView textView_deep;
        TextView textView_dream;
        TextView textView_wakeup;
    }

    public static class Entity {
        public String duration;
        public String range;
        public String shallow;
        public String deep;
        public String dream;
        public String wakeup;
    }
}
