package jp.s5r.android.tuna;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import jp.s5r.android.tuna.model.Log;

import java.util.ArrayList;
import java.util.List;

public class LogsFragment extends Fragment implements TextView.OnEditorActionListener {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private List<Log> mLogs;
    private Adapter mAdapter;
    private ListView mListView;
    private EditText mEditText;

    public static LogsFragment newInstance(int sectionNumber) {
        LogsFragment fragment = new LogsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public LogsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);
        if (rootView != null) {
            mListView = (ListView) rootView.findViewById(R.id.listView);
            mEditText = (EditText) rootView.findViewById(R.id.editText);
            mEditText.setOnEditorActionListener(this);
            mAdapter = new Adapter(getActivity(), getLogs());
            mListView.setAdapter(mAdapter);
        }
        return rootView;
    }

    public void addLog(Log log) {
        if (mLogs == null) {
            mLogs = new ArrayList<Log>();
        }
        mAdapter.add(log);
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mAdapter.getCount() - 1);
    }

    public void addLogs(List<Log> logs) {
        if (mLogs == null) {
            mLogs = new ArrayList<Log>();
        }
        mAdapter.addAll(logs);
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mAdapter.getCount() - 1);
    }

    public List<Log> getLogs() {
        if (mLogs == null) {
            mLogs = new ArrayList<Log>();
        }
        return mLogs;
    }

    private MainActivity getMainActivity() {
        Activity a = getActivity();
        if (a != null && a instanceof MainActivity) {
            return (MainActivity) a;
        }
        return null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getMainActivity().onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private String getEditTextString() {
        String message = null;
        if (mEditText != null) {
            Editable e = mEditText.getText();
            if (e != null) {
                message = e.toString();
            }
        }
        return message;
    }

    private boolean onEnter() {
        String message = getEditTextString();
        if (!TextUtils.isEmpty(message)) {
            getMainActivity().sendMessage("PRIVMSG", "#timeline@tm", message);
            mEditText.setText("");
            return true;
        }
        return false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            return onEnter();
        }
        return false;
    }

    private static class Adapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;
        private List<Log> mLogs;

        private Adapter(Context context, List<Log> logs) {
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mLogs = logs;
        }

        @Override
        public int getCount() {
            return mLogs.size();
        }

        @Override
        public Log getItem(int position) {
            return mLogs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log log = getItem(position);
            View view = convertView;
            ViewHolder holder;
            if (view == null) {
                view = mLayoutInflater.inflate(R.layout.log_item, null);
                holder = new ViewHolder();
                holder.message = (TextView) view.findViewById(R.id.log_item_message);
                holder.nick = (TextView) view.findViewById(R.id.log_item_nick);
                holder.time = (TextView) view.findViewById(R.id.log_item_time);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.time.setText(log.getFormatCreatedAt());
            holder.nick.setText(log.getFrom());
            holder.message.setText(log.getPlainMessage());

            return view;
        }

        class ViewHolder {
            TextView time;
            TextView nick;
            TextView message;
        }

        public void cleanupOldLogs() {
            if (mLogs.size() > 100) {
                for (int i = 0; i < 100; i++) {
                    mLogs.remove(i);
                }
            }
        }

        public void addAll(List<Log> logs) {
            mLogs.addAll(logs);
            cleanupOldLogs();
        }

        public void add(Log log) {
            mLogs.add(log);
            cleanupOldLogs();
        }
    }
}

