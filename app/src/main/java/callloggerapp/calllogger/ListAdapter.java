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
        if (convertView == null)
            vi = inflater.inflate(R.layout.missedcall_item, null);

        TextView timestamp = (TextView) vi.findViewById(R.id.timestamp);
        TextView number = (TextView) vi.findViewById(R.id.number);
        TextView name = (TextView) vi.findViewById(R.id.cachedname);

        HashMap<String, String> song = new HashMap<String, String>();
        song = data.get(position);

        // Setting the values in ListView

            timestamp.setText(song.get(MainActivity.KEY_NUMBER));
            number.setText(song.get(MainActivity.KEY_TIME));
            name.setText(song.get(MainActivity.KEY_NAME));


        Button deleteBtn = (Button) vi.findViewById(R.id.delete_btn);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do smth
                data.remove(position); // or some other task
                notifyDataSetChanged();
            }
        });

        return vi;

    }
}
