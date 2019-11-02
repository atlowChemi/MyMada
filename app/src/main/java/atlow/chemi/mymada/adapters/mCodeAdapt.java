package atlow.chemi.mymada.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.core.widget.CompoundButtonCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import atlow.chemi.mymada.R;
import atlow.chemi.mymada.classes.mCodeKid;

public class mCodeAdapt extends ArrayAdapter<mCodeKid> {
    private Context mContext;
    LayoutInflater inflater;
    private ArrayList<mCodeKid> userKids = null;
    private ArrayList<mCodeKid> arrayList;
    private boolean[] checked, sep;

    private static class ViewHolder {
        TextView mcId, mcSep;
        CheckBox mcName;
    }

    public mCodeAdapt(Context context, ArrayList<mCodeKid> codes, boolean[] sep) {
        super(context, R.layout.m_code_row, codes);
        mContext = context;
        this.userKids = codes;
        this.arrayList = new ArrayList<>();
        this.arrayList.addAll(codes);
        this.checked = new boolean[arrayList.size()];
        this.sep = sep;
    }
    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        mCodeKid msg = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.m_code_row, parent, false);
            viewHolder.mcId = convertView.findViewById(R.id.mcId);
            viewHolder.mcName = convertView.findViewById(R.id.mcName);
            viewHolder.mcSep = convertView.findViewById(R.id.mcSep);
            viewHolder.mcName.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int getPos = (Integer) buttonView.getTag();
                checked[getPos] = isChecked;
                arrayList.get(getPos).setChecked(isChecked);
            });
            convertView.setTag(viewHolder);
            convertView.setTag(R.id.mcName, viewHolder.mcName);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        int order = userKids.get(position).getOrder();
        viewHolder.mcId.setText(arrayList.get(order).getId());
        viewHolder.mcName.setText(arrayList.get(order).getName());
        viewHolder.mcSep.setText(arrayList.get(position).getName());
        Typeface myTypeface = Typeface.createFromAsset(this.mContext.getAssets(), "fonts/opensanshebrew.ttf");
        viewHolder.mcName.setTypeface(myTypeface);
        viewHolder.mcName.setTag(order);
        viewHolder.mcName.setChecked(arrayList.get(order).isChecked());
        ColorStateList colorStateList;
        switch (arrayList.get(order).getCatogory()) {
            case "area":
            case "pain":
                colorStateList = new ColorStateList(
                        new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked} ,},
                        new int[]{Color.parseColor("#4bacc6"),Color.parseColor("#4bacc6"),}
                );
                CompoundButtonCompat.setButtonTintList(viewHolder.mcName, colorStateList);
                viewHolder.mcSep.setBackgroundColor(Color.parseColor("#4bacc6"));
                break;
            case "trauma":
                colorStateList = new ColorStateList(
                        new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked} ,},
                        new int[]{Color.parseColor("#8064a2"),Color.parseColor("#8064a2"),}
                );
                CompoundButtonCompat.setButtonTintList(viewHolder.mcName, colorStateList);
                viewHolder.mcSep.setBackgroundColor(Color.parseColor("#8064a2"));
                break;
            case "vasc":
                colorStateList = new ColorStateList(
                        new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked} ,},
                        new int[]{Color.parseColor("#c0504d"),Color.parseColor("#c0504d"),}
                );
                CompoundButtonCompat.setButtonTintList(viewHolder.mcName, colorStateList);
                viewHolder.mcSep.setBackgroundColor(Color.parseColor("#c0504d"));
                break;
            case "resp":
                colorStateList = new ColorStateList(
                    new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked} ,},
                    new int[]{Color.parseColor("#4f81bd"),Color.parseColor("#4f81bd"),}
                );
                CompoundButtonCompat.setButtonTintList(viewHolder.mcName, colorStateList);
                viewHolder.mcSep.setBackgroundColor(Color.parseColor("#4f81bd"));
                break;
            case "neuro":
                colorStateList = new ColorStateList(
                        new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked} ,},
                        new int[]{Color.parseColor("#a1a30e"),Color.parseColor("#a1a30e"),}
                );
                CompoundButtonCompat.setButtonTintList(viewHolder.mcName, colorStateList);
                viewHolder.mcSep.setBackgroundColor(Color.parseColor("#a1a30e"));
                break;
            case "gastro":
                colorStateList = new ColorStateList(
                        new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked} ,},
                        new int[]{Color.parseColor("#d8863a"),Color.parseColor("#d8863a"),}
                );
                CompoundButtonCompat.setButtonTintList(viewHolder.mcName, colorStateList);
                viewHolder.mcSep.setBackgroundColor(Color.parseColor("#d8863a"));
                break;
            case "infect":
                colorStateList = new ColorStateList(
                        new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked} ,},
                        new int[]{Color.parseColor("#829ca3"),Color.parseColor("#829ca3"),}
                );
                CompoundButtonCompat.setButtonTintList(viewHolder.mcName, colorStateList);
                viewHolder.mcSep.setBackgroundColor(Color.parseColor("#829ca3"));
                break;
            case "pshyc":
                colorStateList = new ColorStateList(
                        new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked} ,},
                        new int[]{Color.parseColor("#00b050"),Color.parseColor("#00b050"),}
                );
                CompoundButtonCompat.setButtonTintList(viewHolder.mcName, colorStateList);
                viewHolder.mcSep.setBackgroundColor(Color.parseColor("#00b050"));
                break;
            case "ando":
                colorStateList = new ColorStateList(
                        new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked} ,},
                        new int[]{Color.parseColor("#92d050"),Color.parseColor("#92d050"),}
                );
                CompoundButtonCompat.setButtonTintList(viewHolder.mcName, colorStateList);
                viewHolder.mcSep.setBackgroundColor(Color.parseColor("#92d050"));
                break;
            case "pish":
                colorStateList = new ColorStateList(
                        new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked} ,},
                        new int[]{Color.parseColor("#ba8280"),Color.parseColor("#ba8280"),}
                );
                CompoundButtonCompat.setButtonTintList(viewHolder.mcName, colorStateList);
                viewHolder.mcSep.setBackgroundColor(Color.parseColor("#ba8280"));
                break;
            case "clali":
                colorStateList = new ColorStateList(
                        new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked} ,},
                        new int[]{Color.parseColor("#6b7177"),Color.parseColor("#6b7177"),}
                );
                CompoundButtonCompat.setButtonTintList(viewHolder.mcName, colorStateList);
                viewHolder.mcSep.setBackgroundColor(Color.parseColor("#6b7177"));
                break;
        }
        if (!arrayList.get(order).getId().contains("sep"))  {
            viewHolder.mcSep.setVisibility(View.GONE);
            viewHolder.mcName.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mcName.setVisibility(View.GONE);
            viewHolder.mcSep.setVisibility(View.VISIBLE);
        }
        return convertView;
    }
    public String getName(int position)   {
        return userKids.get(position).getName();
    }
    public String getId(int position)   {
        return userKids.get(position).getId();
    }
    public void clear()   {
        for (mCodeKid mc : arrayList) {
            mc.setChecked(false);
        }
    }
    public void checkItem(String code)   {
        for (mCodeKid mc : arrayList) {
            if (mc.getId().equals(code)) {
                mc.setChecked(true);
            }
        }
    }
    public String getChecked()  {
        String toReturn = "";
        for (int i=0;i<arrayList.size();i++)  {
            if(checked[i])
                toReturn += arrayList.get(i).getId() + ", ";
        }
        if (toReturn.length() > 1)
            toReturn = toReturn.substring(0, toReturn.length() - 2);
        return toReturn;
    }
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        userKids.clear();
        if(charText.length() == 0)
            userKids.addAll(arrayList);
        else {
            for(mCodeKid uk : arrayList) {
                String name = uk.getName().toLowerCase(Locale.getDefault());
                String id = uk.getId().toLowerCase(Locale.getDefault());
                if(name.contains(charText) || id.contains(charText))
                    userKids.add(uk);
                if (charText.contains("לבבי") && uk.getCatogory().contains("vasc"))
                    userKids.add(uk);

            }
        }
        notifyDataSetChanged();
    }
}