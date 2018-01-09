package callloggerapp.calllogger;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Julian on 30/10/2017.
 */

public class ListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;

    public ListAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;

//        if (convertView == null) <-- recycles but resets positions. Increases performance though ...
            vi = inflater.inflate(R.layout.missedcall_item, null);


        TextView timestamp = (TextView) vi.findViewById(R.id.timestamp);
        TextView number = (TextView) vi.findViewById(R.id.number);
        TextView name = (TextView) vi.findViewById(R.id.cachedname);

        HashMap<String, String> call = new HashMap<String, String>();
        call = data.get(position);

        final String phNumber = call.get(MainActivity.KEY_NUMBER);

        timestamp.setText(call.get(MainActivity.KEY_TIME));

        if (call.get(MainActivity.KEY_NAME) != null) {
            name.setText(call.get(MainActivity.KEY_NAME));
            number.setText(call.get(null));
        }

        if (call.get(MainActivity.KEY_NAME) == null) {
            number.setText(call.get(MainActivity.KEY_NUMBER));
            name.setText(null);
        }

        Button deleteBtn = (Button) vi.findViewById(R.id.delete_btn);
        Button moveUpBtn = (Button) vi.findViewById(R.id.moveup_btn);

        if (position == 0) {
            moveUpBtn.setVisibility(View.GONE);
        }

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.remove(position);
                notifyDataSetChanged();

                MainActivity mActivity = new MainActivity();
                mActivity.dismissCall(activity.getApplicationContext(), phNumber);
            }
        });

        moveUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collections.swap(data, position, position-1);
                notifyDataSetChanged();
            }
        });

        return vi;

    }





}
